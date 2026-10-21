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
public class WarehouseProductionHistoryService {

    private final JdbcTemplate jdbc;

    public WarehouseProductionHistoryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // =====================================================
    // LỊCH SỬ KHO SẢN XUẤT
    // =====================================================

    public List<Map<String, Object>> history(
            LocalDate from,
            LocalDate to,
            String kind,
            String type) {

        StringBuilder sql = new StringBuilder("""
        WITH h AS (
            SELECT
                id,
                loai_kho,
                loai_hang,
                loai_giao_dich,
                thoi_gian,
                bang_tham_chieu,
                ma_tham_chieu,
                soluong,
                ma_hang,
                nguoi_thuchien,
                NULL AS mabom_ref
            FROM lich_su_kho_san_xuat

            UNION ALL

            SELECT
                l.id,
                'LOI_SAN_XUAT' AS loai_kho,
                'THANH_PHAM' AS loai_hang,
                'NHAP_LOI_SAN_XUAT' AS loai_giao_dich,
                l.thoi_gian,
                'ketqua_sanxuat' AS bang_tham_chieu,
                l.makqsx AS ma_tham_chieu,
                l.soluong,
                l.masp AS ma_hang,
                l.nguoi_thuchien,
                l.mabom AS mabom_ref
            FROM lich_su_kho_loi_san_xuat l
        )

        SELECT
            MIN(h.id) AS malichsu,
            MIN(h.loai_kho) AS loai_kho,
            MIN(h.loai_hang) AS loai_hang,
            h.loai_giao_dich,
            MIN(h.thoi_gian) AS thoi_gian,
            h.bang_tham_chieu,
            h.ma_tham_chieu,
            COUNT(*) AS so_dong,
            COALESCE(SUM(h.soluong), 0) AS tong_so_luong,

            GROUP_CONCAT(
                DISTINCT COALESCE(n.tennvl, s.tensp)
                ORDER BY COALESCE(n.tennvl, s.tensp)
                SEPARATOR ', '
            ) AS hanghoa,

            MIN(v.hoten) AS nguoithuchien,

            MIN(COALESCE(
                gn_worker.hoten,
                k_worker.hoten,
                assignee.hoten
            )) AS nhanviensanxuat,

            MIN(COALESCE(
                px.maycsx,
                gn.maycsx,
                k.maycsx
            )) AS mayeucau,

            MIN(d.madot_code) AS madot_code,
            MIN(d.tendot) AS tendot,
            MIN(b.mabom) AS mabom,
            MIN(b.phienban) AS phienban_bom

        FROM h

        LEFT JOIN nguyenvatlieu n
            ON h.loai_hang = 'NVL'
            AND n.manvl = h.ma_hang

        LEFT JOIN sanpham s
            ON h.loai_hang = 'THANH_PHAM'
            AND s.masp = h.ma_hang

        LEFT JOIN nhanvien v
            ON v.manv = h.nguoi_thuchien

        LEFT JOIN phieuxuat_nvl px
            ON h.bang_tham_chieu = 'phieuxuat_nvl'
            AND px.mapx = h.ma_tham_chieu

        LEFT JOIN ghi_nhan_san_xuat gn
            ON h.bang_tham_chieu = 'ghi_nhan_san_xuat'
            AND gn.id = h.ma_tham_chieu

        LEFT JOIN ketqua_sanxuat k
            ON h.bang_tham_chieu = 'ketqua_sanxuat'
            AND k.makqsx = h.ma_tham_chieu

        LEFT JOIN yeucausanxuat y
            ON y.maycsx = COALESCE(
                px.maycsx,
                gn.maycsx,
                k.maycsx
            )

        LEFT JOIN dot_san_xuat d
            ON d.madot = y.madot

        LEFT JOIN bom b
            ON b.mabom = COALESCE(
                gn.mabom,
                y.mabom,
                h.mabom_ref
            )

        LEFT JOIN nhanvien gn_worker
            ON gn_worker.manv = gn.nguoighi

        LEFT JOIN nhanvien k_worker
            ON k_worker.manv = k.nguoithuchien

        LEFT JOIN nhanvien assignee
            ON assignee.manv = y.nguoiphutrach

        WHERE 1 = 1
        """);

        List<Object> args = new ArrayList<>();

        if (from != null) {
            sql.append(" AND h.thoi_gian >= ?");
            args.add(from.atStartOfDay());
        }

        if (to != null) {
            sql.append(" AND h.thoi_gian < ?");
            args.add(to.plusDays(1).atStartOfDay());
        }

        if (kind != null && !kind.isBlank()) {
            sql.append(" AND h.loai_hang = ?");
            args.add(kind);
        }

        if (type != null && !type.isBlank()) {
            if ("NHAP_THANH_PHAM_SAN_XUAT".equals(type)) {
                sql.append("""
            AND h.loai_giao_dich IN (
                'NHAP_THANH_PHAM_SAN_XUAT',
                'NHAP_LOI_SAN_XUAT'
            )
            """);
            } else {
                sql.append(" AND h.loai_giao_dich = ?");
                args.add(type);
            }
        }

        sql.append("""
        GROUP BY
            h.bang_tham_chieu,
            h.ma_tham_chieu,
            h.loai_giao_dich

        ORDER BY
            MIN(h.thoi_gian) DESC,
            MIN(h.id) DESC

        LIMIT 500
        """);

        return jdbc.queryForList(sql.toString(), args.toArray());
    }


    // =====================================================
    // CHI TIẾT CHỨNG TỪ
    //
    // Ví dụ:
    // phieuxuat_nvl #25
    // ghi_nhan_san_xuat #12
    // ketqua_sanxuat #24
    // =====================================================

    public List<Map<String, Object>> detail(
            String sourceTable,
            long documentId) {

        if (sourceTable == null || sourceTable.isBlank()) {
            throw new IllegalArgumentException(
                    "Không xác định được bảng chứng từ."
            );
        }

        if (documentId <= 0) {
            throw new IllegalArgumentException(
                    "Mã chứng từ không hợp lệ."
            );
        }

        // -------------------------------------------------
        // CHỈ CHO PHÉP CÁC BẢNG MÀ HỆ THỐNG THỰC SỰ SỬ DỤNG
        //
        // Không đưa sourceTable trực tiếp vào SQL nếu
        // không whitelist để tránh SQL injection.
        // -------------------------------------------------

        if (!isAllowedSourceTable(sourceTable)) {
            throw new IllegalArgumentException(
                    "Loại chứng từ không hợp lệ."
            );
        }


        String sql = """
    WITH h AS (
        SELECT
            id,
            loai_kho,
            loai_hang,
            ma_hang,
            loai_giao_dich,
            soluong,
            ton_truoc,
            ton_sau,
            thoi_gian,
            bang_tham_chieu,
            ma_tham_chieu,
            nguoi_thuchien,
            ghichu,
            NULL AS mabom_ref
        FROM lich_su_kho_san_xuat

        UNION ALL

        SELECT
            l.id,
            'LOI_SAN_XUAT' AS loai_kho,
            'THANH_PHAM' AS loai_hang,
            l.masp AS ma_hang,
            'NHAP_LOI_SAN_XUAT' AS loai_giao_dich,
            l.soluong,
            l.ton_truoc,
            l.ton_sau,
            l.thoi_gian,
            'ketqua_sanxuat' AS bang_tham_chieu,
            l.makqsx AS ma_tham_chieu,
            l.nguoi_thuchien,
            l.lydo AS ghichu,
            l.mabom AS mabom_ref
        FROM lich_su_kho_loi_san_xuat l
    )

    SELECT
        h.id AS malichsu,
        h.loai_kho,
        h.loai_hang,
        h.ma_hang,
        h.loai_giao_dich,
        h.soluong,
        h.ton_truoc,
        h.ton_sau,
        h.thoi_gian,
        h.bang_tham_chieu,
        h.ma_tham_chieu,
        h.nguoi_thuchien,
        h.ghichu,

        COALESCE(n.tennvl, s.tensp) AS tenhang,
        COALESCE(n.donvitinh, s.donvitinh, 'Cái') AS donvitinh,
        v.hoten AS nguoithuchien,

        COALESCE(k.trangthai, px.trangthai) AS trangthai_chungtu,

        COALESCE(
            gn.ghichu,
            k.ghichu,
            px.ghichu,
            h.ghichu
        ) AS ghichu_chungtu,

        COALESCE(px.lydo, h.ghichu) AS lydo,

        COALESCE(
            gn_worker.hoten,
            k_worker.hoten,
            assignee.hoten
        ) AS nhanviensanxuat,

        requester.hoten AS nguoidenghi,
        approver.hoten AS nguoiduyet,

        COALESCE(px.maycsx, gn.maycsx, k.maycsx) AS mayeucau,

        d.madot_code,
        d.tendot,

        COALESCE(gn.mabom, y.mabom, h.mabom_ref) AS mabom,
        b.phienban AS phienban_bom,

        product.tensp AS thanhpham_sanxuat,

        COALESCE(
            px.ngaylap,
            gn.ngayghi,
            k.ngayhoanthanh
        ) AS ngaylap_chungtu,

        px.ngayduyet AS ngayduyet_chungtu,
        k.soluongloi AS soluongloi

    FROM h

    LEFT JOIN nguyenvatlieu n
        ON h.loai_hang = 'NVL'
        AND n.manvl = h.ma_hang

    LEFT JOIN sanpham s
        ON h.loai_hang = 'THANH_PHAM'
        AND s.masp = h.ma_hang

    LEFT JOIN nhanvien v
        ON v.manv = h.nguoi_thuchien

    LEFT JOIN phieuxuat_nvl px
        ON h.bang_tham_chieu = 'phieuxuat_nvl'
        AND px.mapx = h.ma_tham_chieu

    LEFT JOIN ghi_nhan_san_xuat gn
        ON h.bang_tham_chieu = 'ghi_nhan_san_xuat'
        AND gn.id = h.ma_tham_chieu

    LEFT JOIN ketqua_sanxuat k
        ON h.bang_tham_chieu = 'ketqua_sanxuat'
        AND k.makqsx = h.ma_tham_chieu

    LEFT JOIN yeucausanxuat y
        ON y.maycsx = COALESCE(
            px.maycsx,
            gn.maycsx,
            k.maycsx
        )

    LEFT JOIN dot_san_xuat d
        ON d.madot = y.madot

    LEFT JOIN bom b
        ON b.mabom = COALESCE(
            gn.mabom,
            y.mabom,
            h.mabom_ref
        )

    LEFT JOIN sanpham product
        ON product.masp = COALESCE(
            gn.masp,
            y.masp,
            h.ma_hang
        )

    LEFT JOIN nhanvien gn_worker
        ON gn_worker.manv = gn.nguoighi

    LEFT JOIN nhanvien k_worker
        ON k_worker.manv = k.nguoithuchien

    LEFT JOIN nhanvien assignee
        ON assignee.manv = y.nguoiphutrach

    LEFT JOIN nhanvien requester
        ON requester.manv = px.nguoilap

    LEFT JOIN nhanvien approver
        ON approver.manv = px.nguoiduyet

WHERE h.bang_tham_chieu = ?
  AND h.ma_tham_chieu = ?
  AND (
      h.loai_kho = 'LOI_SAN_XUAT'
      OR NOT EXISTS (
          SELECT 1
          FROM lich_su_kho_loi_san_xuat l
          WHERE l.makqsx = h.ma_tham_chieu
            AND h.bang_tham_chieu = 'ketqua_sanxuat'
      )
  )

ORDER BY h.id ASC
    """;


        return jdbc.queryForList(
                sql,
                sourceTable,
                documentId
        );
    }


    // =====================================================
    // LẤY THÔNG TIN CHỨNG TỪ
    // =====================================================

    public Map<String, Object> documentInfo(
            String sourceTable,
            long documentId) {

        if (!isAllowedSourceTable(sourceTable)) {
            throw new IllegalArgumentException(
                    "Loại chứng từ không hợp lệ."
            );
        }


        String sql = """
    WITH h AS (
        SELECT
            thoi_gian,
            nguoi_thuchien,
            bang_tham_chieu,
            ma_tham_chieu
        FROM lich_su_kho_san_xuat

        UNION ALL

        SELECT
            thoi_gian,
            nguoi_thuchien,
            'ketqua_sanxuat' AS bang_tham_chieu,
            makqsx AS ma_tham_chieu
        FROM lich_su_kho_loi_san_xuat
    )
    SELECT
        MIN(thoi_gian) AS thoi_gian,
        MAX(nguoi_thuchien) AS nguoi_thuchien,
        MAX(bang_tham_chieu) AS bang_tham_chieu,
        MAX(ma_tham_chieu) AS ma_tham_chieu
    FROM h
    WHERE bang_tham_chieu = ?
      AND ma_tham_chieu = ?
    """;


        List<Map<String, Object>> rows =
                jdbc.queryForList(
                        sql,
                        sourceTable,
                        documentId
                );

        if (rows.isEmpty()
                || rows.get(0).get("ma_tham_chieu") == null) {

            throw new IllegalArgumentException(
                    "Không tìm thấy chứng từ."
            );
        }

        return rows.get(0);
    }


    // =====================================================
    // KIỂM TRA BẢNG CHỨNG TỪ ĐƯỢC PHÉP
    // =====================================================

    private boolean isAllowedSourceTable(
            String sourceTable) {

        return switch (sourceTable) {

            case "phieuxuat_nvl",
                 "ghi_nhan_san_xuat",
                 "ketqua_sanxuat" -> true;

            default -> false;
        };
    }


    // =====================================================
    // TỔNG SỐ LƯỢNG THEO LOẠI GIAO DỊCH
    // =====================================================

    public Map<String, BigDecimal> totals(
            LocalDate from,
            LocalDate to) {

        String sql = """
            SELECT
                loai_giao_dich,
                COALESCE(SUM(soluong), 0) AS total
            FROM lich_su_kho_san_xuat
            WHERE thoi_gian >= ?
              AND thoi_gian < ?
            GROUP BY loai_giao_dich
            """;

        Map<String, BigDecimal> result =
                new HashMap<>();

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

            result.put(
                    String.valueOf(
                            row.get("loai_giao_dich")
                    ),
                    total
            );
        }

        return result;
    }
}
