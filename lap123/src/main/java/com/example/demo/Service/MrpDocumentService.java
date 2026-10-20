package com.example.demo.Service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashMap;

@Service
public class MrpDocumentService {
    private final JdbcTemplate jdbc;

    public MrpDocumentService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int employeeId(String email) {
        List<Integer> ids = jdbc.query("SELECT manv FROM taikhoan WHERE email = ? AND manv IS NOT NULL",
                (rs, row) -> rs.getInt(1), email);
        if (ids.isEmpty()) throw new IllegalStateException("Tài khoản chưa liên kết với nhân viên.");
        return ids.get(0);
    }

    public List<Map<String, Object>> suppliers() {
        return jdbc.queryForList(
                """
                SELECT mancc, tenncc
                FROM ncc
                WHERE trangthai = 0
                ORDER BY tenncc
                """
        );
    }

    public List<Map<String, Object>> materials() {
        return jdbc.queryForList(
                """
                SELECT
                    n.manvl,
                    n.manvl_code,
                    n.tennvl,
                    n.donvitinh,
                    n.dongianhap,
                    n.soluongton,
                    n.ton_toithieu,
                    l.maloai,
                    l.tenloai
                FROM nguyenvatlieu n
                JOIN loai_nguyen_vat_lieu l
                    ON l.maloai = n.maloai
                WHERE n.trangthai = 1
                  AND l.trangthai = 1
                ORDER BY l.tenloai, n.tennvl
                """
        );
    }

    public List<Map<String, Object>> products() {
        return jdbc.queryForList("SELECT masp, tensp, COALESCE(soluongton, 0) soluongton FROM sanpham ORDER BY tensp");
    }

    public List<Map<String, Object>> receipts() {
        return jdbc.queryForList("SELECT p.mapn AS maphieu, n.tenncc, v.hoten, p.ngaylap AS ngaytao, p.trangthai, p.ghichu, p.tongtien " +
                "FROM phieunhap_nvl p JOIN ncc n ON n.mancc = p.mancc JOIN nhanvien v ON v.manv = p.nguoilap ORDER BY p.mapn DESC");
    }

    public Map<String, Object> receipt(long id) {
        return one("SELECT p.*, p.mapn AS maphieu, n.tenncc, v.hoten FROM phieunhap_nvl p JOIN ncc n ON n.mancc = p.mancc " +
                "JOIN nhanvien v ON v.manv = p.nguoilap WHERE p.mapn = ?", id);
    }

    public List<Map<String, Object>> receiptLines(long id) {
        return jdbc.queryForList(
                """
                SELECT
                    c.*,
                    n.manvl_code,
                    n.tennvl,
                    n.donvitinh,
                    l.tenloai
                FROM ct_phieunhap_nvl c
                JOIN nguyenvatlieu n
                    ON n.manvl = c.manvl
                LEFT JOIN loai_nguyen_vat_lieu l
                    ON l.maloai = n.maloai
                WHERE c.mapn = ?
                ORDER BY c.id
                """,
                id
        );
    }

    @Transactional
    public long createReceipt(String email, int supplierId, String note, List<Long> materialIds,
                              List<Integer> quantities, List<BigDecimal> prices) {
        validateActiveSupplier(supplierId);
        validateLines(materialIds, quantities);
        validateActiveMaterials(materialIds);
        validateNote(note);
        if (prices == null || prices.size() != materialIds.size() || prices.stream().anyMatch(p -> p == null || p.signum() < 0)) {
            throw new IllegalArgumentException("Đơn giá nhập không hợp lệ.");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < prices.size(); i++) {
            total = total.add(prices.get(i).multiply(BigDecimal.valueOf(quantities.get(i))));
        }
        long id = insert("INSERT INTO phieunhap_nvl (mancc, nguoilap, trangthai, ghichu, tongtien) VALUES (?, ?, 'NHAP_NHAP', ?, ?)",
                supplierId, employeeId(email), note, total);
        for (int i = 0; i < materialIds.size(); i++) {
            jdbc.update("INSERT INTO ct_phieunhap_nvl (mapn, manvl, soluong, dongia) VALUES (?, ?, ?, ?)",
                    id, materialIds.get(i), quantities.get(i), prices.get(i));
        }
        return id;
    }

    @Transactional
    public void deleteDraftReceipt(long receiptId) {
        List<String> statuses = jdbc.queryForList(
                "SELECT trangthai FROM phieunhap_nvl WHERE mapn = ? FOR UPDATE",
                String.class, receiptId);
        if (statuses.isEmpty()) {
            throw new IllegalArgumentException("Phiếu nhập không tồn tại.");
        }
        if (!"NHAP_NHAP".equals(statuses.get(0))) {
            throw new IllegalStateException("Chỉ có thể xóa phiếu nhập ở trạng thái Bản nháp.");
        }

        jdbc.update("DELETE FROM ct_phieunhap_nvl WHERE mapn = ?", receiptId);
        int deleted = jdbc.update(
                "DELETE FROM phieunhap_nvl WHERE mapn = ? AND trangthai = 'NHAP_NHAP'",
                receiptId);
        if (deleted != 1) {
            throw new IllegalStateException("Không thể xóa phiếu nhập này.");
        }
    }

    public List<Map<String, Object>> materialIssues(String email, boolean manager) {
        String sql = """
        SELECT
            p.mapx AS maphieu,
            p.maycsx AS mayeucau,
            p.ngaylap AS ngaytao,
            p.trangthai,
            p.ghichu,
            n.hoten,
            s.tensp,
            d.madot_code AS madot_code,
            d.tendot AS tendot,
            CASE
                WHEN EXISTS (
                    SELECT 1
                    FROM phieuxuat_nvl_ycsx py
                    WHERE py.mapx = p.mapx
                )
                THEN (
                    SELECT COUNT(*)
                    FROM phieuxuat_nvl_ycsx py
                    WHERE py.mapx = p.mapx
                )
                ELSE 1
            END AS soyeucau
        FROM phieuxuat_nvl p
        JOIN nhanvien n
            ON n.manv = p.nguoilap
        JOIN yeucausanxuat y
            ON y.maycsx = p.maycsx
        JOIN sanpham s
            ON s.masp = y.masp
        LEFT JOIN dot_san_xuat d
            ON d.madot = y.madot
        """;

        if (manager) {
            return jdbc.queryForList(sql + " ORDER BY p.mapx DESC");
        }

        return jdbc.queryForList(
                sql + """
            WHERE EXISTS (
                SELECT 1
                FROM taikhoan t
                WHERE t.email = ?
                  AND (
                      t.manv = p.nguoilap
                      OR t.manv = y.nguoiphutrach
                  )
            )
            ORDER BY p.mapx DESC
            """,
                email
        );
    }

    public Map<String, Object> materialIssue(long id, String email, boolean manager) {
        String sql = "SELECT p.*, p.mapx AS maphieu, p.maycsx AS mayeucau, n.hoten, s.tensp, " +
                "b.mabom, b.phienban, CONCAT(s.tensp, ' / BOM ', b.phienban) AS tenbom, " +
                "d.madot_code, d.tendot FROM phieuxuat_nvl p " +
                "JOIN nhanvien n ON n.manv = p.nguoilap JOIN yeucausanxuat y ON y.maycsx = p.maycsx " +
                "JOIN sanpham s ON s.masp = y.masp JOIN bom b ON b.mabom = y.mabom " +
                "LEFT JOIN dot_san_xuat d ON d.madot = y.madot ";
        return manager ? one(sql + "WHERE p.mapx = ?", id) :
                ownedOne(sql + "WHERE p.mapx = ? AND EXISTS (SELECT 1 FROM taikhoan t WHERE t.email = ? AND (t.manv = p.nguoilap OR t.manv = y.nguoiphutrach))", id, email);
    }

    public List<Map<String, Object>> materialIssueRequests(long issueId) {
        return jdbc.queryForList("""
        SELECT
            y.maycsx AS mayeucau,
            s.tensp,
            y.masp,
            y.mabom,
            b.phienban,
            y.soluongyeucau,
            y.soluonghoanthanh,
            y.trangthai AS trangthai_yeucau,
            d.madot_code,
            d.tendot
        FROM phieuxuat_nvl p
        LEFT JOIN phieuxuat_nvl_ycsx py
            ON py.mapx = p.mapx
        JOIN yeucausanxuat y
            ON y.maycsx = COALESCE(py.maycsx, p.maycsx)
        JOIN sanpham s
            ON s.masp = y.masp
        JOIN bom b
            ON b.mabom = y.mabom
        LEFT JOIN dot_san_xuat d
            ON d.madot = y.madot
        WHERE p.mapx = ?
        ORDER BY y.maycsx
        """, issueId);
    }

    public List<Map<String, Object>> materialIssueLines(long id) {
        return jdbc.queryForList(
                """
                SELECT
                    c.*,
                    n.manvl_code,
                    n.tennvl,
                    n.donvitinh,
                    l.tenloai
                FROM ct_phieuxuat_nvl c
                JOIN nguyenvatlieu n
                    ON n.manvl = c.manvl
                LEFT JOIN loai_nguyen_vat_lieu l
                    ON l.maloai = n.maloai
                WHERE c.mapx = ?
                ORDER BY c.id
                """,
                id
        );
    }

    public List<Map<String, Object>> assignedOrders(String email) {
        return jdbc.queryForList(
                """
                SELECT
                    y.maycsx AS mayeucau,
                    y.masp,
                    y.mabom,
                    d.madot_code,
                    d.tendot,
                    s.tensp,
                    b.phienban,
                    y.soluongyeucau,
                    LEAST(
                        y.soluongyeucau,
                        GREATEST(
                            COALESCE(g.daghi, 0),
                            y.soluonghoanthanh + COALESCE(p.dagui, 0)
                        )
                    ) AS soluonghoanthanh,
                    y.hanhoanthanh AS ngaydenhan,
                    CASE
                        WHEN y.trangthai NOT IN ('HOAN_THANH', 'HUY')
                         AND y.soluonghoanthanh < y.soluongyeucau
                         AND y.hanhoanthanh IS NOT NULL
                         AND y.hanhoanthanh < CURDATE()
                        THEN DATEDIFF(CURDATE(), y.hanhoanthanh)
                        ELSE 0
                    END AS overdueDays,
                    COALESCE(g.daghi, 0) AS soluong_daghi_nhan,
                    COALESCE(p.dagui, 0) AS soluong_da_gui,
                    GREATEST(
                        COALESCE(g.daghi, 0) - COALESCE(p.dagui, 0),
                        0
                    ) AS soluong_co_the_gui,
                    GREATEST(
                        y.soluongyeucau - GREATEST(
                            COALESCE(g.daghi, 0),
                            y.soluonghoanthanh + COALESCE(p.dagui, 0)
                        ),
                        0
                    ) AS soluong_con_lai
                FROM yeucausanxuat y
                JOIN sanpham s ON s.masp = y.masp
                JOIN bom b ON b.mabom = y.mabom
                JOIN taikhoan t ON t.manv = y.nguoiphutrach
                LEFT JOIN dot_san_xuat d ON d.madot = y.madot
                LEFT JOIN (
                    SELECT maycsx, SUM(soluong) AS daghi
                    FROM ghi_nhan_san_xuat
                    GROUP BY maycsx
                ) g ON g.maycsx = y.maycsx
                LEFT JOIN (
                    SELECT maycsx, SUM(soluonghoanthanh) AS dagui
                    FROM ketqua_sanxuat
                    WHERE trangthai = 'CHO_XAC_NHAN'
                    GROUP BY maycsx
                ) p ON p.maycsx = y.maycsx
                WHERE t.email = ?
                  AND y.trangthai <> 'HUY'
                  AND (
                      GREATEST(
                          COALESCE(g.daghi, 0),
                          y.soluonghoanthanh + COALESCE(p.dagui, 0)
                      ) < y.soluongyeucau
                      OR COALESCE(g.daghi, 0) > COALESCE(p.dagui, 0)
                  )
                ORDER BY y.maycsx DESC
                """,
                email
        );
    }

    public List<Map<String, Object>> customers() {
        return jdbc.queryForList(
                """
                SELECT
                    makh,
                    hoten,
                    sdt,
                    email,
                    diachi
                FROM khachhang
                WHERE trangthai = 0
                ORDER BY hoten
                """
        );
    }

    public Map<String, Object> finishedBomPrice(int bomId) {

        List<Map<String, Object>> rows = jdbc.queryForList(
                """
                SELECT
                    b.mabom,
                    b.masp,
                    b.phienban,
                    b.trangthai,
                    COALESCE(
                        SUM(
                            c.soluong_dinhmuc
                            * (1 + c.tyle_haohut / 100)
                            * n.dongianhap
                        ),
                        0
                    ) AS tong_gia_bom
                FROM bom b
                LEFT JOIN bom_chitiet c
                    ON c.mabom = b.mabom
                LEFT JOIN nguyenvatlieu n
                    ON n.manvl = c.manvl
                WHERE b.mabom = ?
                GROUP BY
                    b.mabom,
                    b.masp,
                    b.phienban,
                    b.trangthai
                """,
                bomId
        );

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không tìm thấy BOM."
            );
        }

        Map<String, Object> result = rows.get(0);

        if (!"HIEU_LUC".equals(
                String.valueOf(result.get("trangthai")))) {

            throw new IllegalArgumentException(
                    "BOM đã ngừng sử dụng."
            );
        }

        return result;
    }

    public List<Map<String, Object>> bomRequestLines(long orderId, String email) {
        orderOwned(orderId, email);
        return jdbc.queryForList(
                """
                    SELECT
                        c.manvl,
                        n.manvl_code,
                        n.tennvl,
                        n.donvitinh,
                        COALESCE(k.soluongton, 0) AS soluongton,
                        COALESCE(pending.soluong, 0) AS dang_de_nghi,
                        n.trangthai AS nvl_trangthai,
                        l.trangthai AS loai_trangthai,
                        c.soluong_dinhmuc,
                        c.tyle_haohut,
                    
                        CEIL(
                            GREATEST(
                                y.soluongyeucau
                                - GREATEST(
                                    COALESCE(g.daghi, 0),
                                    y.soluonghoanthanh + COALESCE(r.dangcho, 0)
                                ),
                                0
                            )
                            * c.soluong_dinhmuc
                            * (1 + c.tyle_haohut / 100)
                        ) AS nhu_cau,
                    
                        GREATEST(
                            CEIL(
                                GREATEST(
                                    y.soluongyeucau
                                    - GREATEST(
                                        COALESCE(g.daghi, 0),
                                        y.soluonghoanthanh + COALESCE(r.dangcho, 0)
                                    ),
                                    0
                                )
                                * c.soluong_dinhmuc
                                * (1 + c.tyle_haohut / 100)
                            )
                                - COALESCE(k.soluongton, 0)
                                - COALESCE(pending.soluong, 0),
                            0
                        ) AS thieu_hut
                FROM yeucausanxuat y
                JOIN bom_chitiet c ON c.mabom = y.mabom
                JOIN nguyenvatlieu n ON n.manvl = c.manvl
                JOIN loai_nguyen_vat_lieu l ON l.maloai = n.maloai
                LEFT JOIN kho_nvl_san_xuat k ON k.manvl = n.manvl
                LEFT JOIN (
                    SELECT maycsx, SUM(soluong) AS daghi
                    FROM ghi_nhan_san_xuat
                    GROUP BY maycsx
                ) g ON g.maycsx = y.maycsx
                LEFT JOIN (
                    SELECT maycsx, SUM(soluonghoanthanh) AS dangcho
                    FROM ketqua_sanxuat
                    WHERE trangthai = 'CHO_XAC_NHAN'
                    GROUP BY maycsx
                ) r ON r.maycsx = y.maycsx
                LEFT JOIN (
                    SELECT p.maycsx, c.manvl, SUM(c.soluong) AS soluong
                    FROM phieuxuat_nvl p
                    JOIN ct_phieuxuat_nvl c ON c.mapx = p.mapx
                    WHERE p.trangthai IN ('CHO_DUYET', 'DA_DUYET')
                    GROUP BY p.maycsx, c.manvl
                ) pending ON pending.maycsx = y.maycsx
                         AND pending.manvl = n.manvl
                WHERE y.maycsx = ?
                ORDER BY n.tennvl
                """,
                orderId
        );
    }

    @Transactional
    public long createMaterialIssue(
            String email,
            long orderId,
            String note,
            List<Long> materialIds,
            List<Integer> quantities) {

        // =====================================================
        // KIỂM TRA INPUT GỐC
        // =====================================================

        if (materialIds == null
                || quantities == null
                || materialIds.size() != quantities.size()
                || materialIds.isEmpty()) {

            throw new IllegalArgumentException(
                    "Phiếu phải có ít nhất một dòng chi tiết."
            );
        }

        validateNote(note);

        Map<String, Object> order =
                orderOwnedForUpdate(orderId, email);

        if ("HOAN_THANH".equals(order.get("trangthai"))
                || "HUY".equals(order.get("trangthai"))) {

            throw new IllegalArgumentException(
                    "Yêu cầu sản xuất đã kết thúc."
            );
        }

        // =====================================================
        // LỌC CÁC DÒNG CÓ SỐ LƯỢNG = 0
        //
        // 0 nghĩa là:
        // Tồn hiện tại >= nhu cầu
        // => Không cần xuất
        // => Không tạo chi tiết phiếu
        // =====================================================

        List<Long> validMaterialIds =
                new java.util.ArrayList<>();

        List<Integer> validQuantities =
                new java.util.ArrayList<>();

        for (int i = 0; i < materialIds.size(); i++) {

            Long materialId = materialIds.get(i);
            Integer quantity = quantities.get(i);

            if (materialId == null) {
                throw new IllegalArgumentException(
                        "Nguyên vật liệu không hợp lệ."
                );
            }

            if (quantity == null) {
                throw new IllegalArgumentException(
                        "Số lượng đề nghị không hợp lệ."
                );
            }

            // -------------------------------------------------
            // SL = 0 -> bỏ qua dòng
            // -------------------------------------------------

            if (quantity == 0) {
                continue;
            }

            // -------------------------------------------------
            // SL âm -> lỗi
            // -------------------------------------------------

            if (quantity < 0) {
                throw new IllegalArgumentException(
                        "Số lượng đề nghị không được âm."
                );
            }

            validMaterialIds.add(materialId);
            validQuantities.add(quantity);
        }

        // =====================================================
        // SAU KHI BỎ CÁC DÒNG = 0
        // NẾU KHÔNG CÒN NVL NÀO -> KHÔNG CẦN LẬP PHIẾU
        // =====================================================

        if (validMaterialIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không có nguyên vật liệu nào cần xuất."
            );
        }

        // =====================================================
        // KIỂM TRA NVL TRÙNG + SỐ LƯỢNG
        // Chỉ validate những dòng thực sự được tạo phiếu
        // =====================================================

        validateLines(
                validMaterialIds,
                validQuantities
        );

        // =====================================================
        // KIỂM TRA NVL CÓ THUỘC BOM KHÔNG
        // =====================================================

        Set<Long> allowed = new HashSet<>();

        for (Map<String, Object> line :
                bomRequestLines(orderId, email)) {

            allowed.add(
                    ((Number) line.get("manvl"))
                            .longValue()
            );
        }

        if (!allowed.containsAll(validMaterialIds)) {

            throw new IllegalArgumentException(
                    "NVL đề nghị không thuộc BOM của yêu cầu."
            );
        }

        // =====================================================
        // KIỂM TRA NVL + LOẠI NVL ĐANG HOẠT ĐỘNG
        // =====================================================

        validateActiveMaterials(
                validMaterialIds
        );

        // =====================================================
        // LẤY NHU CẦU THIẾU HỤT HIỆN TẠI
        // =====================================================

        Map<Long, Integer> maxByMaterial =
                new java.util.HashMap<>();

        for (Map<String, Object> line :
                bomRequestLines(orderId, email)) {

            maxByMaterial.put(
                    ((Number) line.get("manvl"))
                            .longValue(),
                    ((Number) line.get("thieu_hut"))
                            .intValue()
            );
        }

        // =====================================================
        // KIỂM TRA SỐ LƯỢNG ĐỀ NGHỊ
        // =====================================================

        for (int i = 0;
             i < validMaterialIds.size();
             i++) {

            long materialId =
                    validMaterialIds.get(i);

            int quantity =
                    validQuantities.get(i);

            int max =
                    maxByMaterial.getOrDefault(
                            materialId,
                            0
                    );

            if (max <= 0) {
                throw new IllegalArgumentException(
                        "NVL này đã đủ hoặc đã có đề nghị xuất đang chờ duyệt."
                );
            }

            if (quantity > max) {
                throw new IllegalArgumentException(
                        "Số lượng đề nghị vượt quá phần NVL còn thiếu (tối đa "
                                + max
                                + ")."
                );
            }
        }

        // =====================================================
        // TẠO PHIẾU
        // =====================================================

        long id = insert(
                """
                INSERT INTO phieuxuat_nvl
                    (
                        maycsx,
                        nguoilap,
                        trangthai,
                        ghichu
                    )
                VALUES
                    (
                        ?,
                        ?,
                        'CHO_DUYET',
                        ?
                    )
                """,
                orderId,
                employeeId(email),
                note
        );

        // =====================================================
        // TẠO CHI TIẾT PHIẾU
        // Chỉ insert quantity > 0
        // =====================================================

        for (int i = 0;
             i < validMaterialIds.size();
             i++) {

            jdbc.update(
                    """
                    INSERT INTO ct_phieuxuat_nvl
                        (
                            mapx,
                            manvl,
                            soluong
                        )
                    VALUES
                        (?, ?, ?)
                    """,
                    id,
                    validMaterialIds.get(i),
                    validQuantities.get(i)
            );
        }

        return id;
    }

    public List<Map<String, Object>> results(String email, boolean manager) {

        String sql =
                "SELECT " +
                        "k.*, " +
                        "k.makqsx AS maketqua, " +
                        "k.maycsx AS mayeucau, " +
                        "y.mabom, " +
                        "s.tensp, " +
                        "b.phienban, " +
                        "d.madot_code, d.tendot, " +
                        "n.hoten " +
                        "FROM ketqua_sanxuat k " +
                        "JOIN yeucausanxuat y " +
                        "    ON y.maycsx = k.maycsx " +
                        "JOIN sanpham s " +
                        "    ON s.masp = y.masp " +
                        "JOIN bom b " +
                        "    ON b.mabom = y.mabom " +
                        "LEFT JOIN dot_san_xuat d " +
                        "    ON d.madot = y.madot " +
                        "JOIN nhanvien n " +
                        "    ON n.manv = k.nguoithuchien ";

        return manager
                ? jdbc.queryForList(
                sql +
                "ORDER BY k.makqsx DESC"
        )
                : jdbc.queryForList(
                sql +
                "JOIN taikhoan t " +
                "    ON t.manv = k.nguoithuchien " +
                "WHERE t.email = ? " +
                "ORDER BY k.makqsx DESC",
                email
        );
    }

    public Map<String, Object> result(
            long id,
            String email,
            boolean manager) {

        String sql =
                "SELECT " +
                        "k.*, " +
                        "k.makqsx AS maketqua, " +
                        "k.maycsx AS mayeucau, " +
                        "y.mabom, " +
                        "s.tensp, " +
                        "b.phienban, " +
                        "d.madot_code, d.tendot, " +
                        "n.hoten " +
                        "FROM ketqua_sanxuat k " +
                        "JOIN yeucausanxuat y " +
                        "    ON y.maycsx = k.maycsx " +
                        "JOIN sanpham s " +
                        "    ON s.masp = y.masp " +
                        "JOIN bom b " +
                        "    ON b.mabom = y.mabom " +
                        "LEFT JOIN dot_san_xuat d " +
                        "    ON d.madot = y.madot " +
                        "JOIN nhanvien n " +
                        "    ON n.manv = k.nguoithuchien ";

        return manager
                ? one(
                sql +
                "WHERE k.makqsx = ?",
                id
        )
                : ownedOne(
                sql +
                "JOIN taikhoan t " +
                "    ON t.manv = k.nguoithuchien " +
                "WHERE k.makqsx = ? " +
                "AND t.email = ?",
                id,
                email
        );
    }

    @Transactional
    public long createResult(String email, long orderId, int completed, int defective, String note) {
        if (completed <= 0) throw new IllegalArgumentException("Số lượng gửi kết quả phải lớn hơn 0.");
        if (defective < 0) throw new IllegalArgumentException("Số lượng lỗi không được âm.");
        validateNote(note);

        Map<String, Object> order = orderOwnedForUpdate(orderId, email);
        String status = String.valueOf(order.get("trangthai"));
        if ("HUY".equals(status)) throw new IllegalArgumentException("Yêu cầu sản xuất đã bị hủy.");
        if ("HOAN_THANH".equals(status)) throw new IllegalArgumentException("Yêu cầu sản xuất đã hoàn thành.");

        int requested = ((Number) order.get("soluongyeucau")).intValue();
        int confirmed = ((Number) order.get("soluonghoanthanh")).intValue();

        Integer recorded = jdbc.queryForObject(
                "SELECT COALESCE(SUM(soluong),0) FROM ghi_nhan_san_xuat WHERE maycsx = ?",
                Integer.class, orderId);
        if (recorded == null) recorded = 0;

        Integer pending = jdbc.queryForObject(
                "SELECT COALESCE(SUM(soluonghoanthanh),0) FROM ketqua_sanxuat WHERE maycsx = ? AND trangthai = 'CHO_XAC_NHAN'",
                Integer.class, orderId);
        if (pending == null) pending = 0;

        int availableRecorded = recorded - confirmed - pending;
        int remainingOrder = requested - confirmed - pending;
        if (availableRecorded <= 0) {
            throw new IllegalStateException("Chưa có thành phẩm chưa gửi kết quả cho yêu cầu này.");
        }
        if (completed > availableRecorded) {
            throw new IllegalArgumentException("Số lượng gửi kết quả (" + completed + ") vượt quá số lượng đã ghi nhận chưa gửi (" + availableRecorded + ").");
        }
        if (completed > remainingOrder) {
            throw new IllegalArgumentException("Số lượng gửi kết quả vượt quá số lượng còn lại của YCSX (" + Math.max(remainingOrder,0) + ").");
        }

        long productId = ((Number) order.get("masp")).longValue();
        long bomId = ((Number) order.get("mabom")).longValue();

        Number stockNumber = jdbc.queryForObject(
                "SELECT COALESCE(soluongton,0) FROM kho_thanhpham_san_xuat WHERE masp = ? AND mabom = ? FOR UPDATE",
                Number.class, productId, bomId);
        int stock = stockNumber == null ? 0 : stockNumber.intValue();
        if (completed > stock) {
            throw new IllegalStateException("Kho thành phẩm sản xuất hiện chỉ còn " + stock + " sản phẩm khả dụng.");
        }

        return insert(
                """
                INSERT INTO ketqua_sanxuat
                (maycsx, nguoithuchien, soluonghoanthanh, soluongloi, trangthai, ghichu)
                VALUES (?, ?, ?, ?, 'CHO_XAC_NHAN', ?)
                """,
                orderId, employeeId(email), completed, defective, note
        );
    }

    public List<Map<String, Object>> finishedIssues(
            String email,
            boolean manager) {

        String sql =
                "SELECT p.*, " +
                        "p.mapxtp AS maphieu, " +
                        "p.ngaylap AS ngaytao, " +
                        "n.hoten, " +
                        "GROUP_CONCAT(s.tensp ORDER BY c.id SEPARATOR ', ') AS tensp, " +
                        "SUM(c.soluong) AS soluong, " +
                        "COUNT(c.id) AS sodong " +
                        "FROM phieuxuat_thanhpham p " +
                        "JOIN nhanvien n ON n.manv = p.nguoilap " +
                        "JOIN ct_phieuxuat_thanhpham c ON c.mapxtp = p.mapxtp " +
                        "JOIN sanpham s ON s.masp = c.masp ";

        return manager
                ? jdbc.queryForList(sql + "GROUP BY p.mapxtp, n.hoten ORDER BY p.mapxtp DESC")
                : jdbc.queryForList(
                sql +
                "JOIN taikhoan t ON t.manv = p.nguoilap " +
                "WHERE t.email = ? " +
                        "GROUP BY p.mapxtp, n.hoten ORDER BY p.mapxtp DESC",
                email
        );
    }

    public Map<String, Object> finishedIssue(
            long id,
            String email,
            boolean manager) {

        String sql =
                "SELECT p.*, " +
                        "p.mapxtp AS maphieu, " +
                        "n.hoten, " +
                        "(SELECT GROUP_CONCAT(s.tensp ORDER BY c.id SEPARATOR ', ') FROM ct_phieuxuat_thanhpham c JOIN sanpham s ON s.masp = c.masp WHERE c.mapxtp = p.mapxtp) AS tensp, " +
                        "(SELECT GROUP_CONCAT(b.phienban ORDER BY c.id SEPARATOR ', ') FROM ct_phieuxuat_thanhpham c JOIN bom b ON b.mabom = c.mabom WHERE c.mapxtp = p.mapxtp) AS phienban, " +
                        "(SELECT GROUP_CONCAT(c.mabom ORDER BY c.id SEPARATOR ', ') FROM ct_phieuxuat_thanhpham c WHERE c.mapxtp = p.mapxtp) AS mabom, " +
                        "(SELECT SUM(c.soluong) FROM ct_phieuxuat_thanhpham c WHERE c.mapxtp = p.mapxtp) AS soluong, " +
                        "(SELECT SUM(c.dongia) FROM ct_phieuxuat_thanhpham c WHERE c.mapxtp = p.mapxtp) AS dongia, " +
                        "(SELECT MAX(c.gia_da_fix) FROM ct_phieuxuat_thanhpham c WHERE c.mapxtp = p.mapxtp) AS gia_da_fix, " +
                        "(SELECT SUM(c.thanhtien) FROM ct_phieuxuat_thanhpham c WHERE c.mapxtp = p.mapxtp) AS thanhtien, " +
                        "kh.hoten AS nguoinhan, " +
                        "kh.sdt AS sdt_nguoinhan, " +
                        "kh.email AS email_nguoinhan " +
                        "FROM phieuxuat_thanhpham p " +
                        "JOIN nhanvien n ON n.manv = p.nguoilap " +
                        "LEFT JOIN khachhang kh ON kh.makh = p.makh ";

        return manager
                ? one(
                sql + "WHERE p.mapxtp = ?",
                id
        )
                : ownedOne(
                sql +
                "JOIN taikhoan t " +
                "ON t.manv = p.nguoilap " +
                "WHERE p.mapxtp = ? " +
                "AND t.email = ?",
                id,
                email
        );
    }

    public List<Map<String, Object>> finishedIssueLines(long id) {
        return jdbc.queryForList(
                """
                SELECT c.*, s.tensp, b.phienban
                FROM ct_phieuxuat_thanhpham c
                JOIN sanpham s ON s.masp = c.masp
                JOIN bom b ON b.mabom = c.mabom
                WHERE c.mapxtp = ?
                ORDER BY c.id
                """,
                id
        );
    }

    @Transactional
    public long createFinishedIssueBatch(
            String email,
            List<Integer> productIds,
            List<Integer> bomIds,
            List<Integer> quantities,
            int customerId,
            String address,
            List<BigDecimal> unitPrices,
            List<Boolean> priceFixedValues,
            String note) {

        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("Phiếu xuất phải có ít nhất một thành phẩm.");
        }

        int size = productIds.size();
        if (bomIds == null || quantities == null || unitPrices == null || priceFixedValues == null ||
                bomIds.size() != size || quantities.size() != size ||
                unitPrices.size() != size || priceFixedValues.size() != size) {
            throw new IllegalArgumentException("Thông tin các dòng thành phẩm không đầy đủ.");
        }

        if (productIds.stream().distinct().count() != size) {
            throw new IllegalArgumentException("Không được chọn trùng thành phẩm trong cùng phiếu xuất.");
        }

        long issueId = createFinishedIssue(
                email, productIds.get(0), bomIds.get(0), quantities.get(0), customerId,
                address, unitPrices.get(0), Boolean.TRUE.equals(priceFixedValues.get(0)), note
        );

        for (int index = 1; index < size; index++) {
            int productId = productIds.get(index);
            int bomId = bomIds.get(index);
            int quantity = quantities.get(index);
            BigDecimal unitPrice = unitPrices.get(index);

            validateFinishedIssueLine(productId, bomId, quantity, unitPrice);
            jdbc.update(
                    "INSERT INTO ct_phieuxuat_thanhpham " +
                            "(mapxtp, masp, mabom, soluong, dongia, gia_da_fix) VALUES (?, ?, ?, ?, ?, ?)",
                    issueId, productId, bomId, quantity, unitPrice,
                    Boolean.TRUE.equals(priceFixedValues.get(index)) ? 1 : 0
            );
        }

        return issueId;
    }

    private void validateFinishedIssueLine(int productId, int bomId, int quantity, BigDecimal unitPrice) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng xuất phải lớn hơn 0.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá không hợp lệ.");
        }

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bom b JOIN sanpham s ON s.masp = b.masp " +
                        "WHERE b.mabom = ? AND b.masp = ? AND b.trangthai = 'HIEU_LUC'",
                Integer.class, bomId, productId
        );
        if (count == null || count == 0) {
            throw new IllegalArgumentException("BOM không hợp lệ hoặc không thuộc thành phẩm đã chọn.");
        }
    }

    @Transactional
    public long createFinishedIssue(
            String email,
            int productId,
            int bomId,
            int quantity,
            int customerId,
            String address,
            BigDecimal unitPrice,
            boolean priceFixed,
            String note) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Số lượng xuất phải lớn hơn 0."
            );
        }

        if (customerId <= 0) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn người nhận."
            );
        }

        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập địa chỉ giao hàng."
            );
        }

        if (address.length() > 255) {
            throw new IllegalArgumentException(
                    "Địa chỉ không được vượt quá 255 ký tự."
            );
        }

        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Đơn giá không hợp lệ."
            );
        }

        validateNote(note);

        // =========================
        // Kiểm tra khách hàng
        // =========================

        List<Map<String, Object>> customerRows = jdbc.queryForList(
                """
                SELECT makh, hoten
                FROM khachhang
                WHERE makh = ?
                  AND trangthai = 0
                """,
                customerId
        );

        if (customerRows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Người nhận không tồn tại hoặc đã ngừng sử dụng."
            );
        }

        // =========================
        // Kiểm tra BOM
        // =========================

        List<Map<String, Object>> bomRows = jdbc.queryForList(
                """
                SELECT mabom, masp, trangthai
                FROM bom
                WHERE mabom = ?
                  AND masp = ?
                """,
                bomId,
                productId
        );

        if (bomRows.isEmpty()) {
            throw new IllegalArgumentException(
                    "BOM không thuộc sản phẩm đã chọn."
            );
        }

        String bomStatus = String.valueOf(
                bomRows.get(0).get("trangthai")
        );

        if (!"HIEU_LUC".equals(bomStatus)) {
            throw new IllegalArgumentException(
                    "Không thể xuất thành phẩm theo BOM đã ngừng sử dụng."
            );
        }

        // =========================
        // Kiểm tra sản phẩm
        // =========================

        List<Map<String, Object>> productRows = jdbc.queryForList(
                """
                SELECT masp
                FROM sanpham
                WHERE masp = ?
                """,
                productId
        );

        if (productRows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sản phẩm không tồn tại."
            );
        }

        // =========================
        // Tạo phiếu
        // =========================

        long id = insert(
                """
                INSERT INTO phieuxuat_thanhpham
                    (
                        makh,
                        diachi_giaohang,
                        nguoilap,
                        trangthai,
                        ghichu
                    )
                VALUES
                    (?, ?, ?, 'CHO_DUYET', ?)
                """,
                customerId,
                address.trim(),
                employeeId(email),
                note
        );

        // =========================
        // Tạo chi tiết phiếu
        // =========================

        jdbc.update(
                """
                INSERT INTO ct_phieuxuat_thanhpham
                    (
                        mapxtp,
                        masp,
                        mabom,
                        soluong,
                        dongia,
                        gia_da_fix
                    )
                VALUES
                    (?, ?, ?, ?, ?, ?)
                """,
                id,
                productId,
                bomId,
                quantity,
                unitPrice,
                priceFixed ? 1 : 0
        );

        return id;
    }

    private Map<String, Object> orderOwned(long id, String email) {
        return ownedOne("SELECT y.* FROM yeucausanxuat y JOIN taikhoan t ON t.manv = y.nguoiphutrach " +
                "WHERE y.maycsx = ? AND t.email = ?", id, email);
    }

    private Map<String, Object> orderOwnedForUpdate(long id, String email) {
        return ownedOne("SELECT y.* FROM yeucausanxuat y JOIN taikhoan t ON t.manv = y.nguoiphutrach " +
                "WHERE y.maycsx = ? AND t.email = ? FOR UPDATE", id, email);
    }

    private Map<String, Object> ownedOne(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbc.queryForList(sql, args);
        if (rows.isEmpty()) throw new org.springframework.security.access.AccessDeniedException("Bạn không có quyền truy cập dữ liệu này.");
        return rows.get(0);
    }

    private Map<String, Object> one(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbc.queryForList(sql, args);
        if (rows.isEmpty()) throw new IllegalArgumentException("Không tìm thấy dữ liệu hoặc bạn không có quyền xem.");
        return rows.get(0);
    }

    private long insert(String sql, Object... values) {
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.length; i++) statement.setObject(i + 1, values[i]);
            return statement;
        }, key);
        return key.getKey().longValue();
    }

    private void validateLines(List<Long> ids, List<Integer> quantities) {
        if (ids == null || ids.isEmpty() || quantities == null || ids.size() != quantities.size()) {
            throw new IllegalArgumentException("Phiếu phải có ít nhất một dòng chi tiết.");
        }
        Set<Long> unique = new HashSet<>();
        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i) == null || !unique.add(ids.get(i)) || quantities.get(i) == null || quantities.get(i) <= 0) {
                throw new IllegalArgumentException("NVL trùng hoặc số lượng không hợp lệ.");
            }
        }
    }

    private void validateNote(String note) {
        if (note != null && note.length() > 255) throw new IllegalArgumentException("Ghi chú tối đa 255 ký tự.");
    }

    public Map<String, List<Map<String, Object>>> groupMaterialsByType(
            List<Map<String, Object>> materials) {

        Map<String, List<Map<String, Object>>> groups =
                new LinkedHashMap<>();

        for (Map<String, Object> material : materials) {

            String typeName =
                    material.get("tenloai") != null
                            ? material.get("tenloai").toString()
                            : "Chưa phân loại";

            groups
                    .computeIfAbsent(
                            typeName,
                            key -> new java.util.ArrayList<>()
                    )
                    .add(material);
        }

        return groups;
    }

    private void validateActiveMaterials(List<Long> materialIds) {

        if (materialIds == null || materialIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Phiếu phải có ít nhất một nguyên vật liệu."
            );
        }

        String sql =
                """
                SELECT
                    n.manvl,
                    n.tennvl,
                    n.trangthai AS nvl_trangthai,
                    l.trangthai AS loai_trangthai
                FROM nguyenvatlieu n
                JOIN loai_nguyen_vat_lieu l
                    ON l.maloai = n.maloai
                WHERE n.manvl = ?
                """;

        for (Long materialId : materialIds) {

            List<Map<String, Object>> rows =
                    jdbc.queryForList(sql, materialId);

            if (rows.isEmpty()) {
                throw new IllegalArgumentException(
                        "Nguyên vật liệu không tồn tại: " + materialId
                );
            }

            Map<String, Object> row = rows.get(0);

            int materialStatus =
                    ((Number) row.get("nvl_trangthai")).intValue();

            int typeStatus =
                    ((Number) row.get("loai_trangthai")).intValue();

            String materialName =
                    String.valueOf(row.get("tennvl"));

            if (materialStatus != 1) {
                throw new IllegalArgumentException(
                        "Nguyên vật liệu \"" + materialName
                                + "\" đã ngừng sử dụng, không thể tạo nghiệp vụ mới."
                );
            }

            if (typeStatus != 1) {
                throw new IllegalArgumentException(
                        "Loại của nguyên vật liệu \""
                                + materialName
                                + "\" đã ngừng sử dụng, không thể tạo nghiệp vụ mới."
                );
            }
        }
    }

    private void validateActiveSupplier(int supplierId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                """
                SELECT mancc, tenncc
                FROM ncc
                WHERE mancc = ?
                  AND trangthai = 0
                """,
                supplierId
        );

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Nhà cung cấp không tồn tại hoặc đã ngừng hoạt động."
            );
        }
    }

    public List<Map<String, Object>> assignedBatches(String email) {
        return jdbc.queryForList(
                """
                SELECT
                    d.madot AS batchId,
                    d.madot_code,
                    d.tendot,
                    COUNT(DISTINCT y.maycsx) AS so_yeu_cau,
                    GROUP_CONCAT(
                        DISTINCT s.tensp
                        ORDER BY s.tensp
                        SEPARATOR ', '
                    ) AS danh_sach_san_pham
                FROM dot_san_xuat d
                JOIN yeucausanxuat y ON y.madot = d.madot
                JOIN sanpham s ON s.masp = y.masp
                JOIN taikhoan t ON t.manv = y.nguoiphutrach
                WHERE t.email = ?
                  AND y.trangthai NOT IN ('HOAN_THANH', 'HUY')
                  AND (
                      y.soluonghoanthanh < y.soluongyeucau
                      OR EXISTS (
                          SELECT 1
                          FROM ghi_nhan_san_xuat g
                          WHERE g.maycsx = y.maycsx
                      )
                  )
                GROUP BY d.madot, d.madot_code, d.tendot
                ORDER BY d.madot DESC
                """,
                email
        );
    }

    public List<Map<String, Object>> bomRequestLinesByBatch(
            long batchId,
            String email) {

        return jdbc.queryForList(
                """
                SELECT
                    demand.manvl,
                    n.manvl_code,
                    n.tennvl,
                    n.donvitinh,
                    demand.soluong_dinhmuc,
                    demand.tyle_haohut,
                    demand.nhu_cau,
                    COALESCE(k.soluongton, 0) AS soluongton,
                    COALESCE(pending.dang_de_nghi, 0) AS dang_de_nghi,
                    GREATEST(
                        demand.nhu_cau
                        - COALESCE(k.soluongton, 0)
                        - COALESCE(pending.dang_de_nghi, 0),
                        0
                    ) AS thieu_hut
                FROM (
                    SELECT
                        y.madot,
                        c.manvl,
                        SUM(c.soluong_dinhmuc) AS soluong_dinhmuc,
                        MAX(c.tyle_haohut) AS tyle_haohut,
                        SUM(
                            CEIL(
                                GREATEST(
                                    y.soluongyeucau
                                    - GREATEST(
                                        COALESCE(g.daghi, 0),
                                        y.soluonghoanthanh
                                          + COALESCE(r.dangcho, 0)
                                    ),
                                    0
                                )
                                * c.soluong_dinhmuc
                                * (1 + c.tyle_haohut / 100)
                            )
                        ) AS nhu_cau
                    FROM yeucausanxuat y
                    JOIN bom_chitiet c ON c.mabom = y.mabom
                    JOIN taikhoan t ON t.manv = y.nguoiphutrach
                    LEFT JOIN (
                        SELECT maycsx, SUM(soluong) AS daghi
                        FROM ghi_nhan_san_xuat
                        GROUP BY maycsx
                    ) g ON g.maycsx = y.maycsx
                    LEFT JOIN (
                        SELECT maycsx, SUM(soluonghoanthanh) AS dangcho
                        FROM ketqua_sanxuat
                        WHERE trangthai = 'CHO_XAC_NHAN'
                        GROUP BY maycsx
                    ) r ON r.maycsx = y.maycsx
                    WHERE y.madot = ?
                      AND t.email = ?
                      AND y.trangthai NOT IN ('HOAN_THANH', 'HUY')
                    GROUP BY y.madot, c.manvl
                ) demand
                JOIN nguyenvatlieu n ON n.manvl = demand.manvl
                JOIN loai_nguyen_vat_lieu l ON l.maloai = n.maloai
                LEFT JOIN kho_nvl_san_xuat k ON k.manvl = demand.manvl
                LEFT JOIN (
                    SELECT
                        x.madot,
                        x.manvl,
                        SUM(x.soluong) AS dang_de_nghi
                    FROM (
                        SELECT DISTINCT
                            y.madot,
                            p.mapx,
                            c.manvl,
                            c.soluong
                        FROM phieuxuat_nvl p
                        JOIN ct_phieuxuat_nvl c ON c.mapx = p.mapx
                        JOIN phieuxuat_nvl_ycsx py ON py.mapx = p.mapx
                        JOIN yeucausanxuat y ON y.maycsx = py.maycsx
                        WHERE p.trangthai IN ('CHO_DUYET', 'DA_DUYET')
        
                        UNION
        
                        SELECT DISTINCT
                            y.madot,
                            p.mapx,
                            c.manvl,
                            c.soluong
                        FROM phieuxuat_nvl p
                        JOIN ct_phieuxuat_nvl c ON c.mapx = p.mapx
                        JOIN yeucausanxuat y ON y.maycsx = p.maycsx
                        WHERE p.trangthai IN ('CHO_DUYET', 'DA_DUYET')
                          AND NOT EXISTS (
                              SELECT 1
                              FROM phieuxuat_nvl_ycsx py
                              WHERE py.mapx = p.mapx
                          )
                    ) x
                    GROUP BY x.madot, x.manvl
                ) pending
                    ON pending.madot = demand.madot
                   AND pending.manvl = demand.manvl
                WHERE n.trangthai = 1
                  AND l.trangthai = 1
                ORDER BY l.tenloai, n.tennvl
                """,
                batchId,
                email
        );
    }

    @Transactional
    public long createBatchMaterialIssue(
            String email,
            long batchId,
            String note,
            List<Long> materialIds,
            List<Integer> quantities) {

        if (materialIds == null || quantities == null
                || materialIds.isEmpty()
                || materialIds.size() != quantities.size()) {
            throw new IllegalArgumentException(
                    "Phiếu phải có ít nhất một dòng chi tiết."
            );
        }

        validateNote(note);

        // Lấy các yêu cầu còn hiệu lực trong đợt mà nhân viên được giao.
        List<Long> orderIds = jdbc.queryForList(
                """
                SELECT y.maycsx
                FROM yeucausanxuat y
                JOIN taikhoan t ON t.manv = y.nguoiphutrach
                WHERE y.madot = ?
                  AND t.email = ?
                  AND y.trangthai NOT IN ('HOAN_THANH', 'HUY')
                ORDER BY y.maycsx
                FOR UPDATE
                """,
                Long.class,
                batchId,
                email
        );

        if (orderIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không có yêu cầu sản xuất phù hợp trong đợt này."
            );
        }

        // Chỉ giữ các dòng có số lượng lớn hơn 0.
        Map<Long, Integer> requested = new LinkedHashMap<>();

        for (int i = 0; i < materialIds.size(); i++) {
            Long materialId = materialIds.get(i);
            Integer quantity = quantities.get(i);

            if (materialId == null || quantity == null || quantity < 0) {
                throw new IllegalArgumentException(
                        "Nguyên vật liệu hoặc số lượng đề nghị không hợp lệ."
                );
            }

            if (quantity > 0) {
                if (requested.putIfAbsent(materialId, quantity) != null) {
                    throw new IllegalArgumentException(
                            "Một nguyên vật liệu chỉ được xuất hiện một lần trong phiếu."
                    );
                }
            }
        }

        if (requested.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không có nguyên vật liệu nào cần xuất."
            );
        }

        List<Long> validMaterialIds =
                new java.util.ArrayList<>(requested.keySet());

        validateActiveMaterials(validMaterialIds);

        // Tính lại nhu cầu và phần thiếu từ database, không tin số liệu gửi từ HTML.
        Map<Long, Integer> maxByMaterial = new java.util.HashMap<>();

        for (Map<String, Object> line :
                bomRequestLinesByBatch(batchId, email)) {
            maxByMaterial.put(
                    ((Number) line.get("manvl")).longValue(),
                    ((Number) line.get("thieu_hut")).intValue()
            );
        }

        for (Map.Entry<Long, Integer> entry : requested.entrySet()) {
            int max = maxByMaterial.getOrDefault(entry.getKey(), 0);

            if (max <= 0) {
                throw new IllegalArgumentException(
                        "Một NVL đã đủ tồn hoặc đã có đề nghị xuất đang chờ duyệt."
                );
            }

            if (entry.getValue() > max) {
                throw new IllegalArgumentException(
                        "Số lượng đề nghị vượt phần còn thiếu của NVL ID "
                                + entry.getKey() + " (tối đa " + max + ")."
                );
            }
        }

        // Dùng một yêu cầu làm khóa tương thích với cấu trúc bảng cũ.
        long firstOrderId = orderIds.get(0);

        long issueId = insert(
                """
                INSERT INTO phieuxuat_nvl
                    (maycsx, nguoilap, trangthai, ghichu)
                VALUES (?, ?, 'CHO_DUYET', ?)
                """,
                firstOrderId,
                employeeId(email),
                note
        );

        // Liên kết phiếu với tất cả yêu cầu sản xuất thuộc đợt được xử lý.
        for (Long orderId : orderIds) {
            jdbc.update(
                    """
                    INSERT INTO phieuxuat_nvl_ycsx (mapx, maycsx)
                    VALUES (?, ?)
                    """,
                    issueId,
                    orderId
            );
        }

        // Mỗi NVL chỉ có một dòng chi tiết.
        for (Map.Entry<Long, Integer> entry : requested.entrySet()) {
            jdbc.update(
                    """
                    INSERT INTO ct_phieuxuat_nvl (mapx, manvl, soluong)
                    VALUES (?, ?, ?)
                    """,
                    issueId,
                    entry.getKey(),
                    entry.getValue()
            );
        }

        return issueId;
    }

    public boolean hasPendingBatchMaterialIssue(
            long batchId,
            String email
    ) {
        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(DISTINCT p.mapx)
                FROM phieuxuat_nvl p
                WHERE p.trangthai = 'CHO_DUYET'
                  AND (
                      EXISTS (
                          SELECT 1
                          FROM phieuxuat_nvl_ycsx py
                          JOIN yeucausanxuat y
                            ON y.maycsx = py.maycsx
                          JOIN taikhoan t
                            ON t.manv = y.nguoiphutrach
                          WHERE py.mapx = p.mapx
                            AND y.madot = ?
                            AND t.email = ?
                      )
                      OR (
                          NOT EXISTS (
                              SELECT 1
                              FROM phieuxuat_nvl_ycsx py0
                              WHERE py0.mapx = p.mapx
                          )
                          AND EXISTS (
                              SELECT 1
                              FROM yeucausanxuat y
                              JOIN taikhoan t
                                ON t.manv = y.nguoiphutrach
                              WHERE y.maycsx = p.maycsx
                                AND y.madot = ?
                                AND t.email = ?
                          )
                      )
                  )
                """,
                Integer.class,
                batchId, email,
                batchId, email
        );

        return count != null && count > 0;
    }

    public List<Map<String, Object>> resultGroups(
            String email,
            boolean manager) {

        String sql = """
        SELECT
            k.*,
            k.makqsx AS maketqua,
            k.maycsx AS mayeucau,
            COALESCE(
                k.ma_dot_ghi_nhan,
                CONCAT('legacy-', k.makqsx)
            ) AS ma_nhom_ghi_nhan,
            y.mabom,
            s.tensp,
            b.phienban,
            d.madot AS madot,
            d.madot_code,
            d.tendot,
            n.hoten
        FROM ketqua_sanxuat k
        JOIN yeucausanxuat y
            ON y.maycsx = k.maycsx
        JOIN sanpham s
            ON s.masp = y.masp
        JOIN bom b
            ON b.mabom = y.mabom
        LEFT JOIN dot_san_xuat d
            ON d.madot = y.madot
        JOIN nhanvien n
            ON n.manv = k.nguoithuchien
        """;

        List<Map<String, Object>> rows = manager
                ? jdbc.queryForList(
                sql + " ORDER BY k.makqsx DESC")
                : jdbc.queryForList(
                sql + """
                        JOIN taikhoan t
                            ON t.manv = k.nguoithuchien
                        WHERE t.email = ?
                        ORDER BY k.makqsx DESC
                        """,
                email
        );

        Map<String, Map<String, Object>> grouped =
                new LinkedHashMap<>();

        for (Map<String, Object> row : rows) {

            String key = String.valueOf(
                    row.get("ma_nhom_ghi_nhan")
            );

            Map<String, Object> group = grouped.get(key);

            if (group == null) {
                group = new LinkedHashMap<>();

                group.put("ma_nhom_ghi_nhan", key);
                group.put("ngayghi", row.get("ngayhoanthanh"));
                group.put("madot_code", row.get("madot_code"));
                group.put("tendot", row.get("tendot"));
                group.put("soluongtong", 0);
                group.put("soketqua", 0);

                // Trạng thái tổng hợp của cả nhóm
                group.put("trangthai", row.get("trangthai"));

                group.put(
                        "items",
                        new java.util.ArrayList<Map<String, Object>>()
                );

                grouped.put(key, group);
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items =
                    (List<Map<String, Object>>) group.get("items");

            items.add(row);

            // Cộng tổng số lượng
            int quantity = row.get("soluonghoanthanh") == null
                    ? 0
                    : ((Number) row.get("soluonghoanthanh")).intValue();

            group.put(
                    "soluongtong",
                    ((Number) group.get("soluongtong")).intValue()
                            + quantity
            );

            // Đếm số kết quả trong nhóm
            group.put(
                    "soketqua",
                    ((Number) group.get("soketqua")).intValue() + 1
            );

            /*
             * Trạng thái nhóm:
             * - Nếu có kết quả chờ xác nhận: CHO_XAC_NHAN
             * - Nếu không còn chờ xác nhận nhưng có kết quả chưa nhập kho:
             *   giữ trạng thái chưa hoàn tất tương ứng
             * - Nếu toàn bộ đã nhập kho: DA_NHAP_KHO
             * - Nếu toàn bộ đã hủy: HUY
             *
             * Ưu tiên trạng thái chưa hoàn tất để tránh nhóm bị hiển thị
             * "Đã duyệt" khi vẫn còn kết quả cần xử lý.
             */
            String currentStatus = String.valueOf(
                    group.get("trangthai")
            );

            String rowStatus = String.valueOf(
                    row.get("trangthai")
            );

            if ("CHO_XAC_NHAN".equals(rowStatus)) {
                group.put("trangthai", "CHO_XAC_NHAN");
            } else if ("CHO_XAC_NHAN".equals(currentStatus)) {
                // Giữ trạng thái đang chờ xác nhận
            } else if ("DA_NHAP_KHO".equals(rowStatus)
                    && !"HUY".equals(currentStatus)) {
                group.put("trangthai", "DA_NHAP_KHO");
            } else if ("HUY".equals(rowStatus)
                    && ("HUY".equals(currentStatus)
                    || group.get("soketqua").equals(1))) {
                group.put("trangthai", "HUY");
            }
        }

        return new java.util.ArrayList<>(grouped.values());
    }

    public boolean hasPendingBatchResults(long batchId) {
        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM ketqua_sanxuat k
                JOIN yeucausanxuat y
                    ON y.maycsx = k.maycsx
                WHERE y.madot = ?
                  AND k.trangthai = 'CHO_XAC_NHAN'
                """,
                Integer.class,
                batchId
        );

        return count != null && count > 0;
    }
}
