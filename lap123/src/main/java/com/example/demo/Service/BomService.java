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
public class BomService {
    private final JdbcTemplate jdbc;

    public BomService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list() {
        return jdbc.queryForList("SELECT b.mabom, b.phienban, b.trangthai, b.ghichu, s.tensp " +
                "FROM bom b JOIN sanpham s ON s.masp = b.masp ORDER BY b.mabom DESC");
    }

    public List<Map<String, Object>> products() {
        return jdbc.queryForList("SELECT masp, tensp FROM sanpham ORDER BY tensp");
    }

    public List<Map<String, Object>> materials() {
        return jdbc.queryForList("SELECT manvl, manvl_code, tennvl, donvitinh FROM nguyenvatlieu WHERE trangthai = 1 ORDER BY tennvl");
    }

    public Map<String, Object> get(long id) {
        return jdbc.queryForMap("SELECT b.mabom, b.masp, b.phienban, b.trangthai, b.ghichu, s.tensp " +
                "FROM bom b JOIN sanpham s ON s.masp = b.masp WHERE b.mabom = ?", id);
    }

    public List<Map<String, Object>> details(long id) {
        return jdbc.queryForList("SELECT c.manvl, n.manvl_code, n.tennvl, n.donvitinh, " +
                "c.soluong_dinhmuc, c.tyle_haohut AS ty_le_hao_hut FROM bom_chitiet c " +
                "JOIN nguyenvatlieu n ON n.manvl = c.manvl WHERE c.mabom = ? ORDER BY c.id", id);
    }

    @Transactional
    public long create(String email, int productId, String version, String note, List<Long> materialIds,
                       List<BigDecimal> quantities, List<BigDecimal> wastePercentages) {
        if (version == null || version.isBlank() || version.length() > 30) throw new IllegalArgumentException("Phiên bản BOM không được trống.");
        if (note != null && note.length() > 255) throw new IllegalArgumentException("Ghi chú tối đa 255 ký tự.");
        if (materialIds == null || materialIds.isEmpty() || materialIds.size() != quantities.size() ||
                materialIds.size() != wastePercentages.size()) {
            throw new IllegalArgumentException("Chi tiết BOM không hợp lệ.");
        }
        Set<Long> unique = new HashSet<>();
        for (int i = 0; i < materialIds.size(); i++) {
            if (!unique.add(materialIds.get(i))) throw new IllegalArgumentException("Nguyên vật liệu bị trùng trong BOM.");
            if (quantities.get(i) == null || quantities.get(i).signum() <= 0 ||
                    wastePercentages.get(i) == null || wastePercentages.get(i).signum() < 0 || wastePercentages.get(i).compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("Định mức phải lớn hơn 0 và hao hụt không được âm.");
            }
        }
        Integer creator = jdbc.queryForObject("SELECT manv FROM taikhoan WHERE email = ?", Integer.class, email);
        if (creator == null) throw new IllegalArgumentException("Tài khoản chưa liên kết với nhân viên.");
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO bom (masp, phienban, nguoitao, ghichu, trangthai) VALUES (?, ?, ?, ?, 'HIEU_LUC')",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, productId);
            statement.setString(2, version.trim());
            statement.setInt(3, creator);
            statement.setString(4, note);
            return statement;
        }, key);
        long id = key.getKey().longValue();
        for (int i = 0; i < materialIds.size(); i++) {
            jdbc.update("INSERT INTO bom_chitiet (mabom, manvl, soluong_dinhmuc, tyle_haohut) VALUES (?, ?, ?, ?)",
                    id, materialIds.get(i), quantities.get(i), wastePercentages.get(i));
        }
        return id;
    }

    @Transactional
    public void disable(long id) {
        int count = jdbc.update("UPDATE bom SET trangthai = 'NGUNG_SU_DUNG' WHERE mabom = ? AND trangthai = 'HIEU_LUC'", id);
        if (count == 0) throw new IllegalArgumentException("BOM không tồn tại hoặc đã ngừng sử dụng.");
    }
}
