package com.example.demo.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MrpWarehouseService {

    private final JdbcTemplate jdbc;

    public MrpWarehouseService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // =====================================================
    // NHẬP NGUYÊN VẬT LIỆU
    // Nhà cung cấp -> Kho NVL trung tâm
    // =====================================================

    @Transactional
    public void completeMaterialReceipt(
            long receiptId,
            String managerEmail) {

        String status = lockStatus(
                "phieunhap_nvl",
                "mapn",
                receiptId
        );

        if ("DA_NHAP".equals(status)) {
            throw new IllegalStateException(
                    "Phiếu nhập đã được xử lý trước đó."
            );
        }

        requireStatus(status, "NHAP_NHAP");

        List<Line> lines = jdbc.query(
                """
                SELECT
                    manvl,
                    SUM(soluong),
                    MAX(dongia)
                FROM ct_phieunhap_nvl
                WHERE mapn = ?
                GROUP BY manvl
                ORDER BY manvl
                """,
                (rs, row) -> new Line(
                        rs.getLong(1),
                        0L,
                        rs.getBigDecimal(2),
                        rs.getBigDecimal(3)
                ),
                receiptId
        );

        requireLines(lines);

        // =====================================================
        // KIỂM TRA LẠI TRẠNG THÁI NVL + LOẠI NVL
        // NGAY TẠI THỜI ĐIỂM DUYỆT
        // =====================================================

        validateActiveMaterialsForApproval(lines);

        Integer actor = employeeId(managerEmail);

        // =====================================================
        // KHÓA TỒN KHO + CỘNG TỒN
        // =====================================================

        for (Line line : lines) {

            requirePositive(line.quantity());

            BigDecimal before = lockStock(
                    "nguyenvatlieu",
                    "manvl",
                    line.itemId()
            );

            BigDecimal after =
                    before.add(line.quantity());

            jdbc.update(
                    """
                    UPDATE nguyenvatlieu
                    SET soluongton = ?
                    WHERE manvl = ?
                    """,
                    after,
                    line.itemId()
            );

            history(
                    "NHAP_NVL",
                    "NVL",
                    line,
                    actor,
                    "phieunhap_nvl",
                    receiptId
            );
        }

        jdbc.update(
                """
                UPDATE phieunhap_nvl
                SET trangthai = 'DA_NHAP'
                WHERE mapn = ?
                """,
                receiptId
        );
    }


    // =====================================================
    // XUẤT NGUYÊN VẬT LIỆU
    //
    // Kho NVL trung tâm
    //        |
    //        | xuất
    //        v
    // Kho NVL sản xuất
    // =====================================================

    @Transactional
    public void issueMaterials(long issueId) {
        issueMaterials(issueId, null);
    }

    @Transactional
    public void issueMaterials(
            long issueId,
            String managerEmail) {

        // =====================================================
        // 1. KHÓA PHIẾU XUẤT
        // =====================================================

        String status = lockStatus(
                "phieuxuat_nvl",
                "mapx",
                issueId
        );

        if ("DA_XUAT".equals(status)) {
            throw new IllegalStateException(
                    "Phiếu xuất đã được xử lý trước đó."
            );
        }

        requireStatus(status, "CHO_DUYET");


        // =====================================================
// KIỂM TRA CÁC YÊU CẦU ĐƯỢC GOM VÀO PHIẾU
// TẤT CẢ PHẢI THUỘC CÙNG MỘT ĐỢT SẢN XUẤT
// =====================================================

        List<Map<String, Object>> batchChecks = jdbc.queryForList(
                """
                SELECT
                    COUNT(py.maycsx) AS total_links,
                    COUNT(DISTINCT y.madot) AS batch_count,
                    SUM(
                        CASE
                            WHEN py.maycsx = p.maycsx THEN 1
                            ELSE 0
                        END
                    ) AS header_linked
                FROM phieuxuat_nvl p
                LEFT JOIN phieuxuat_nvl_ycsx py
                    ON py.mapx = p.mapx
                LEFT JOIN yeucausanxuat y
                    ON y.maycsx = py.maycsx
                WHERE p.mapx = ?
                """,
                issueId
        );

        Map<String, Object> batchCheck = batchChecks.get(0);

        long totalLinks = ((Number) batchCheck.get("total_links")).longValue();
        long batchCount = ((Number) batchCheck.get("batch_count")).longValue();
        long headerLinked = batchCheck.get("header_linked") == null
                ? 0L
                : ((Number) batchCheck.get("header_linked")).longValue();

        if (totalLinks > 0) {
            if (batchCount != 1) {
                throw new IllegalStateException(
                        "Các yêu cầu trong phiếu xuất phải thuộc cùng một đợt sản xuất."
                );
            }

            if (headerLinked == 0) {
                throw new IllegalStateException(
                        "Yêu cầu sản xuất đại diện của phiếu chưa được liên kết hợp lệ."
                );
            }
        }




        // =====================================================
        // 2. LẤY CHI TIẾT PHIẾU
        // =====================================================

        List<Line> lines = jdbc.query(
                """
                SELECT
                    manvl,
                    SUM(soluong)
                FROM ct_phieuxuat_nvl
                WHERE mapx = ?
                GROUP BY manvl
                ORDER BY manvl
                """,
                (rs, row) -> new Line(
                        rs.getLong(1),
                        0L,
                        rs.getBigDecimal(2),
                        BigDecimal.ZERO
                ),
                issueId
        );

        requireLines(lines);


        // =====================================================
        // 3. KIỂM TRA LẠI TRẠNG THÁI NVL + LOẠI NVL
        // NGAY TẠI THỜI ĐIỂM DUYỆT
        // =====================================================

        validateActiveMaterialsForApproval(lines);


        // =====================================================
        // 4. KIỂM TRA TỒN KHO
        //
        // Đồng thời khóa:
        // - Kho NVL trung tâm
        // - Kho NVL sản xuất
        // =====================================================

        List<MaterialStockChange> changes =
                new ArrayList<>();

        for (Line line : lines) {

            requirePositive(line.quantity());

            // -------------------------------------------------
            // Khóa tồn kho NVL trung tâm
            // -------------------------------------------------

            BigDecimal centralBefore = lockStock(
                    "nguyenvatlieu",
                    "manvl",
                    line.itemId()
            );


            // -------------------------------------------------
            // Kiểm tra tồn kho trung tâm
            // -------------------------------------------------

            if (centralBefore.compareTo(line.quantity()) < 0) {

                String name = jdbc.queryForObject(
                        """
                        SELECT tennvl
                        FROM nguyenvatlieu
                        WHERE manvl = ?
                        """,
                        String.class,
                        line.itemId()
                );

                throw new IllegalStateException(
                        "Không đủ nguyên vật liệu "
                                + name
                                + ". Tồn hiện tại: "
                                + centralBefore
                                + ". Số lượng yêu cầu: "
                                + line.quantity()
                                + "."
                );
            }


            // -------------------------------------------------
            // Khóa tồn kho NVL sản xuất
            // -------------------------------------------------

            BigDecimal productionBefore = lockStock(
                    "kho_nvl_san_xuat",
                    "manvl",
                    line.itemId()
            );


            // -------------------------------------------------
            // Tính tồn sau khi chuyển kho
            // -------------------------------------------------

            BigDecimal centralAfter =
                    centralBefore.subtract(line.quantity());

            BigDecimal productionAfter =
                    productionBefore.add(line.quantity());


            changes.add(
                    new MaterialStockChange(
                            line,
                            centralBefore,
                            centralAfter,
                            productionBefore,
                            productionAfter
                    )
            );
        }


        // =====================================================
        // 5. NGƯỜI DUYỆT
        // =====================================================

        Integer actor =
                managerEmail == null
                        ? null
                        : employeeId(managerEmail);


        // =====================================================
        // 6. THỰC HIỆN CHUYỂN KHO
        //
        // Kho trung tâm:
        //     - quantity
        //
        // Kho sản xuất:
        //     + quantity
        // =====================================================

        for (MaterialStockChange change : changes) {

            // -------------------------------------------------
            // Trừ kho NVL trung tâm
            // -------------------------------------------------

            jdbc.update(
                    """
                    UPDATE nguyenvatlieu
                    SET soluongton = ?
                    WHERE manvl = ?
                    """,
                    change.afterCentral(),
                    change.line().itemId()
            );


            // -------------------------------------------------
            // Cộng kho NVL sản xuất
            // -------------------------------------------------

            jdbc.update(
                    """
                    UPDATE kho_nvl_san_xuat
                    SET soluongton = ?
                    WHERE manvl = ?
                    """,
                    change.afterProduction(),
                    change.line().itemId()
            );


            // -------------------------------------------------
            // Ghi lịch sử kho trung tâm
            // -------------------------------------------------

            history(
                    "XUAT_NVL",
                    "NVL",
                    change.line(),
                    actor,
                    "phieuxuat_nvl",
                    issueId
            );


            // -------------------------------------------------
            // Ghi lịch sử kho NVL sản xuất
            //
            // Trung tâm:
            //     XUAT_NVL
            //
            // Kho sản xuất:
            //     NHAP_NVL_SAN_XUAT
            // -------------------------------------------------

            productionHistory(
                    "NVL_SAN_XUAT",
                    "NVL",
                    change.line().itemId(),
                    "NHAP_NVL_SAN_XUAT",
                    change.line().quantity(),
                    change.beforeProduction(),
                    change.afterProduction(),
                    actor,
                    "phieuxuat_nvl",
                    issueId
            );
        }


        // =====================================================
        // 7. ĐÁNH DẤU PHIẾU ĐÃ XUẤT
        // =====================================================

        jdbc.update(
                """
                UPDATE phieuxuat_nvl
                SET
                    trangthai = 'DA_XUAT',
                    nguoiduyet = ?,
                    ngayduyet = CURRENT_TIMESTAMP
                WHERE mapx = ?
                """,
                actor,
                issueId
        );
    }


    // =====================================================
    // TỪ CHỐI PHIẾU XUẤT NVL
    // =====================================================

    @Transactional
    public void rejectMaterialIssue(
            long issueId,
            String reason,
            String managerEmail) {

        String status = lockStatus(
                "phieuxuat_nvl",
                "mapx",
                issueId
        );

        requireStatus(status, "CHO_DUYET");

        requireReason(reason);

        jdbc.update(
                """
                UPDATE phieuxuat_nvl
                SET
                    trangthai = 'TU_CHOI',
                    lydo = ?,
                    nguoiduyet = ?,
                    ngayduyet = CURRENT_TIMESTAMP
                WHERE mapx = ?
                """,
                reason.trim(),
                employeeId(managerEmail),
                issueId
        );
    }


    // =====================================================
    // XÁC NHẬN KẾT QUẢ SẢN XUẤT
    //
    // Kho thành phẩm sản xuất
    //        |
    //        | xuất
    //        v
    // Kho thành phẩm trung tâm
    // =====================================================

    @Transactional
    public void confirmProduction(long resultId) {
        confirmProduction(resultId, null);
    }

    @Transactional
    public void confirmProduction(
            long resultId,
            String managerEmail) {

        String status = lockStatus(
                "ketqua_sanxuat",
                "makqsx",
                resultId
        );

        if ("DA_NHAP_KHO".equals(status)) {
            throw new IllegalStateException(
                    "Kết quả đã được xác nhận trước đó."
            );
        }

        requireStatus(status, "CHO_XAC_NHAN");

        ProductionResult result = jdbc.queryForObject(
                """
                SELECT
                    maycsx,
                    soluonghoanthanh
                FROM ketqua_sanxuat
                WHERE makqsx = ?
                """,
                (rs, row) -> new ProductionResult(
                        rs.getLong(1),
                        rs.getInt(2)
                ),
                resultId
        );

        if (result == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy kết quả sản xuất."
            );
        }

        if (result.quantity() <= 0) {
            throw new IllegalArgumentException(
                    "Số lượng hoàn thành phải lớn hơn 0."
            );
        }

        ProductionOrder order = jdbc.queryForObject(
                """
                SELECT
                    masp,
                    mabom,
                    soluongyeucau,
                    soluonghoanthanh
                FROM yeucausanxuat
                WHERE maycsx = ?
                FOR UPDATE
                """,
                (rs, row) -> new ProductionOrder(
                        rs.getLong(1),
                        rs.getLong(2),
                        rs.getInt(3),
                        rs.getInt(4)
                ),
                result.orderId()
        );

        if (order == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy yêu cầu sản xuất."
            );
        }

        if (order.completed() + result.quantity()
                > order.requested()) {

            throw new IllegalStateException(
                    "Số lượng hoàn thành vượt quá số lượng còn lại "
                            + "của yêu cầu sản xuất."
            );
        }

        Integer recorded = jdbc.queryForObject(
                """
                SELECT COALESCE(SUM(soluong),0)
                FROM ghi_nhan_san_xuat
                WHERE maycsx = ?
                """,
                Integer.class,
                result.orderId()
        );

        if (recorded == null) {
            recorded = 0;
        }

        Integer pendingOther = jdbc.queryForObject(
                """
                SELECT COALESCE(SUM(soluonghoanthanh),0)
                FROM ketqua_sanxuat
                WHERE maycsx = ?
                  AND trangthai = 'CHO_XAC_NHAN'
                  AND makqsx <> ?
                """,
                Integer.class,
                result.orderId(),
                resultId
        );

        if (pendingOther == null) {
            pendingOther = 0;
        }

        int unsubmitted =
                recorded - pendingOther;

        if (result.quantity() > unsubmitted) {

            throw new IllegalStateException(
                    "Kết quả xác nhận vượt quá số lượng "
                            + "đã ghi nhận chưa gửi của YCSX."
            );
        }


        // =====================================================
        // KHÓA TỒN KHO THÀNH PHẨM SẢN XUẤT
        // =====================================================

        BigDecimal before = lockStock(
                "kho_thanhpham_san_xuat",
                "masp",
                order.productId(),
                "mabom",
                order.bomId()
        );

        BigDecimal quantity =
                BigDecimal.valueOf(result.quantity());

        if (before.compareTo(quantity) < 0) {

            throw new IllegalStateException(
                    "Kho thành phẩm sản xuất không đủ "
                            + "số lượng để xác nhận."
            );
        }

        BigDecimal after =
                before.subtract(quantity);


        // =====================================================
        // TRỪ KHO THÀNH PHẨM SẢN XUẤT
        // =====================================================

        jdbc.update(
                """
                UPDATE kho_thanhpham_san_xuat
                SET soluongton = ?
                WHERE masp = ?
                  AND mabom = ?
                """,
                after,
                order.productId(),
                order.bomId()
        );


        // =====================================================
        // GHI LỊCH SỬ KHO THÀNH PHẨM SẢN XUẤT
        // =====================================================

        productionHistory(
                "THANH_PHAM_SAN_XUAT",
                "THANH_PHAM",
                order.productId(),
                "XUAT_THANH_PHAM_SAN_XUAT",
                quantity,
                before,
                after,
                managerEmail == null
                        ? null
                        : employeeId(managerEmail),
                "ketqua_sanxuat",
                resultId
        );


        // =====================================================
        // KHÓA + CỘNG KHO THÀNH PHẨM TRUNG TÂM
        // =====================================================

        BigDecimal centralBefore = lockStock(
                "sanpham",
                "masp",
                order.productId()
        );

        BigDecimal centralAfter =
                centralBefore.add(quantity);

        jdbc.update(
                """
                UPDATE sanpham
                SET soluongton = ?
                WHERE masp = ?
                """,
                centralAfter,
                order.productId()
        );


        // =====================================================
        // CẬP NHẬT TIẾN ĐỘ YCSX
        // =====================================================

        int completed =
                order.completed()
                        + result.quantity();

        jdbc.update(
                """
                UPDATE yeucausanxuat
                SET
                    soluonghoanthanh = ?,
                    trangthai = ?
                WHERE maycsx = ?
                """,
                completed,
                completed >= order.requested()
                        ? "HOAN_THANH"
                        : "DANG_SAN_XUAT",
                result.orderId()
        );


        // =====================================================
        // ĐỐI SOÁT / XÓA SỐ LƯỢNG ĐÃ GHI NHẬN
        // =====================================================

        consumeRecordedProduction(
                result.orderId(),
                result.quantity()
        );


        // =====================================================
        // GHI LỊCH SỬ KHO THÀNH PHẨM TRUNG TÂM
        // =====================================================

        history(
                "NHAP_THANH_PHAM",
                "THANH_PHAM",
                new Line(
                        order.productId(),
                        order.bomId(),
                        quantity,
                        BigDecimal.ZERO
                ),
                managerEmail == null
                        ? null
                        : employeeId(managerEmail),
                "ketqua_sanxuat",
                resultId
        );


        // =====================================================
        // ĐÁNH DẤU KẾT QUẢ ĐÃ NHẬP KHO
        // =====================================================

        jdbc.update(
                """
                UPDATE ketqua_sanxuat
                SET trangthai = 'DA_NHAP_KHO'
                WHERE makqsx = ?
                """,
                resultId
        );
    }


    // =====================================================
    // ĐỐI SOÁT GHI NHẬN SẢN XUẤT
    // =====================================================

    private void consumeRecordedProduction(
            long orderId,
            int quantity) {

        int remaining = quantity;

        List<Map<String, Object>> rows =
                jdbc.queryForList(
                        """
                        SELECT
                            id,
                            soluong
                        FROM ghi_nhan_san_xuat
                        WHERE maycsx = ?
                        ORDER BY id
                        FOR UPDATE
                        """,
                        orderId
                );

        for (Map<String, Object> row : rows) {

            if (remaining <= 0) {
                break;
            }

            long id =
                    ((Number) row.get("id"))
                            .longValue();

            int q =
                    ((Number) row.get("soluong"))
                            .intValue();

            if (q <= remaining) {

                jdbc.update(
                        """
                        DELETE FROM ghi_nhan_san_xuat
                        WHERE id = ?
                        """,
                        id
                );

                remaining -= q;

            } else {

                jdbc.update(
                        """
                        UPDATE ghi_nhan_san_xuat
                        SET soluong = soluong - ?
                        WHERE id = ?
                        """,
                        remaining,
                        id
                );

                remaining = 0;
            }
        }

        if (remaining > 0) {

            throw new IllegalStateException(
                    "Không thể đối soát số lượng "
                            + "đã ghi nhận sản xuất."
            );
        }
    }


    // =====================================================
    // XUẤT THÀNH PHẨM
    // Kho thành phẩm trung tâm
    // =====================================================

    @Transactional
    public void issueFinishedProducts(
            long issueId,
            String managerEmail) {

        String status = lockStatus(
                "phieuxuat_thanhpham",
                "mapxtp",
                issueId
        );

        if ("DA_XUAT".equals(status)) {
            throw new IllegalStateException(
                    "Phiếu xuất đã được xử lý trước đó."
            );
        }

        requireStatus(
                status,
                "CHO_DUYET"
        );


        // =====================================================
        // LẤY CHI TIẾT PHIẾU
        // =====================================================

        List<Line> lines = jdbc.query(
                """
                SELECT
                    masp,
                    mabom,
                    SUM(soluong),
                    MAX(dongia)
                FROM ct_phieuxuat_thanhpham
                WHERE mapxtp = ?
                GROUP BY masp, mabom
                ORDER BY masp, mabom
                """,
                (rs, row) -> new Line(
                        rs.getLong(1),
                        rs.getLong(2),
                        rs.getBigDecimal(3),
                        rs.getBigDecimal(4)
                ),
                issueId
        );

        requireLines(lines);

        List<StockChange> changes =
                new ArrayList<>();


        for (Line line : lines) {

            requirePositive(line.quantity());


            // =================================================
            // KIỂM TRA BOM
            // =================================================

            Integer bomProduct = jdbc.query(
                    """
                    SELECT masp
                    FROM bom
                    WHERE mabom = ?
                    """,
                    ps -> ps.setLong(
                            1,
                            line.bomId()
                    ),
                    rs -> rs.next()
                            ? rs.getInt("masp")
                            : null
            );

            if (bomProduct == null) {

                throw new IllegalStateException(
                        "Không tìm thấy BOM "
                                + line.bomId()
                                + "."
                );
            }

            if (bomProduct.longValue()
                    != line.itemId()) {

                throw new IllegalStateException(
                        "BOM "
                                + line.bomId()
                                + " không thuộc sản phẩm "
                                + line.itemId()
                                + "."
                );
            }


            // =================================================
            // KIỂM TRA BOM CÒN HIỆU LỰC
            // =================================================

            String bomStatus = jdbc.query(
                    """
                    SELECT trangthai
                    FROM bom
                    WHERE mabom = ?
                    """,
                    ps -> ps.setLong(
                            1,
                            line.bomId()
                    ),
                    rs -> rs.next()
                            ? rs.getString("trangthai")
                            : null
            );

            if (!"HIEU_LUC".equals(bomStatus)) {

                throw new IllegalStateException(
                        "BOM "
                                + line.bomId()
                                + " đã ngừng sử dụng, "
                                + "không thể xuất thành phẩm."
                );
            }


            // =================================================
            // KHÓA TỒN KHO THÀNH PHẨM
            // =================================================

            BigDecimal before = lockStock(
                    "sanpham",
                    "masp",
                    line.itemId()
            );

            if (before.compareTo(line.quantity()) < 0) {

                throw new IllegalStateException(
                        "Không đủ thành phẩm. "
                                + "Tồn hiện tại: "
                                + before
                                + ". Số lượng yêu cầu: "
                                + line.quantity()
                                + "."
                );
            }


            // =================================================
            // TÍNH THÀNH PHẨM ĐÃ SẢN XUẤT THEO BOM
            // =================================================

            BigDecimal produced =
                    jdbc.queryForObject(
                            """
                            SELECT
                                COALESCE(
                                    SUM(k.soluonghoanthanh),
                                    0
                                )
                            FROM yeucausanxuat y
                            JOIN ketqua_sanxuat k
                                ON k.maycsx = y.maycsx
                            WHERE y.mabom = ?
                              AND k.trangthai = 'DA_NHAP_KHO'
                            """,
                            BigDecimal.class,
                            line.bomId()
                    );


            // =================================================
            // TÍNH THÀNH PHẨM ĐÃ XUẤT THEO BOM
            // =================================================

            BigDecimal exported =
                    jdbc.queryForObject(
                            """
                            SELECT
                                COALESCE(
                                    SUM(c.soluong),
                                    0
                                )
                            FROM phieuxuat_thanhpham p
                            JOIN ct_phieuxuat_thanhpham c
                                ON c.mapxtp = p.mapxtp
                            WHERE c.mabom = ?
                              AND p.trangthai = 'DA_XUAT'
                            """,
                            BigDecimal.class,
                            line.bomId()
                    );


            BigDecimal bomAvailable =
                    produced.subtract(exported);

            if (bomAvailable.compareTo(
                    line.quantity()) < 0) {

                throw new IllegalStateException(
                        "Không đủ thành phẩm theo BOM "
                                + line.bomId()
                                + ". Còn lại: "
                                + bomAvailable
                                + ". Số lượng yêu cầu: "
                                + line.quantity()
                                + "."
                );
            }


            changes.add(
                    new StockChange(
                            line,
                            before,
                            before.subtract(
                                    line.quantity()
                            )
                    )
            );
        }


        Integer actor =
                managerEmail == null
                        ? null
                        : employeeId(managerEmail);


        // =====================================================
        // THỰC SỰ TRỪ TỒN KHO THÀNH PHẨM
        // =====================================================

        for (StockChange change : changes) {

            jdbc.update(
                    """
                    UPDATE sanpham
                    SET soluongton = ?
                    WHERE masp = ?
                    """,
                    change.after().intValueExact(),
                    change.line().itemId()
            );

            history(
                    "XUAT_THANH_PHAM",
                    "THANH_PHAM",
                    change.line(),
                    actor,
                    "phieuxuat_thanhpham",
                    issueId
            );
        }


        // =====================================================
        // ĐÁNH DẤU PHIẾU ĐÃ XUẤT
        // =====================================================

        jdbc.update(
                """
                UPDATE phieuxuat_thanhpham
                SET
                    trangthai = 'DA_XUAT',
                    nguoiduyet = ?,
                    ngayduyet = CURRENT_TIMESTAMP
                WHERE mapxtp = ?
                """,
                actor,
                issueId
        );
    }


    // =====================================================
    // TỪ CHỐI PHIẾU XUẤT THÀNH PHẨM
    // =====================================================

    @Transactional
    public void rejectFinishedIssue(
            long issueId,
            String reason,
            String managerEmail) {

        String status = lockStatus(
                "phieuxuat_thanhpham",
                "mapxtp",
                issueId
        );

        requireStatus(
                status,
                "CHO_DUYET"
        );

        requireReason(reason);

        jdbc.update(
                """
                UPDATE phieuxuat_thanhpham
                SET
                    trangthai = 'TU_CHOI',
                    lydo = ?,
                    nguoiduyet = ?,
                    ngayduyet = CURRENT_TIMESTAMP
                WHERE mapxtp = ?
                """,
                reason.trim(),
                employeeId(managerEmail),
                issueId
        );
    }


    // =====================================================
    // KIỂM TRA TRẠNG THÁI NVL + LOẠI NVL
    // TẠI THỜI ĐIỂM DUYỆT
    // =====================================================

    private void validateActiveMaterialsForApproval(
            List<Line> lines) {

        String sql =
                """
                SELECT
                    n.tennvl,
                    n.trangthai AS nvl_trangthai,
                    l.tenloai,
                    l.trangthai AS loai_trangthai
                FROM nguyenvatlieu n
                JOIN loai_nguyen_vat_lieu l
                    ON l.maloai = n.maloai
                WHERE n.manvl = ?
                """;

        for (Line line : lines) {

            List<Map<String, Object>> rows =
                    jdbc.queryForList(
                            sql,
                            line.itemId()
                    );

            if (rows.isEmpty()) {

                throw new IllegalStateException(
                        "Nguyên vật liệu không tồn tại: "
                                + line.itemId()
                );
            }

            Map<String, Object> row =
                    rows.get(0);

            int materialStatus =
                    ((Number) row.get(
                            "nvl_trangthai"
                    )).intValue();

            int typeStatus =
                    ((Number) row.get(
                            "loai_trangthai"
                    )).intValue();

            String materialName =
                    String.valueOf(
                            row.get("tennvl")
                    );

            String typeName =
                    String.valueOf(
                            row.get("tenloai")
                    );


            // -------------------------------------------------
            // NVL đã ngừng
            // -------------------------------------------------

            if (materialStatus != 1) {

                throw new IllegalStateException(
                        "Không thể duyệt phiếu. "
                                + "Nguyên vật liệu \""
                                + materialName
                                + "\" đã ngừng sử dụng."
                );
            }


            // -------------------------------------------------
            // Loại NVL đã ngừng
            // -------------------------------------------------

            if (typeStatus != 1) {

                throw new IllegalStateException(
                        "Không thể duyệt phiếu. "
                                + "Loại nguyên vật liệu \""
                                + typeName
                                + "\" đã ngừng sử dụng."
                );
            }
        }
    }


    // =====================================================
    // LẤY MÃ NHÂN VIÊN TỪ EMAIL
    // =====================================================

    private int employeeId(String email) {

        Integer id = jdbc.queryForObject(
                """
                SELECT manv
                FROM taikhoan
                WHERE email = ?
                """,
                Integer.class,
                email
        );

        if (id == null) {
            throw new IllegalStateException(
                    "Tài khoản chưa liên kết với nhân viên."
            );
        }

        return id;
    }


    // =====================================================
    // KHÓA TRẠNG THÁI CHỨNG TỪ
    // =====================================================

    private String lockStatus(
            String table,
            String key,
            long id) {

        List<String> values =
                jdbc.query(
                        "SELECT trangthai FROM "
                                + table
                                + " WHERE "
                                + key
                                + " = ? FOR UPDATE",
                        (rs, row) ->
                                rs.getString(1),
                        id
                );

        if (values.isEmpty()) {

            throw new IllegalArgumentException(
                    "Không tìm thấy chứng từ: "
                            + id
            );
        }

        return values.get(0);
    }


    // =====================================================
    // KHÓA TỒN KHO
    // =====================================================

    private BigDecimal lockStock(
            String table,
            String key,
            long id) {

        List<Number> values = jdbc.query(
                "SELECT COALESCE(soluongton, 0) FROM "
                        + table
                        + " WHERE "
                        + key
                        + " = ? FOR UPDATE",
                (rs, row) ->
                        (Number) rs.getObject(1),
                id
        );

        if (values.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không tìm thấy hàng hóa: "
                            + id
            );
        }

        return values.get(0) == null
                ? BigDecimal.ZERO
                : new BigDecimal(
                values.get(0).toString()
        );
    }


    // =====================================================
    // KHÓA TỒN KHO COMPOSITE
    // =====================================================

    private BigDecimal lockStock(
            String table,
            String key1,
            long id1,
            String key2,
            long id2) {

        List<Number> values = jdbc.query(
                "SELECT COALESCE(soluongton, 0) FROM "
                        + table
                        + " WHERE "
                        + key1
                        + " = ? AND "
                        + key2
                        + " = ? FOR UPDATE",
                (rs, row) ->
                        (Number) rs.getObject(1),
                id1,
                id2
        );

        if (values.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không tìm thấy tồn kho sản xuất."
            );
        }

        return values.get(0) == null
                ? BigDecimal.ZERO
                : new BigDecimal(
                values.get(0).toString()
        );
    }


    // =====================================================
    // GHI LỊCH SỬ KHO TRUNG TÂM
    // =====================================================

    private void history(
            String type,
            String kind,
            Line line,
            Integer actor,
            String sourceTable,
            long documentId) {

        jdbc.update(
                """
                INSERT INTO lich_su_kho
                (
                    loai_hang,
                    ma_hang,
                    loai_giaodich,
                    soluong,
                    don_gia,
                    nguoi_thuchien,
                    bang_tham_chieu,
                    ma_tham_chieu
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                kind,
                line.itemId(),
                type,
                line.quantity(),
                line.price(),
                actor,
                sourceTable,
                documentId
        );
    }


    // =====================================================
    // GHI LỊCH SỬ KHO SẢN XUẤT
    // =====================================================

    private void productionHistory(
            String warehouseType,
            String kind,
            long itemId,
            String transactionType,
            BigDecimal quantity,
            BigDecimal before,
            BigDecimal after,
            Integer actor,
            String sourceTable,
            long documentId) {

        jdbc.update(
                """
                INSERT INTO lich_su_kho_san_xuat
                (
                    loai_kho,
                    loai_hang,
                    ma_hang,
                    loai_giao_dich,
                    soluong,
                    ton_truoc,
                    ton_sau,
                    nguoi_thuchien,
                    bang_tham_chieu,
                    ma_tham_chieu
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                warehouseType,
                kind,
                itemId,
                transactionType,
                quantity,
                before,
                after,
                actor,
                sourceTable,
                documentId
        );
    }


    // =====================================================
    // VALIDATE STATUS
    // =====================================================

    private void requireStatus(
            String actual,
            String expected) {

        if (!expected.equals(actual)) {

            throw new IllegalStateException(
                    "Chứng từ không ở trạng thái "
                            + expected
                            + "."
            );
        }
    }


    // =====================================================
    // VALIDATE SỐ LƯỢNG
    // =====================================================

    private void requirePositive(
            BigDecimal quantity) {

        if (quantity == null
                || quantity.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Số lượng phải lớn hơn 0."
            );
        }
    }


    // =====================================================
    // VALIDATE CHI TIẾT PHIẾU
    // =====================================================

    private void requireLines(
            List<Line> lines) {

        if (lines.isEmpty()) {

            throw new IllegalArgumentException(
                    "Phiếu phải có ít nhất một dòng chi tiết."
            );
        }
    }


    // =====================================================
    // VALIDATE LÝ DO TỪ CHỐI
    // =====================================================

    private void requireReason(
            String reason) {

        if (reason == null
                || reason.isBlank()
                || reason.length() > 255) {

            throw new IllegalArgumentException(
                    "Lý do từ chối phải có từ 1 đến 255 ký tự."
            );
        }
    }


    // =====================================================
    // RECORDS
    // =====================================================

    private record Line(
            long itemId,
            long bomId,
            BigDecimal quantity,
            BigDecimal price
    ) {}


    // Dùng riêng cho luồng:
    // Kho NVL trung tâm -> Kho NVL sản xuất

    private record MaterialStockChange(
            Line line,
            BigDecimal beforeCentral,
            BigDecimal afterCentral,
            BigDecimal beforeProduction,
            BigDecimal afterProduction
    ) {}


    // Dùng cho xuất thành phẩm

    private record StockChange(
            Line line,
            BigDecimal before,
            BigDecimal after
    ) {}


    private record ProductionResult(
            long orderId,
            int quantity
    ) {}


    private record ProductionOrder(
            long productId,
            long bomId,
            int requested,
            int completed
    ) {}

    // =====================================================
// TỪ CHỐI KẾT QUẢ SẢN XUẤT
// =====================================================

    @Transactional
    public void rejectProduction(
            long resultId,
            String rejectionType,
            String reason,
            Integer defectQuantity,
            String managerEmail) {

        // 1. Khóa kết quả, ngăn xử lý lặp
        String status = lockStatus(
                "ketqua_sanxuat",
                "makqsx",
                resultId
        );

        requireStatus(status, "CHO_XAC_NHAN");

        if (reason == null
                || reason.isBlank()
                || reason.trim().length() > 500) {
            throw new IllegalArgumentException(
                    "Lý do từ chối phải có từ 1 đến 500 ký tự."
            );
        }

        boolean rejectWrongRequest =
                "TU_CHOI_SAI_YEU_CAU".equals(rejectionType);

        boolean rejectDefect =
                "TU_CHOI_LOI".equals(rejectionType);

        if (!rejectWrongRequest && !rejectDefect) {
            throw new IllegalArgumentException(
                    "Loại từ chối không hợp lệ."
            );
        }

        // 2. Lấy thông tin kết quả
        ProductionResult result = jdbc.queryForObject(
                """
                SELECT maycsx, soluonghoanthanh
                FROM ketqua_sanxuat
                WHERE makqsx = ?
                """,
                (rs, row) -> new ProductionResult(
                        rs.getLong("maycsx"),
                        rs.getInt("soluonghoanthanh")
                ),
                resultId
        );

        if (result == null || result.quantity() <= 0) {
            throw new IllegalArgumentException(
                    "Không tìm thấy kết quả hoặc số lượng không hợp lệ."
            );
        }

        // 3. Xác định số lượng lỗi và số lượng đạt
        int defect = 0;

        if (rejectDefect) {
            if (defectQuantity == null
                    || defectQuantity < 1
                    || defectQuantity > result.quantity()) {
                throw new IllegalArgumentException(
                        "Số lượng lỗi phải từ 1 đến "
                                + result.quantity() + "."
                );
            }

            defect = defectQuantity;
        }

        int goodQuantity = result.quantity() - defect;

        // 4. Khóa yêu cầu sản xuất
        ProductionOrder order = jdbc.queryForObject(
                """
                SELECT
                    masp,
                    mabom,
                    soluongyeucau,
                    soluonghoanthanh
                FROM yeucausanxuat
                WHERE maycsx = ?
                FOR UPDATE
                """,
                (rs, row) -> new ProductionOrder(
                        rs.getLong("masp"),
                        rs.getLong("mabom"),
                        rs.getInt("soluongyeucau"),
                        rs.getInt("soluonghoanthanh")
                ),
                result.orderId()
        );

        if (order == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy yêu cầu sản xuất."
            );
        }

        Integer recorded = jdbc.queryForObject(
                """
                SELECT COALESCE(SUM(soluong), 0)
                FROM ghi_nhan_san_xuat
                WHERE maycsx = ?
                """,
                Integer.class,
                result.orderId()
        );

        Integer pendingOther = jdbc.queryForObject(
                """
                SELECT COALESCE(SUM(soluonghoanthanh), 0)
                FROM ketqua_sanxuat
                WHERE maycsx = ?
                  AND trangthai = 'CHO_XAC_NHAN'
                  AND makqsx <> ?
                """,
                Integer.class,
                result.orderId(),
                resultId
        );

        int unsubmitted =
                (recorded == null ? 0 : recorded)
                        - (pendingOther == null ? 0 : pendingOther);

        if (result.quantity() > unsubmitted) {
            throw new IllegalStateException(
                    "Số lượng kết quả vượt quá số lượng đã ghi nhận chưa gửi."
            );
        }

        Integer actor = employeeId(managerEmail);

        // =================================================
        // TRƯỜNG HỢP 1: TỪ CHỐI SAI YÊU CẦU
        // Không thay đổi bất kỳ tồn kho nào.
        // =================================================

        if (rejectWrongRequest) {
            consumeRecordedProduction(
                    result.orderId(),
                    result.quantity()
            );

            jdbc.update(
                    """
                    UPDATE ketqua_sanxuat
                    SET
                        trangthai = 'TU_CHOI_SAI_YEU_CAU',
                        soluongloi = 0,
                        lydotuchoi = ?,
                        nguoiduyet = ?,
                        ngayduyet = CURRENT_TIMESTAMP
                    WHERE makqsx = ?
                      AND trangthai = 'CHO_XAC_NHAN'
                    """,
                    reason.trim(),
                    actor,
                    resultId
            );

            return;
        }

        // =================================================
        // TRƯỜNG HỢP 2: TỪ CHỐI DO SẢN PHẨM LỖI
        // Chuyển toàn bộ sản phẩm khỏi kho sản xuất:
        // - Hàng đạt -> kho thành phẩm quản lý
        // - Hàng lỗi -> kho lỗi sản xuất
        // =================================================

        if (order.completed() + goodQuantity > order.requested()) {
            throw new IllegalStateException(
                    "Số lượng thành phẩm đạt vượt quá số lượng còn lại của yêu cầu."
            );
        }

        BigDecimal total = BigDecimal.valueOf(result.quantity());

        // Khóa tồn kho thành phẩm sản xuất
        BigDecimal productionBefore = lockStock(
                "kho_thanhpham_san_xuat",
                "masp",
                order.productId(),
                "mabom",
                order.bomId()
        );

        if (productionBefore.compareTo(total) < 0) {
            throw new IllegalStateException(
                    "Kho thành phẩm sản xuất không đủ số lượng để xử lý kết quả."
            );
        }

        BigDecimal productionAfter = productionBefore.subtract(total);

        // Trừ toàn bộ số lượng khỏi kho thành phẩm sản xuất
        jdbc.update(
                """
                UPDATE kho_thanhpham_san_xuat
                SET soluongton = ?
                WHERE masp = ?
                  AND mabom = ?
                """,
                productionAfter,
                order.productId(),
                order.bomId()
        );

        productionHistory(
                "THANH_PHAM_SAN_XUAT",
                "THANH_PHAM",
                order.productId(),
                "XUAT_THANH_PHAM_SAN_XUAT",
                total,
                productionBefore,
                productionAfter,
                actor,
                "ketqua_sanxuat",
                resultId
        );

        // Cộng hàng đạt vào kho thành phẩm quản lý
        if (goodQuantity > 0) {
            BigDecimal good = BigDecimal.valueOf(goodQuantity);

            BigDecimal centralBefore = lockStock(
                    "sanpham",
                    "masp",
                    order.productId()
            );

            BigDecimal centralAfter = centralBefore.add(good);

            jdbc.update(
                    """
                    UPDATE sanpham
                    SET soluongton = ?
                    WHERE masp = ?
                    """,
                    centralAfter,
                    order.productId()
            );

            history(
                    "NHAP_THANH_PHAM",
                    "THANH_PHAM",
                    new Line(
                            order.productId(),
                            order.bomId(),
                            good,
                            BigDecimal.ZERO
                    ),
                    actor,
                    "ketqua_sanxuat",
                    resultId
            );
        }

        // Cộng hàng lỗi vào kho lỗi sản xuất
        if (defect > 0) {
            jdbc.update(
                    """
                    INSERT INTO kho_loi_san_xuat
                        (masp, mabom, soluongton)
                    VALUES (?, ?, 0)
                    ON DUPLICATE KEY UPDATE
                        masp = VALUES(masp)
                    """,
                    order.productId(),
                    order.bomId()
            );

            BigDecimal defectBefore = lockStock(
                    "kho_loi_san_xuat",
                    "masp",
                    order.productId(),
                    "mabom",
                    order.bomId()
            );

            BigDecimal defectAmount = BigDecimal.valueOf(defect);
            BigDecimal defectAfter = defectBefore.add(defectAmount);

            jdbc.update(
                    """
                    UPDATE kho_loi_san_xuat
                    SET soluongton = ?
                    WHERE masp = ?
                      AND mabom = ?
                    """,
                    defectAfter,
                    order.productId(),
                    order.bomId()
            );


            // Lịch sử chi tiết kho lỗi
            jdbc.update(
                    """
                    INSERT INTO lich_su_kho_loi_san_xuat
                    (
                        makqsx,
                        masp,
                        mabom,
                        soluong,
                        ton_truoc,
                        ton_sau,
                        lydo,
                        nguoi_thuchien
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    resultId,
                    order.productId(),
                    order.bomId(),
                    defect,
                    defectBefore.intValueExact(),
                    defectAfter.intValueExact(),
                    reason.trim(),
                    actor
            );
        }

        // Chỉ tính hàng đạt vào tiến độ hoàn thành yêu cầu
        if (goodQuantity > 0) {
            int completed = order.completed() + goodQuantity;

            jdbc.update(
                    """
                    UPDATE yeucausanxuat
                    SET
                        soluonghoanthanh = ?,
                        trangthai = ?
                    WHERE maycsx = ?
                    """,
                    completed,
                    completed >= order.requested()
                            ? "HOAN_THANH"
                            : "DANG_SAN_XUAT",
                    result.orderId()
            );
        }

        // Đối soát số lượng đã ghi nhận để không tạo kết quả trùng
        consumeRecordedProduction(
                result.orderId(),
                result.quantity()
        );

        // Lưu trạng thái, số lượng lỗi, lý do và người duyệt
        jdbc.update(
                """
                UPDATE ketqua_sanxuat
                SET
                    trangthai = 'TU_CHOI_LOI',
                    soluongloi = ?,
                    lydotuchoi = ?,
                    nguoiduyet = ?,
                    ngayduyet = CURRENT_TIMESTAMP
                WHERE makqsx = ?
                  AND trangthai = 'CHO_XAC_NHAN'
                """,
                defect,
                reason.trim(),
                actor,
                resultId
        );
    }
}