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
        return jdbc.queryForList("SELECT mancc, tenncc FROM ncc ORDER BY tenncc");
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
        String sql = "SELECT p.mapx AS maphieu, p.maycsx AS mayeucau, p.ngaylap AS ngaytao, p.trangthai, p.ghichu, n.hoten, s.tensp " +
                "FROM phieuxuat_nvl p JOIN nhanvien n ON n.manv = p.nguoilap " +
                "JOIN yeucausanxuat y ON y.maycsx = p.maycsx JOIN sanpham s ON s.masp = y.masp ";
        return manager ? jdbc.queryForList(sql + "ORDER BY p.mapx DESC") :
                jdbc.queryForList(sql + "WHERE EXISTS (SELECT 1 FROM taikhoan t WHERE t.email = ? AND (t.manv = p.nguoilap OR t.manv = y.nguoiphutrach)) ORDER BY p.mapx DESC", email);
    }

    public Map<String, Object> materialIssue(long id, String email, boolean manager) {
        String sql = "SELECT p.*, p.mapx AS maphieu, p.maycsx AS mayeucau, n.hoten, s.tensp FROM phieuxuat_nvl p " +
                "JOIN nhanvien n ON n.manv = p.nguoilap JOIN yeucausanxuat y ON y.maycsx = p.maycsx " +
                "JOIN sanpham s ON s.masp = y.masp ";
        return manager ? one(sql + "WHERE p.mapx = ?", id) :
                ownedOne(sql + "WHERE p.mapx = ? AND EXISTS (SELECT 1 FROM taikhoan t WHERE t.email = ? AND (t.manv = p.nguoilap OR t.manv = y.nguoiphutrach))", id, email);
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
                    s.tensp,
                    b.phienban,
                    y.soluongyeucau,
                    LEAST(y.soluongyeucau, y.soluonghoanthanh + COALESCE(g.daghi, 0)) AS soluonghoanthanh,
                    y.hanhoanthanh AS ngaydenhan,
                    COALESCE(g.daghi, 0) AS soluong_daghi_nhan,
                    COALESCE(p.dagui, 0) AS soluong_da_gui,
                    GREATEST(
                        COALESCE(g.daghi, 0) - COALESCE(p.dagui, 0),
                        0
                    ) AS soluong_co_the_gui,
                    GREATEST(
                        y.soluongyeucau - y.soluonghoanthanh - COALESCE(g.daghi, 0),
                        0
                    ) AS soluong_con_lai
                FROM yeucausanxuat y
                JOIN sanpham s ON s.masp = y.masp
                JOIN bom b ON b.mabom = y.mabom
                JOIN taikhoan t ON t.manv = y.nguoiphutrach
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
                      y.soluonghoanthanh + COALESCE(g.daghi, 0) < y.soluongyeucau
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
                        n.trangthai AS nvl_trangthai,
                        l.trangthai AS loai_trangthai,
                        c.soluong_dinhmuc,
                        c.tyle_haohut,
                    
                        CEIL(
                            GREATEST(
                                y.soluongyeucau
                                - y.soluonghoanthanh
                                - COALESCE(g.daghi, 0),
                                0
                            )
                            * c.soluong_dinhmuc
                            * (1 + c.tyle_haohut / 100)
                        ) AS nhu_cau,
                    
                        GREATEST(
                            CEIL(
                                GREATEST(
                                    y.soluongyeucau
                                    - y.soluonghoanthanh
                                    - COALESCE(g.daghi, 0),
                                    0
                                )
                                * c.soluong_dinhmuc
                                * (1 + c.tyle_haohut / 100)
                            ) - COALESCE(k.soluongton, 0),
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
                orderOwned(orderId, email);

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
                        "n.hoten " +
                        "FROM ketqua_sanxuat k " +
                        "JOIN yeucausanxuat y " +
                        "    ON y.maycsx = k.maycsx " +
                        "JOIN sanpham s " +
                        "    ON s.masp = y.masp " +
                        "JOIN bom b " +
                        "    ON b.mabom = y.mabom " +
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
                        "n.hoten " +
                        "FROM ketqua_sanxuat k " +
                        "JOIN yeucausanxuat y " +
                        "    ON y.maycsx = k.maycsx " +
                        "JOIN sanpham s " +
                        "    ON s.masp = y.masp " +
                        "JOIN bom b " +
                        "    ON b.mabom = y.mabom " +
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

        int availableRecorded = recorded - pending;
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
                        "s.tensp, " +
                        "c.soluong, " +
                        "c.mabom, " +
                        "b.phienban " +
                        "FROM phieuxuat_thanhpham p " +
                        "JOIN nhanvien n ON n.manv = p.nguoilap " +
                        "JOIN ct_phieuxuat_thanhpham c ON c.mapxtp = p.mapxtp " +
                        "JOIN sanpham s ON s.masp = c.masp " +
                        "JOIN bom b ON b.mabom = c.mabom ";

        return manager
                ? jdbc.queryForList(sql + "ORDER BY p.mapxtp DESC")
                : jdbc.queryForList(
                sql +
                "JOIN taikhoan t ON t.manv = p.nguoilap " +
                "WHERE t.email = ? " +
                "ORDER BY p.mapxtp DESC",
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
                        "s.tensp, " +
                        "c.soluong, " +
                        "c.mabom, " +
                        "c.dongia, " +
                        "CAST(c.gia_da_fix AS UNSIGNED) AS gia_da_fix, " +
                        "c.thanhtien, " +
                        "b.phienban, " +
                        "kh.hoten AS nguoinhan, " +
                        "kh.sdt AS sdt_nguoinhan, " +
                        "kh.email AS email_nguoinhan " +
                        "FROM phieuxuat_thanhpham p " +
                        "JOIN nhanvien n ON n.manv = p.nguoilap " +
                        "JOIN ct_phieuxuat_thanhpham c ON c.mapxtp = p.mapxtp " +
                        "JOIN sanpham s ON s.masp = c.masp " +
                        "JOIN bom b ON b.mabom = c.mabom " +
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
                """,
                customerId
        );

        if (customerRows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Người nhận không tồn tại."
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
}