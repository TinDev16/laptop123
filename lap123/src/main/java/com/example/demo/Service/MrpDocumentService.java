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
        return jdbc.queryForList("SELECT manvl, manvl_code, tennvl, donvitinh, dongianhap, soluongton, ton_toithieu FROM nguyenvatlieu WHERE trangthai = 1 ORDER BY tennvl");
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
        return jdbc.queryForList("SELECT c.*, n.manvl_code, n.tennvl, n.donvitinh FROM ct_phieunhap_nvl c " +
                "JOIN nguyenvatlieu n ON n.manvl = c.manvl WHERE c.mapn = ? ORDER BY c.id", id);
    }

    @Transactional
    public long createReceipt(String email, int supplierId, String note, List<Long> materialIds,
                              List<Integer> quantities, List<BigDecimal> prices) {
        validateLines(materialIds, quantities);
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
        return jdbc.queryForList("SELECT c.*, n.manvl_code, n.tennvl, n.donvitinh FROM ct_phieuxuat_nvl c " +
                "JOIN nguyenvatlieu n ON n.manvl = c.manvl WHERE c.mapx = ? ORDER BY c.id", id);
    }

    public List<Map<String, Object>> assignedOrders(String email) {
        return jdbc.queryForList(
                "SELECT " +
                        "y.maycsx AS mayeucau, " +
                        "y.masp, " +
                        "y.mabom, " +
                        "s.tensp, " +
                        "b.phienban, " +
                        "y.soluongyeucau, " +
                        "y.soluonghoanthanh, " +
                        "y.hanhoanthanh AS ngaydenhan " +
                        "FROM yeucausanxuat y " +
                        "JOIN sanpham s ON s.masp = y.masp " +
                        "JOIN bom b ON b.mabom = y.mabom " +
                        "JOIN taikhoan t ON t.manv = y.nguoiphutrach " +
                        "WHERE t.email = ? " +
                        "AND y.trangthai IN ('DA_GIAO', 'DANG_SAN_XUAT') " +
                        "ORDER BY y.maycsx DESC",
                email
        );
    }

    public List<Map<String, Object>> bomRequestLines(long orderId, String email) {
        orderOwned(orderId, email);
        return jdbc.queryForList("SELECT c.manvl, n.manvl_code, n.tennvl, n.donvitinh, n.soluongton, " +
                "c.soluong_dinhmuc, c.tyle_haohut, CEIL((y.soluongyeucau - y.soluonghoanthanh) * c.soluong_dinhmuc * " +
                "(1 + c.tyle_haohut / 100)) AS nhu_cau FROM yeucausanxuat y " +
                "JOIN bom_chitiet c ON c.mabom = y.mabom JOIN nguyenvatlieu n ON n.manvl = c.manvl " +
                "WHERE y.maycsx = ? ORDER BY n.tennvl", orderId);
    }

    @Transactional
    public long createMaterialIssue(String email, long orderId, String note, List<Long> materialIds, List<Integer> quantities) {
        validateLines(materialIds, quantities);
        validateNote(note);
        Map<String, Object> order = orderOwned(orderId, email);
        if ("HOAN_THANH".equals(order.get("trangthai")) || "HUY".equals(order.get("trangthai"))) {
            throw new IllegalArgumentException("Yêu cầu sản xuất đã kết thúc.");
        }
        Set<Long> allowed = new HashSet<>();
        for (Map<String, Object> line : bomRequestLines(orderId, email)) allowed.add(((Number) line.get("manvl")).longValue());
        if (!allowed.containsAll(materialIds)) throw new IllegalArgumentException("NVL đề nghị không thuộc BOM của yêu cầu.");
        long id = insert("INSERT INTO phieuxuat_nvl (maycsx, nguoilap, trangthai, ghichu) VALUES (?, ?, 'CHO_DUYET', ?)",
                orderId, employeeId(email), note);
        for (int i = 0; i < materialIds.size(); i++) {
            jdbc.update("INSERT INTO ct_phieuxuat_nvl (mapx, manvl, soluong) VALUES (?, ?, ?)",
                    id, materialIds.get(i), quantities.get(i));
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
        if (completed <= 0 || defective < 0) throw new IllegalArgumentException("Số lượng hoàn thành phải lớn hơn 0 và số lỗi không được âm.");
        validateNote(note);
        Map<String, Object> order = orderOwnedForUpdate(orderId, email);
        if ("HUY".equals(order.get("trangthai")) || "HOAN_THANH".equals(order.get("trangthai"))) {
            throw new IllegalArgumentException("Yêu cầu sản xuất đã kết thúc.");
        }
        int remaining = ((Number) order.get("soluongyeucau")).intValue() - ((Number) order.get("soluonghoanthanh")).intValue();
        Integer pending = jdbc.queryForObject("SELECT COALESCE(SUM(soluonghoanthanh), 0) FROM ketqua_sanxuat WHERE maycsx = ? AND trangthai = 'CHO_XAC_NHAN'", Integer.class, orderId);
        if (completed > remaining - pending) throw new IllegalArgumentException("Số lượng hoàn thành vượt quá số lượng còn lại của yêu cầu sản xuất.");
        return insert("INSERT INTO ketqua_sanxuat (maycsx, nguoithuchien, soluonghoanthanh, soluongloi, trangthai, ghichu) " +
                "VALUES (?, ?, ?, ?, 'CHO_XAC_NHAN', ?)", orderId, employeeId(email), completed, defective, note);
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
                        "b.phienban " +
                        "FROM phieuxuat_thanhpham p " +
                        "JOIN nhanvien n ON n.manv = p.nguoilap " +
                        "JOIN ct_phieuxuat_thanhpham c ON c.mapxtp = p.mapxtp " +
                        "JOIN sanpham s ON s.masp = c.masp " +
                        "JOIN bom b ON b.mabom = c.mabom ";

        return manager
                ? one(sql + "WHERE p.mapxtp = ?", id)
                : ownedOne(
                sql +
                "JOIN taikhoan t ON t.manv = p.nguoilap " +
                "WHERE p.mapxtp = ? AND t.email = ?",
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
            String note) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Số lượng xuất phải lớn hơn 0."
            );
        }

        validateNote(note);

        // Kiểm tra BOM có thuộc đúng sản phẩm không
        List<Map<String, Object>> bomRows = jdbc.queryForList(
                "SELECT mabom, trangthai " +
                        "FROM bom " +
                        "WHERE mabom = ? AND masp = ?",
                bomId,
                productId
        );

        if (bomRows.isEmpty()) {
            throw new IllegalArgumentException(
                    "BOM không thuộc sản phẩm đã chọn."
            );
        }

        String bomStatus =
                String.valueOf(bomRows.get(0).get("trangthai"));

        if (!"HIEU_LUC".equals(bomStatus)) {
            throw new IllegalArgumentException(
                    "Không thể xuất thành phẩm theo BOM đã ngừng sử dụng."
            );
        }

        // Kiểm tra sản phẩm tồn tại
        List<Map<String, Object>> productRows =
                jdbc.queryForList(
                        "SELECT masp " +
                                "FROM sanpham " +
                                "WHERE masp = ?",
                        productId
                );

        if (productRows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sản phẩm không tồn tại."
            );
        }

        // Tạo phiếu xuất
        long id = insert(
                "INSERT INTO phieuxuat_thanhpham " +
                        "(nguoilap, trangthai, ghichu) " +
                        "VALUES (?, 'CHO_DUYET', ?)",
                employeeId(email),
                note
        );

        // Lưu sản phẩm + BOM + số lượng
        jdbc.update(
                "INSERT INTO ct_phieuxuat_thanhpham " +
                        "(mapxtp, masp, mabom, soluong) " +
                        "VALUES (?, ?, ?, ?)",
                id,
                productId,
                bomId,
                quantity
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
}