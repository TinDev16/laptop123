package com.example.demo.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class WarehouseReportService {
    private final JdbcTemplate jdbc;

    public WarehouseReportService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> history(LocalDate from, LocalDate to, String kind, String type) {
        StringBuilder sql = new StringBuilder("SELECT h.id AS malichsu, h.loai_hang, h.ma_hang, h.loai_giaodich AS loai_giao_dich, " +
                "h.soluong AS so_luong, h.don_gia AS dongia, h.thoi_gian AS ngay_giao_dich, " +
                "h.ma_tham_chieu AS ma_chung_tu, h.ghichu, COALESCE(n.tennvl, s.tensp) tenhang, v.hoten nguoithuchien " +
                "FROM lich_su_kho h LEFT JOIN nguyenvatlieu n ON h.loai_hang = 'NVL' AND n.manvl = h.ma_hang " +
                "LEFT JOIN sanpham s ON h.loai_hang = 'THANH_PHAM' AND s.masp = h.ma_hang " +
                "LEFT JOIN nhanvien v ON v.manv = h.nguoi_thuchien WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (from != null) { sql.append(" AND h.thoi_gian >= ?"); args.add(from.atStartOfDay()); }
        if (to != null) { sql.append(" AND h.thoi_gian < ?"); args.add(to.plusDays(1).atStartOfDay()); }
        if (kind != null && !kind.isBlank()) { sql.append(" AND h.loai_hang = ?"); args.add(kind); }
        if (type != null && !type.isBlank()) { sql.append(" AND h.loai_giaodich = ?"); args.add(type); }
        sql.append(" ORDER BY h.thoi_gian DESC, h.id DESC LIMIT 500");
        return jdbc.queryForList(sql.toString(), args.toArray());
    }

    public Map<String, BigDecimal> totals(LocalDate from, LocalDate to) {
        String sql = "SELECT loai_giaodich, COALESCE(SUM(soluong), 0) total FROM lich_su_kho " +
                "WHERE thoi_gian >= ? AND thoi_gian < ? GROUP BY loai_giaodich";
        Map<String, BigDecimal> totals = new HashMap<>();
        for (Map<String, Object> row : jdbc.queryForList(sql, from.atStartOfDay(), to.plusDays(1).atStartOfDay())) {
            totals.put((String) row.get("loai_giaodich"), (BigDecimal) row.get("total"));
        }
        return totals;
    }
}