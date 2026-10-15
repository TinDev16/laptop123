package com.example.demo.Service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GhiNhanSanXuatService {

    private final JdbcTemplate jdbc;

    public GhiNhanSanXuatService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    // =====================================================
    // LẤY DANH SÁCH YÊU CẦU SẢN XUẤT
    // =====================================================

    public List<Map<String, Object>> getOrders(String email) {

        return jdbc.queryForList(
                """
                SELECT
                    y.maycsx,
                    CONCAT('YCSX-', y.maycsx) AS maycsxCode,
                    y.masp,
                    y.mabom,
                    d.madot_code,
                    d.tendot,
                    s.tensp AS tenSp,
                    y.soluongyeucau,

                    LEAST(
                        y.soluongyeucau,
                        y.soluonghoanthanh
                        + COALESCE(g.daghi,0)
                    ) AS soluonghoanthanh,

                    y.soluonghoanthanh
                        AS soluonghoanthanh_da_xac_nhan,

                    y.trangthai,
                    y.hanhoanthanh AS ngaydenhan,
                    CASE
                        WHEN y.hanhoanthanh IS NOT NULL
                         AND y.hanhoanthanh < CURDATE()
                        THEN DATEDIFF(CURDATE(), y.hanhoanthanh)
                        ELSE 0
                    END AS overdueDays,
                    b.phienban,

                    COALESCE(g.daghi,0)
                        AS soluong_daghi_nhan,

                    GREATEST(
                        y.soluongyeucau
                        - y.soluonghoanthanh
                        - COALESCE(g.daghi,0),
                        0
                    ) AS soluong_con_lai

                FROM yeucausanxuat y

                JOIN sanpham s
                    ON s.masp = y.masp

                JOIN bom b
                    ON b.mabom = y.mabom

                JOIN taikhoan t
                    ON t.manv = y.nguoiphutrach

                LEFT JOIN dot_san_xuat d
                    ON d.madot = y.madot

                LEFT JOIN
                (
                    SELECT
                        maycsx,
                        SUM(soluong) daghi
                    FROM ghi_nhan_san_xuat
                    GROUP BY maycsx
                ) g
                    ON g.maycsx = y.maycsx

                WHERE t.email = ?
                  AND y.trangthai IN (
                      'DA_GIAO',
                      'DANG_SAN_XUAT'
                  )
                  AND y.soluonghoanthanh
                      + COALESCE(g.daghi,0)
                      < y.soluongyeucau

                ORDER BY y.maycsx DESC
                """,
                email
        );
    }


    // =====================================================
    // CHI TIẾT BOM
    // =====================================================

    public List<Map<String, Object>> getBomDetails(
            long bomId) {

        return jdbc.queryForList(
                """
                SELECT
                    c.manvl,
                    n.manvl_code,
                    n.tennvl,
                    n.donvitinh,
                    c.soluong_dinhmuc,
                    c.tyle_haohut,
                    COALESCE(
                        k.soluongton,
                        0
                    ) AS soluongton

                FROM bom_chitiet c

                JOIN nguyenvatlieu n
                    ON n.manvl = c.manvl

                LEFT JOIN kho_nvl_san_xuat k
                    ON k.manvl = c.manvl

                WHERE c.mabom = ?

                ORDER BY n.tennvl
                """,
                bomId
        );
    }


    // =====================================================
    // GHI NHẬN SẢN XUẤT
    //
    // Kho NVL sản xuất
    //        |
    //        | xuất NVL
    //        v
    //     SẢN XUẤT
    //        |
    //        | hoàn thành
    //        v
    // Kho thành phẩm sản xuất
    // =====================================================

    @Transactional(rollbackFor = Exception.class)
    public long recordProduction(
            String email,
            long orderId,
            long bomId,
            int quantity,
            String note) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Số lượng sản xuất phải lớn hơn 0."
            );
        }

        String normalizedNote = note == null ? null : note.trim();
        if (normalizedNote != null && normalizedNote.length() > 255) {
            throw new IllegalArgumentException("Ghi chú tối đa 255 ký tự.");
        }
        if (normalizedNote != null && normalizedNote.isEmpty()) {
            normalizedNote = null;
        }


        // =====================================================
        // 1. KHÓA YÊU CẦU SẢN XUẤT
        // =====================================================

        Map<String, Object> order =
                jdbc.queryForMap(
                        """
                        SELECT
                            y.maycsx,
                            y.masp,
                            y.mabom,
                            y.soluongyeucau,
                            y.soluonghoanthanh,
                            y.trangthai

                        FROM yeucausanxuat y

                        JOIN taikhoan t
                            ON t.manv = y.nguoiphutrach

                        WHERE y.maycsx = ?
                          AND t.email = ?

                        FOR UPDATE
                        """,
                        orderId,
                        email
                );


        // =====================================================
        // 2. KIỂM TRA TRẠNG THÁI
        // =====================================================

        String status =
                String.valueOf(
                        order.get("trangthai")
                );

        if ("HOAN_THANH".equals(status)) {

            throw new IllegalStateException(
                    "Yêu cầu sản xuất đã hoàn thành."
            );
        }

        if ("HUY".equals(status)) {

            throw new IllegalStateException(
                    "Yêu cầu sản xuất đã bị hủy."
            );
        }


        // =====================================================
        // 3. KIỂM TRA SẢN PHẨM + BOM
        // =====================================================

        long productId =
                ((Number) order.get("masp"))
                        .longValue();

        long orderBomId =
                ((Number) order.get("mabom"))
                        .longValue();

        if (orderBomId != bomId) {

            throw new IllegalArgumentException(
                    "BOM được chọn không thuộc "
                            + "yêu cầu sản xuất này."
            );
        }


        int requested =
                ((Number) order.get("soluongyeucau"))
                        .intValue();

        int confirmed =
                ((Number) order.get("soluonghoanthanh"))
                        .intValue();


        // =====================================================
        // 4. KIỂM TRA SỐ LƯỢNG ĐÃ GHI NHẬN
        // =====================================================

        Integer recorded =
                jdbc.queryForObject(
                        """
                        SELECT COALESCE(
                            SUM(soluong),
                            0
                        )
                        FROM ghi_nhan_san_xuat
                        WHERE maycsx = ?
                        """,
                        Integer.class,
                        orderId
                );

        if (recorded == null) {
            recorded = 0;
        }

        int remaining =
                requested
                        - confirmed
                        - recorded;

        if (remaining <= 0) {

            throw new IllegalStateException(
                    "Yêu cầu không còn số lượng "
                            + "chưa ghi nhận."
            );
        }

        if (quantity > remaining) {

            throw new IllegalArgumentException(
                    "Số lượng ghi nhận ("
                            + quantity
                            + ") vượt quá số lượng "
                            + "còn có thể sản xuất ("
                            + remaining
                            + ")."
            );
        }


        // =====================================================
        // 5. KIỂM TRA BOM
        // =====================================================

        Map<String, Object> bom =
                jdbc.queryForMap(
                        """
                        SELECT
                            masp,
                            trangthai
                        FROM bom
                        WHERE mabom = ?
                        """,
                        bomId
                );

        if (((Number) bom.get("masp"))
                .longValue() != productId) {

            throw new IllegalStateException(
                    "BOM không thuộc sản phẩm "
                            + "của yêu cầu."
            );
        }

        if (!"HIEU_LUC".equals(
                String.valueOf(
                        bom.get("trangthai")
                ))) {

            throw new IllegalStateException(
                    "BOM của yêu cầu sản xuất "
                            + "không còn hiệu lực."
            );
        }


        // =====================================================
        // 6. LẤY CHI TIẾT BOM
        // =====================================================

        List<Map<String, Object>> lines =
                jdbc.queryForList(
                        """
                        SELECT
                            c.manvl,
                            c.soluong_dinhmuc,
                            c.tyle_haohut,
                            n.tennvl

                        FROM bom_chitiet c

                        JOIN nguyenvatlieu n
                            ON n.manvl = c.manvl

                        WHERE c.mabom = ?

                        ORDER BY c.manvl
                        """,
                        bomId
                );

        if (lines.isEmpty()) {

            throw new IllegalStateException(
                    "BOM không có nguyên vật liệu."
            );
        }


        // =====================================================
        // 7. TÍNH + KHÓA TỒN KHO NVL
        // =====================================================

        List<MaterialConsumption> consumptions =
                new ArrayList<>();

        for (Map<String, Object> line : lines) {

            long materialId =
                    ((Number) line.get("manvl"))
                            .longValue();

            BigDecimal base =
                    decimal(
                            line.get(
                                    "soluong_dinhmuc"
                            )
                    );

            BigDecimal waste =
                    decimal(
                            line.get(
                                    "tyle_haohut"
                            )
                    );

            BigDecimal required =
                    BigDecimal.valueOf(quantity)
                            .multiply(base)
                            .multiply(
                                    BigDecimal.ONE.add(
                                            waste.divide(
                                                    BigDecimal.valueOf(100),
                                                    10,
                                                    RoundingMode.HALF_UP
                                            )
                                    )
                            )
                            .setScale(
                                    0,
                                    RoundingMode.CEILING
                            );


            // -------------------------------------------------
            // KHÓA TỒN KHO NVL SẢN XUẤT
            // -------------------------------------------------

            Number stockNumber =
                    jdbc.queryForObject(
                            """
                            SELECT COALESCE(
                                soluongton,
                                0
                            )
                            FROM kho_nvl_san_xuat
                            WHERE manvl = ?
                            FOR UPDATE
                            """,
                            Number.class,
                            materialId
                    );

            BigDecimal stock =
                    stockNumber == null
                            ? BigDecimal.ZERO
                            : decimal(stockNumber);

            if (stock.compareTo(required) < 0) {

                throw new IllegalStateException(
                        "NVL \""
                                + line.get("tennvl")
                                + "\" không đủ tồn kho sản xuất. "
                                + "Cần: "
                                + required
                                .stripTrailingZeros()
                                .toPlainString()
                                + ", tồn: "
                                + stock
                                .stripTrailingZeros()
                                .toPlainString()
                );
            }


            BigDecimal after =
                    stock.subtract(required);

            consumptions.add(
                    new MaterialConsumption(
                            materialId,
                            required,
                            stock,
                            after
                    )
            );
        }


        // =====================================================
        // 8. TRỪ NVL SẢN XUẤT
        // =====================================================

        for (MaterialConsumption consumption
                : consumptions) {

            jdbc.update(
                    """
                    UPDATE kho_nvl_san_xuat
                    SET soluongton = ?
                    WHERE manvl = ?
                      AND soluongton >= ?
                    """,
                    consumption.after(),
                    consumption.materialId(),
                    consumption.quantity()
            );
        }


        // =====================================================
        // 9. CỘNG THÀNH PHẨM VÀO KHO SẢN XUẤT
        //
        // Đầu tiên tạo row nếu chưa tồn tại.
        // Sau đó mới tăng tồn.
        // =====================================================

        jdbc.update(
                """
                INSERT INTO kho_thanhpham_san_xuat
                (
                    masp,
                    mabom,
                    soluongton
                )
                VALUES (?, ?, 0)

                ON DUPLICATE KEY UPDATE
                    soluongton = soluongton
                """,
                productId,
                bomId
        );


        // -----------------------------------------------------
        // Khóa tồn thành phẩm sản xuất
        // -----------------------------------------------------

        Number fgBeforeNumber =
                jdbc.queryForObject(
                        """
                        SELECT COALESCE(
                            soluongton,
                            0
                        )
                        FROM kho_thanhpham_san_xuat
                        WHERE masp = ?
                          AND mabom = ?
                        FOR UPDATE
                        """,
                        Number.class,
                        productId,
                        bomId
                );

        BigDecimal fgBefore =
                fgBeforeNumber == null
                        ? BigDecimal.ZERO
                        : decimal(fgBeforeNumber);

        BigDecimal fgAfter =
                fgBefore.add(
                        BigDecimal.valueOf(quantity)
                );


        // -----------------------------------------------------
        // Cộng kho thành phẩm sản xuất
        // -----------------------------------------------------

        jdbc.update(
                """
                UPDATE kho_thanhpham_san_xuat
                SET soluongton = ?
                WHERE masp = ?
                  AND mabom = ?
                """,
                fgAfter,
                productId,
                bomId
        );


        // =====================================================
        // 10. GHI NHẬN SẢN XUẤT
        //
        // Lấy ID để dùng làm mã tham chiếu lịch sử.
        // =====================================================

        jdbc.update(
                """
                INSERT INTO ghi_nhan_san_xuat
                (
                    maycsx,
                    masp,
                    mabom,
                    soluong,
                    nguoighi,
                    ngayghi
                )
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                orderId,
                productId,
                bomId,
                quantity,
                employeeId(email)
        );

        Long recordId =
                jdbc.queryForObject(
                        "SELECT LAST_INSERT_ID()",
                        Long.class
                );

        if (recordId == null) {

            throw new IllegalStateException(
                    "Không lấy được mã ghi nhận sản xuất."
            );
        }


        // =====================================================
        // 11. GHI LỊCH SỬ:
        //
        // NVL SẢN XUẤT
        //     XUAT_NVL_SAN_XUAT
        // =====================================================

        Integer actor =
                employeeId(email);

        for (MaterialConsumption consumption
                : consumptions) {

            productionHistory(
                    "NVL_SAN_XUAT",
                    "NVL",
                    consumption.materialId(),
                    "XUAT_NVL_SAN_XUAT",
                    consumption.quantity(),
                    consumption.before(),
                    consumption.after(),
                    actor,
                    "ghi_nhan_san_xuat",
                    recordId
            );
        }


        // =====================================================
        // 12. GHI LỊCH SỬ:
        //
        // THÀNH PHẨM SẢN XUẤT
        //     NHAP_THANH_PHAM_SAN_XUAT
        // =====================================================

        productionHistory(
                "THANH_PHAM_SAN_XUAT",
                "THANH_PHAM",
                productId,
                "NHAP_THANH_PHAM_SAN_XUAT",
                BigDecimal.valueOf(quantity),
                fgBefore,
                fgAfter,
                actor,
                "ghi_nhan_san_xuat",
                recordId
        );

        // Tạo kết quả chờ quản lý xác nhận ngay trong cùng giao dịch.
        // Nhân viên chỉ nhập sản lượng một lần tại trang ghi nhận.
        jdbc.update(
                """
                INSERT INTO ketqua_sanxuat
                    (maycsx, nguoithuchien, soluonghoanthanh, soluongloi, trangthai, ghichu)
                VALUES (?, ?, ?, 0, 'CHO_XAC_NHAN', ?)
                """,
                orderId,
                actor,
                quantity,
                normalizedNote
        );

        Long resultId = jdbc.queryForObject(
                "SELECT LAST_INSERT_ID()",
                Long.class
        );

        if (resultId == null) {
            throw new IllegalStateException("Không tạo được kết quả sản xuất chờ xác nhận.");
        }

        return resultId;
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
    // LẤY MÃ NHÂN VIÊN
    // =====================================================

    private int employeeId(String email) {

        List<Integer> ids =
                jdbc.query(
                        """
                        SELECT manv
                        FROM taikhoan
                        WHERE email = ?
                          AND manv IS NOT NULL
                        """,
                        (rs, row) ->
                                rs.getInt(1),
                        email
                );

        if (ids.isEmpty()) {

            throw new IllegalArgumentException(
                    "Không tìm thấy nhân viên đang đăng nhập."
            );
        }

        return ids.get(0);
    }


    // =====================================================
    // CHUYỂN OBJECT -> BIGDECIMAL
    // =====================================================

    private BigDecimal decimal(Object value) {

        return value == null
                ? BigDecimal.ZERO
                : new BigDecimal(
                value.toString()
        );
    }


    // =====================================================
    // RECORD: NVL TIÊU HAO
    // =====================================================

    private record MaterialConsumption(
            long materialId,
            BigDecimal quantity,
            BigDecimal before,
            BigDecimal after
    ) {}
}
