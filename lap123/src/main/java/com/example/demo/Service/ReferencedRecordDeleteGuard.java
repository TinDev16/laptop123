package com.example.demo.Service;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReferencedRecordDeleteGuard {
    private final JdbcTemplate jdbcTemplate;

    public ReferencedRecordDeleteGuard(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void checkUnreferenced(String table, int id) {
        if (!"ncc".equals(table) && !"nhanvien".equals(table)) {
            throw new IllegalArgumentException("Unsupported table");
        }
        List<Reference> references = jdbcTemplate.query(
                "SELECT TABLE_NAME, COLUMN_NAME FROM information_schema.KEY_COLUMN_USAGE "
                + "WHERE REFERENCED_TABLE_SCHEMA = DATABASE() AND REFERENCED_TABLE_NAME = ?",
                (rs, row) -> new Reference(rs.getString(1), rs.getString(2)), table);

        for (Reference reference : references) {
            if (!reference.table.matches("[A-Za-z0-9_]+") || !reference.column.matches("[A-Za-z0-9_]+")) {
                throw new IllegalStateException("Không thể kiểm tra liên kết dữ liệu.");
            }
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `" + reference.table + "` WHERE `" + reference.column + "` = ?",
                    Integer.class, id);
            if (count != null && count > 0) {
                throw new IllegalStateException("Bản ghi đang được sử dụng trong dữ liệu nghiệp vụ hoặc tài khoản. Hãy giữ ở trạng thái lưu trữ.");
            }
        }
    }

    private record Reference(String table, String column) {}
}
