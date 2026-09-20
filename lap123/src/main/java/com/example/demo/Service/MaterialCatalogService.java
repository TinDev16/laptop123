package com.example.demo.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaterialCatalogService {
    private final JdbcTemplate jdbc;

    public MaterialCatalogService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list(String keyword) {
        return jdbc.queryForList("SELECT n.*, c.tenncc, CASE WHEN n.trangthai = 1 THEN 'Hoạt động' ELSE 'Ngừng sử dụng' END trangthai_label FROM nguyenvatlieu n LEFT JOIN ncc c ON c.mancc = n.mancc " +
                "WHERE n.tennvl LIKE ? OR n.manvl_code LIKE ? ORDER BY n.manvl DESC", "%" + keyword + "%", "%" + keyword + "%");
    }

    public List<Map<String, Object>> suppliers() {
        return jdbc.queryForList("SELECT mancc, tenncc FROM ncc ORDER BY tenncc");
    }

    public Map<String, Object> get(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM nguyenvatlieu WHERE manvl = ?", id);
        if (rows.isEmpty()) throw new IllegalArgumentException("Không tìm thấy nguyên vật liệu.");
        return rows.get(0);
    }

    @Transactional
    public void save(Long id, String code, String name, Integer supplierId, String unit,
                     BigDecimal price, int minimum, int leadDays, String status, String note) {
        if (code == null || code.isBlank() || name == null || name.isBlank() || unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Mã, tên và đơn vị tính không được trống.");
        }
        if (code.length() > 30 || name.length() > 255 || unit.length() > 50 || (note != null && note.length() > 255)) throw new IllegalArgumentException("Thông tin NVL vượt độ dài cho phép.");
        if (price == null || price.signum() < 0 || minimum < 0 || leadDays < 0) {
            throw new IllegalArgumentException("Giá và các chỉ số tồn kho không được âm.");
        }
        if (!"HOAT_DONG".equals(status) && !"NGUNG_SU_DUNG".equals(status)) {
            throw new IllegalArgumentException("Trạng thái NVL không hợp lệ.");
        }
        int statusValue = "HOAT_DONG".equals(status) ? 1 : 0;
        if (id == null) {
            jdbc.update("INSERT INTO nguyenvatlieu (mancc, manvl_code, tennvl, donvitinh, dongianhap, " +
                            "soluongton, ton_toithieu, lead_time_ngay, trangthai, ghichu) VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?, ?)",
                    supplierId, code.trim(), name.trim(), unit.trim(), price, minimum, leadDays, statusValue, note);
        } else {
            int updated = jdbc.update("UPDATE nguyenvatlieu SET mancc = ?, manvl_code = ?, tennvl = ?, donvitinh = ?, " +
                            "dongianhap = ?, ton_toithieu = ?, lead_time_ngay = ?, trangthai = ?, ghichu = ? WHERE manvl = ?",
                    supplierId, code.trim(), name.trim(), unit.trim(), price, minimum, leadDays, statusValue, note, id);
            if (updated == 0) throw new IllegalArgumentException("Không tìm thấy nguyên vật liệu.");
        }
    }
}
