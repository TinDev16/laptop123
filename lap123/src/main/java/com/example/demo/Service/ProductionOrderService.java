package com.example.demo.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class ProductionOrderService {
    private final JdbcTemplate jdbc;

    public ProductionOrderService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list(String email, boolean manager) {
        String sql = "SELECT y.maycsx AS mayeucau, y.soluongyeucau, " +
                "y.soluonghoanthanh, y.ngayyeucau, y.hanhoanthanh, " +
                "y.trangthai, s.tensp, n.hoten " +
                "FROM yeucausanxuat y " +
                "JOIN sanpham s ON s.masp = y.masp " +
                "LEFT JOIN nhanvien n ON n.manv = y.nguoiphutrach ";

        List<Map<String, Object>> rows;

        if (manager) {
            rows = jdbc.queryForList(sql + "ORDER BY y.maycsx DESC");
        } else {
            rows = jdbc.queryForList(
                    sql +
                            "JOIN taikhoan t ON t.manv = y.nguoiphutrach " +
                            "WHERE t.email = ? ORDER BY y.maycsx DESC",
                    email
            );
        }

        for (Map<String, Object> row : rows) {
            addOverdueInfo(row);
        }

        return rows;
    }

    public Map<String, Object> get(long id, String email, boolean manager) {
        String sql = "SELECT y.maycsx AS mayeucau, y.masp, y.mabom, y.nguoiphutrach AS manv, " +
                "y.soluongyeucau, y.soluonghoanthanh, y.ngayyeucau, y.hanhoanthanh, y.trangthai, y.ghichu, " +
                "s.tensp, n.hoten FROM yeucausanxuat y JOIN sanpham s ON s.masp = y.masp " +
                "LEFT JOIN nhanvien n ON n.manv = y.nguoiphutrach ";
        List<Map<String, Object>> rows = manager ?
                jdbc.queryForList(sql + "WHERE y.maycsx = ?", id) :
                jdbc.queryForList(sql + "JOIN taikhoan t ON t.manv = y.nguoiphutrach WHERE y.maycsx = ? AND t.email = ?", id, email);
        if (rows.isEmpty()) {
            if (!manager) throw new org.springframework.security.access.AccessDeniedException("Không có quyền xem yêu cầu sản xuất này.");
            throw new IllegalArgumentException("Không tìm thấy yêu cầu sản xuất.");
        }
        Map<String, Object> order = rows.get(0);

        addOverdueInfo(order);

        return order;
    }

    public List<Map<String, Object>> activeBoms() {
        return jdbc.queryForList("SELECT b.mabom, b.phienban, s.tensp FROM bom b JOIN sanpham s ON s.masp = b.masp WHERE b.trangthai = 'HIEU_LUC' ORDER BY s.tensp");
    }

    public List<Map<String, Object>> employees() {
        return jdbc.queryForList("SELECT DISTINCT n.manv, n.hoten FROM nhanvien n JOIN taikhoan t ON t.manv = n.manv " +
                "JOIN vaitro v ON v.vaitro_id = t.vaitro_id WHERE v.ma_vaitro = 'NHAN_VIEN' ORDER BY n.hoten");
    }

    @Transactional
    public void create(String creatorEmail, long bomId, int employeeId, int quantity, LocalDate deadline, String note) {
        if (note != null && note.length() > 255) throw new IllegalArgumentException("Ghi chú tối đa 255 ký tự.");
        if (quantity <= 0) throw new IllegalArgumentException("Số lượng sản xuất phải lớn hơn 0.");
        if (deadline == null || deadline.isBefore(LocalDate.now())) throw new IllegalArgumentException("Hạn hoàn thành không hợp lệ.");
        List<Integer> productIds = jdbc.query("SELECT masp FROM bom WHERE mabom = ? AND trangthai = 'HIEU_LUC'",
                (rs, row) -> rs.getInt(1), bomId);
        if (productIds.isEmpty()) throw new IllegalArgumentException("BOM không hiệu lực hoặc không tồn tại.");
        Integer creator = jdbc.queryForObject("SELECT manv FROM taikhoan WHERE email = ?", Integer.class, creatorEmail);
        if (creator == null) throw new IllegalArgumentException("Tài khoản quản lý chưa liên kết với nhân viên.");
        jdbc.update("INSERT INTO yeucausanxuat (masp, mabom, nguoitao, nguoiphutrach, soluongyeucau, hanhoanthanh, trangthai, ghichu) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 'DA_GIAO', ?)",
                productIds.get(0), bomId, creator, employeeId, quantity, deadline, note);
    }

    public List<MaterialNeed> materialNeeds(Map<String, Object> order) {
        long bomId = ((Number) order.get("mabom")).longValue();
        int remaining = ((Number) order.get("soluongyeucau")).intValue() -
                ((Number) order.get("soluonghoanthanh")).intValue();
        List<Map<String, Object>> lines = jdbc.queryForList("SELECT n.manvl_code, n.tennvl, n.donvitinh, n.soluongton, " +
                "c.soluong_dinhmuc, c.tyle_haohut FROM bom_chitiet c " +
                "JOIN nguyenvatlieu n ON n.manvl = c.manvl WHERE c.mabom = ? ORDER BY n.tennvl", bomId);
        List<MaterialNeed> result = new ArrayList<>();
        for (Map<String, Object> line : lines) {
            BigDecimal standard = (BigDecimal) line.get("soluong_dinhmuc");
            BigDecimal waste = (BigDecimal) line.get("tyle_haohut");
            BigDecimal needed = standard.multiply(BigDecimal.valueOf(Math.max(remaining, 0)))
                    .multiply(BigDecimal.ONE.add(waste.movePointLeft(2)))
                    .setScale(0, RoundingMode.CEILING);
            BigDecimal available = (BigDecimal) line.get("soluongton");
            result.add(new MaterialNeed((String) line.get("manvl_code"), (String) line.get("tennvl"),
                    (String) line.get("donvitinh"), standard, waste, needed, available,
                    needed.subtract(available).max(BigDecimal.ZERO)));
        }
        return result;
    }

    private void addOverdueInfo(Map<String, Object> order) {
        Object deadlineValue = order.get("hanhoanthanh");

        if (deadlineValue == null) {
            order.put("overdueDays", 0L);
            return;
        }

        LocalDate deadline;

        if (deadlineValue instanceof java.sql.Date date) {
            deadline = date.toLocalDate();
        } else if (deadlineValue instanceof LocalDate date) {
            deadline = date;
        } else {
            deadline = LocalDate.parse(deadlineValue.toString());
        }

        boolean completed = "HOAN_THANH".equals(order.get("trangthai"));

        long overdueDays = 0;

        if (!completed && LocalDate.now().isAfter(deadline)) {
            overdueDays = ChronoUnit.DAYS.between(deadline, LocalDate.now());
        }

        order.put("overdueDays", overdueDays);
    }

    public record MaterialNeed(String code, String name, String unit, BigDecimal standard, BigDecimal waste,
                               BigDecimal needed, BigDecimal available, BigDecimal shortage) {}
}