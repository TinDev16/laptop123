package com.example.demo.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MrpWarehouseService {
    private final JdbcTemplate jdbc;

    public MrpWarehouseService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void completeMaterialReceipt(long receiptId) {
        String status = lockStatus("phieunhap_nvl", "mapn", receiptId);
        if ("DA_NHAP".equals(status)) throw new IllegalStateException("Phiếu nhập đã được xử lý trước đó.");
        requireStatus(status, "NHAP_NHAP");
        List<Line> lines = jdbc.query(
                "SELECT manvl, SUM(soluong), MAX(dongia) " +
                        "FROM ct_phieunhap_nvl " +
                        "WHERE mapn = ? " +
                        "GROUP BY manvl " +
                        "ORDER BY manvl",
                (rs, row) -> new Line(
                        rs.getLong(1),
                        0L,
                        rs.getBigDecimal(2),
                        rs.getBigDecimal(3)
                ),
                receiptId
        );
        requireLines(lines);
        Integer actor = jdbc.queryForObject("SELECT nguoilap FROM phieunhap_nvl WHERE mapn = ?", Integer.class, receiptId);
        for (Line line : lines) {
            requirePositive(line.quantity());
            BigDecimal before = lockStock("nguyenvatlieu", "manvl", line.itemId());
            BigDecimal after = before.add(line.quantity());
            jdbc.update("UPDATE nguyenvatlieu SET soluongton = ? WHERE manvl = ?", after, line.itemId());
            history("NHAP_NVL", "NVL", line, actor, "phieunhap_nvl", receiptId);
        }
        jdbc.update("UPDATE phieunhap_nvl SET trangthai = 'DA_NHAP' WHERE mapn = ?", receiptId);
    }

    @Transactional
    public void issueMaterials(long issueId) {
        issueMaterials(issueId, null);
    }

    @Transactional
    public void issueMaterials(long issueId, String managerEmail) {
        String status = lockStatus("phieuxuat_nvl", "mapx", issueId);
        if ("DA_XUAT".equals(status)) throw new IllegalStateException("Phiếu xuất đã được xử lý trước đó.");
        requireStatus(status, "CHO_DUYET");
        List<Line> lines = jdbc.query(
                "SELECT manvl, SUM(soluong) " +
                        "FROM ct_phieuxuat_nvl " +
                        "WHERE mapx = ? " +
                        "GROUP BY manvl " +
                        "ORDER BY manvl",
                (rs, row) -> new Line(
                        rs.getLong(1),
                        0L,
                        rs.getBigDecimal(2),
                        BigDecimal.ZERO
                ),
                issueId
        );
        requireLines(lines);
        List<StockChange> changes = new ArrayList<>();
        for (Line line : lines) {
            requirePositive(line.quantity());
            BigDecimal before = lockStock("nguyenvatlieu", "manvl", line.itemId());
            if (before.compareTo(line.quantity()) < 0) {
                String name = jdbc.queryForObject("SELECT tennvl FROM nguyenvatlieu WHERE manvl = ?", String.class, line.itemId());
                throw new IllegalStateException("Không đủ nguyên vật liệu " + name + ". Tồn hiện tại: " + before + ". Số lượng yêu cầu: " + line.quantity() + ".");
            }
            changes.add(new StockChange(line, before, before.subtract(line.quantity())));
        }
        Integer actor = managerEmail == null ? null : employeeId(managerEmail);
        for (StockChange change : changes) {
            jdbc.update("UPDATE nguyenvatlieu SET soluongton = ? WHERE manvl = ?", change.after(), change.line().itemId());
            history("XUAT_NVL", "NVL", change.line(), actor, "phieuxuat_nvl", issueId);
        }
        jdbc.update("UPDATE phieuxuat_nvl SET trangthai = 'DA_XUAT', nguoiduyet = ?, ngayduyet = CURRENT_TIMESTAMP WHERE mapx = ?", actor, issueId);
    }

    @Transactional
    public void rejectMaterialIssue(long issueId, String reason, String managerEmail) {
        String status = lockStatus("phieuxuat_nvl", "mapx", issueId);
        requireStatus(status, "CHO_DUYET");
        requireReason(reason);
        jdbc.update("UPDATE phieuxuat_nvl SET trangthai = 'TU_CHOI', lydo = ?, nguoiduyet = ?, ngayduyet = CURRENT_TIMESTAMP WHERE mapx = ?",
                reason.trim(), employeeId(managerEmail), issueId);
    }

    @Transactional
    public void confirmProduction(long resultId) {
        confirmProduction(resultId, null);
    }

    @Transactional
    public void confirmProduction(long resultId, String managerEmail) {
        String status = lockStatus("ketqua_sanxuat", "makqsx", resultId);
        if ("DA_NHAP_KHO".equals(status)) throw new IllegalStateException("Kết quả đã được xác nhận trước đó.");
        requireStatus(status, "CHO_XAC_NHAN");
        ProductionResult result = jdbc.queryForObject(
                "SELECT maycsx, soluonghoanthanh FROM ketqua_sanxuat WHERE makqsx = ?",
                (rs, row) -> new ProductionResult(rs.getLong(1), rs.getInt(2)), resultId);
        if (result == null) throw new IllegalArgumentException("Không tìm thấy kết quả sản xuất.");
        if (result.quantity() <= 0) throw new IllegalArgumentException("Số lượng hoàn thành phải lớn hơn 0.");
        ProductionOrder order = jdbc.queryForObject(
                "SELECT masp, soluongyeucau, soluonghoanthanh FROM yeucausanxuat WHERE maycsx = ? FOR UPDATE",
                (rs, row) -> new ProductionOrder(rs.getLong(1), rs.getInt(2), rs.getInt(3)), result.orderId());
        if (order == null || order.completed() + result.quantity() > order.requested()) {
            throw new IllegalStateException("Số lượng hoàn thành vượt quá số lượng còn lại của yêu cầu sản xuất.");
        }
        BigDecimal before = lockStock("sanpham", "masp", order.productId());
        BigDecimal quantity = BigDecimal.valueOf(result.quantity());
        BigDecimal after = before.add(quantity);
        jdbc.update("UPDATE sanpham SET soluongton = ? WHERE masp = ?", after.intValueExact(), order.productId());
        int completed = order.completed() + result.quantity();
        jdbc.update("UPDATE yeucausanxuat SET soluonghoanthanh = ?, trangthai = ? WHERE maycsx = ?",
                completed, completed == order.requested() ? "HOAN_THANH" : "DANG_SAN_XUAT", result.orderId());
        history(
                "NHAP_THANH_PHAM",
                "THANH_PHAM",
                new Line(
                        order.productId(),
                        0L,
                        quantity,
                        BigDecimal.ZERO
                ),
                managerEmail == null ? null : employeeId(managerEmail),
                "ketqua_sanxuat",
                resultId
        );
        jdbc.update("UPDATE ketqua_sanxuat SET trangthai = 'DA_NHAP_KHO' WHERE makqsx = ?", resultId);
    }

    @Transactional
    public void issueFinishedProducts(long issueId) {
        issueFinishedProducts(issueId, null);
    }

    @Transactional
    public void issueFinishedProducts(long issueId, String managerEmail) {

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

        requireStatus(status, "CHO_DUYET");

        /*
         * Lấy sản phẩm + BOM + số lượng trong phiếu.
         *
         * Group theo cả masp và mabom vì một sản phẩm có thể
         * có nhiều phiên bản BOM.
         */
        List<Line> lines = jdbc.query(
                "SELECT masp, mabom, SUM(soluong), MAX(dongia) " +
                        "FROM ct_phieuxuat_thanhpham " +
                        "WHERE mapxtp = ? " +
                        "GROUP BY masp, mabom " +
                        "ORDER BY masp, mabom",
                (rs, row) -> new Line(
                        rs.getLong(1),
                        rs.getLong(2),
                        rs.getBigDecimal(3),
                        rs.getBigDecimal(4)
                ),
                issueId
        );

        requireLines(lines);

        List<StockChange> changes = new ArrayList<>();

        for (Line line : lines) {

            requirePositive(line.quantity());

            /*
             * Kiểm tra BOM có tồn tại và thuộc đúng sản phẩm.
             */
            Integer bomProduct = jdbc.query(
                    "SELECT masp " +
                            "FROM bom " +
                            "WHERE mabom = ?",
                    ps -> ps.setLong(1, line.bomId()),
                    rs -> rs.next() ? rs.getInt("masp") : null
            );

            if (bomProduct == null) {
                throw new IllegalStateException(
                        "Không tìm thấy BOM " + line.bomId() + "."
                );
            }

            if (bomProduct.longValue() != line.itemId()) {
                throw new IllegalStateException(
                        "BOM " + line.bomId()
                                + " không thuộc sản phẩm " + line.itemId() + "."
                );
            }

            /*
             * Chỉ BOM đang hiệu lực mới được xuất.
             */
            String bomStatus = jdbc.query(
                    "SELECT trangthai " +
                            "FROM bom " +
                            "WHERE mabom = ?",
                    ps -> ps.setLong(1, line.bomId()),
                    rs -> rs.next() ? rs.getString("trangthai") : null
            );

            if (!"HIEU_LUC".equals(bomStatus)) {
                throw new IllegalStateException(
                        "BOM " + line.bomId()
                                + " đã ngừng sử dụng, không thể xuất thành phẩm."
                );
            }

            /*
             * Khóa dòng sản phẩm để tránh hai phiếu xuất
             * cùng lúc làm sai tồn kho.
             */
            BigDecimal before = lockStock(
                    "sanpham",
                    "masp",
                    line.itemId()
            );

            if (before.compareTo(line.quantity()) < 0) {
                throw new IllegalStateException(
                        "Không đủ thành phẩm. Tồn hiện tại: "
                                + before
                                + ". Số lượng yêu cầu: "
                                + line.quantity()
                                + "."
                );
            }

            /*
             * Tính số lượng thành phẩm còn lại của BOM:
             *
             * Đã nhập kho theo BOM
             * -
             * Đã xuất thành phẩm theo BOM
             */
            BigDecimal produced = jdbc.queryForObject(
                    "SELECT COALESCE(SUM(k.soluonghoanthanh), 0) " +
                            "FROM yeucausanxuat y " +
                            "JOIN ketqua_sanxuat k " +
                            "ON k.maycsx = y.maycsx " +
                            "WHERE y.mabom = ? " +
                            "AND k.trangthai = 'DA_NHAP_KHO'",
                    BigDecimal.class,
                    line.bomId()
            );

            BigDecimal exported = jdbc.queryForObject(
                    "SELECT COALESCE(SUM(c.soluong), 0) " +
                            "FROM phieuxuat_thanhpham p " +
                            "JOIN ct_phieuxuat_thanhpham c " +
                            "ON c.mapxtp = p.mapxtp " +
                            "WHERE c.mabom = ? " +
                            "AND p.trangthai = 'DA_XUAT'",
                    BigDecimal.class,
                    line.bomId()
            );

            BigDecimal bomAvailable =
                    produced.subtract(exported);

            if (bomAvailable.compareTo(line.quantity()) < 0) {
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
                            before.subtract(line.quantity())
                    )
            );
        }

        Integer actor =
                managerEmail == null
                        ? null
                        : employeeId(managerEmail);

        /*
         * Chỉ đến đây mới thực sự trừ tồn kho.
         */
        for (StockChange change : changes) {

            jdbc.update(
                    "UPDATE sanpham " +
                            "SET soluongton = ? " +
                            "WHERE masp = ?",
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

        /*
         * Phiếu được đánh dấu đã xuất.
         *
         * Các query BOM ở BomService sẽ dựa vào trạng thái DA_XUAT
         * để trừ số lượng.
         */
        jdbc.update(
                "UPDATE phieuxuat_thanhpham " +
                        "SET trangthai = 'DA_XUAT', " +
                        "nguoiduyet = ?, " +
                        "ngayduyet = CURRENT_TIMESTAMP " +
                        "WHERE mapxtp = ?",
                actor,
                issueId
        );
    }

    @Transactional
    public void rejectFinishedIssue(long issueId, String reason, String managerEmail) {
        String status = lockStatus("phieuxuat_thanhpham", "mapxtp", issueId);
        requireStatus(status, "CHO_DUYET");
        requireReason(reason);
        jdbc.update("UPDATE phieuxuat_thanhpham SET trangthai = 'TU_CHOI', lydo = ?, nguoiduyet = ?, ngayduyet = CURRENT_TIMESTAMP WHERE mapxtp = ?",
                reason.trim(), employeeId(managerEmail), issueId);
    }

    private int employeeId(String email) {
        Integer id = jdbc.queryForObject("SELECT manv FROM taikhoan WHERE email = ?", Integer.class, email);
        if (id == null) throw new IllegalStateException("Tài khoản chưa liên kết với nhân viên.");
        return id;
    }

    private String lockStatus(String table, String key, long id) {
        List<String> values = jdbc.query("SELECT trangthai FROM " + table + " WHERE " + key + " = ? FOR UPDATE",
                (rs, row) -> rs.getString(1), id);
        if (values.isEmpty()) throw new IllegalArgumentException("Không tìm thấy chứng từ: " + id);
        return values.get(0);
    }

    private BigDecimal lockStock(String table, String key, long id) {
        List<BigDecimal> values = jdbc.query("SELECT COALESCE(soluongton, 0) FROM " + table + " WHERE " + key + " = ? FOR UPDATE",
                (rs, row) -> rs.getBigDecimal(1), id);
        if (values.isEmpty()) throw new IllegalArgumentException("Không tìm thấy hàng hóa: " + id);
        return values.get(0);
    }

    private void history(String type, String kind, Line line, Integer actor, String sourceTable, long documentId) {
        jdbc.update("INSERT INTO lich_su_kho (loai_hang, ma_hang, loai_giaodich, soluong, don_gia, nguoi_thuchien, bang_tham_chieu, ma_tham_chieu) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                kind, line.itemId(), type, line.quantity(), line.price(), actor, sourceTable, documentId);
    }

    private void requireStatus(String actual, String expected) {
        if (!expected.equals(actual)) throw new IllegalStateException("Chứng từ không ở trạng thái " + expected + ".");
    }

    private void requirePositive(BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0) throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
    }

    private void requireLines(List<Line> lines) {
        if (lines.isEmpty()) throw new IllegalArgumentException("Phiếu phải có ít nhất một dòng chi tiết.");
    }

    private void requireReason(String reason) {
        if (reason == null || reason.isBlank() || reason.length() > 255) throw new IllegalArgumentException("Lý do từ chối phải có từ 1 đến 255 ký tự.");
    }

    private record Line(
            long itemId,
            long bomId,
            BigDecimal quantity,
            BigDecimal price
    ) {}
    private record StockChange(Line line, BigDecimal before, BigDecimal after) {}
    private record ProductionResult(long orderId, int quantity) {}
    private record ProductionOrder(long productId, int requested, int completed) {}
}