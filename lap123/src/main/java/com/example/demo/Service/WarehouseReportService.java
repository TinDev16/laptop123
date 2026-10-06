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

    // =====================================================
    // DANH SÁCH CHỨNG TỪ
    //
    // 1 dòng = 1 chứng từ / 1 giao dịch
    //
    // Không lấy từng dòng lich_su_kho riêng lẻ nữa.
    // Group theo:
    //     bang_tham_chieu
    //     ma_tham_chieu
    // =====================================================

    public List<Map<String, Object>> history(
            LocalDate from,
            LocalDate to,
            String kind,
            String type) {

        StringBuilder sql = new StringBuilder("""
            SELECT
                MIN(h.id) AS malichsu,
                h.bang_tham_chieu,
                h.ma_tham_chieu AS ma_chung_tu,
                MIN(h.thoi_gian) AS ngay_giao_dich,

                MIN(h.loai_hang) AS loai_hang,
                MIN(h.loai_giaodich) AS loai_giao_dich,

                COUNT(*) AS so_dong,

                COALESCE(SUM(h.soluong), 0) AS tong_so_luong,

                MIN(h.nguoi_thuchien) AS ma_nguoi_thuc_hien,
                MIN(v.hoten) AS nguoithuchien

            FROM lich_su_kho h

            LEFT JOIN nhanvien v
                ON v.manv = h.nguoi_thuchien

            WHERE 1 = 1
            """);

        List<Object> args = new ArrayList<>();

        // -------------------------------------------------
        // LỌC NGÀY
        // -------------------------------------------------

        if (from != null) {
            sql.append(" AND h.thoi_gian >= ?");
            args.add(from.atStartOfDay());
        }

        if (to != null) {
            sql.append(" AND h.thoi_gian < ?");
            args.add(to.plusDays(1).atStartOfDay());
        }

        // -------------------------------------------------
        // LỌC LOẠI HÀNG
        // -------------------------------------------------

        if (kind != null && !kind.isBlank()) {
            sql.append(" AND h.loai_hang = ?");
            args.add(kind);
        }

        // -------------------------------------------------
        // LỌC LOẠI GIAO DỊCH
        // -------------------------------------------------

        if (type != null && !type.isBlank()) {
            sql.append(" AND h.loai_giaodich = ?");
            args.add(type);
        }

        // -------------------------------------------------
        // GROUP 1 CHỨNG TỪ
        // -------------------------------------------------

        sql.append("""
            GROUP BY
                h.bang_tham_chieu,
                h.ma_tham_chieu

            ORDER BY
                MIN(h.thoi_gian) DESC,
                MIN(h.id) DESC

            LIMIT 500
            """);

        return jdbc.queryForList(
                sql.toString(),
                args.toArray()
        );
    }


    // =====================================================
    // CHI TIẾT 1 CHỨNG TỪ
    //
    // bang_tham_chieu:
    //     phieunhap_nvl
    //     phieuxuat_nvl
    //     ketqua_sanxuat
    //     phieuxuat_thanhpham
    //
    // ma_tham_chieu:
    //     ID của chứng từ
    // =====================================================

    public List<Map<String, Object>> historyDetail(
            String sourceTable,
            long documentId) {

        // -------------------------------------------------
        // Chỉ cho phép những bảng mà hệ thống thực sự dùng
        // ghi lịch sử.
        // -------------------------------------------------

        if (!isAllowedSourceTable(sourceTable)) {
            throw new IllegalArgumentException(
                    "Loại chứng từ không hợp lệ."
            );
        }

        String sql = """
            SELECT
                h.id AS malichsu,
                h.loai_hang,
                h.ma_hang,
                h.loai_giaodich AS loai_giao_dich,
                h.soluong AS so_luong,
                h.don_gia AS dongia,
                h.thoi_gian AS ngay_giao_dich,
                h.ma_tham_chieu AS ma_chung_tu,
                h.bang_tham_chieu,
                h.ghichu,

                COALESCE(
                    n.tennvl,
                    s.tensp
                ) AS tenhang,

                v.hoten AS nguoithuchien

            FROM lich_su_kho h

            LEFT JOIN nguyenvatlieu n
                ON h.loai_hang = 'NVL'
                AND n.manvl = h.ma_hang

            LEFT JOIN sanpham s
                ON h.loai_hang = 'THANH_PHAM'
                AND s.masp = h.ma_hang

            LEFT JOIN nhanvien v
                ON v.manv = h.nguoi_thuchien

            WHERE h.bang_tham_chieu = ?
              AND h.ma_tham_chieu = ?

            ORDER BY
                h.id ASC
            """;

        return jdbc.queryForList(
                sql,
                sourceTable,
                documentId
        );
    }


    // =====================================================
    // VALIDATE BẢNG CHỨNG TỪ
    // =====================================================

    private boolean isAllowedSourceTable(String sourceTable) {

        return switch (sourceTable) {

            case "phieunhap_nvl",
                 "phieuxuat_nvl",
                 "ketqua_sanxuat",
                 "phieuxuat_thanhpham" -> true;

            default -> false;
        };
    }


    // =====================================================
    // TỔNG HỢP BÁO CÁO
    // =====================================================

    public Map<String, BigDecimal> totals(
            LocalDate from,
            LocalDate to) {

        String sql = """
            SELECT
                loai_giaodich,
                COALESCE(SUM(soluong), 0) AS total

            FROM lich_su_kho

            WHERE thoi_gian >= ?
              AND thoi_gian < ?

            GROUP BY loai_giaodich
            """;

        Map<String, BigDecimal> totals = new HashMap<>();

        for (Map<String, Object> row :
                jdbc.queryForList(
                        sql,
                        from.atStartOfDay(),
                        to.plusDays(1).atStartOfDay()
                )) {

            Object value = row.get("total");

            BigDecimal total;

            if (value instanceof BigDecimal bd) {
                total = bd;
            } else {
                total = new BigDecimal(
                        String.valueOf(value)
                );
            }

            totals.put(
                    String.valueOf(
                            row.get("loai_giaodich")
                    ),
                    total
            );
        }

        return totals;
    }
}