package com.example.demo.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

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

                GROUP_CONCAT(
                    DISTINCT COALESCE(n.tennvl, s.tensp)
                    ORDER BY COALESCE(n.tennvl, s.tensp)
                    SEPARATOR ', '
                ) AS hanghoa,

                MIN(h.nguoi_thuchien) AS ma_nguoi_thuc_hien,
                MIN(v.hoten) AS nguoithuchien,
                MIN(worker.hoten) AS nguoisanxuat

            FROM lich_su_kho h

            LEFT JOIN nhanvien v
                ON v.manv = h.nguoi_thuchien

            LEFT JOIN nguyenvatlieu n
                ON h.loai_hang = 'NVL'
                AND n.manvl = h.ma_hang

            LEFT JOIN sanpham s
                ON h.loai_hang = 'THANH_PHAM'
                AND s.masp = h.ma_hang

            LEFT JOIN ketqua_sanxuat k
                ON h.bang_tham_chieu = 'ketqua_sanxuat'
                AND k.makqsx = h.ma_tham_chieu

            LEFT JOIN nhanvien worker
                ON worker.manv = k.nguoithuchien

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

                COALESCE(n.tennvl, s.tensp) AS tenhang,
                COALESCE(n.donvitinh, s.donvitinh, 'Cái') AS donvitinh,
                v.hoten AS nguoithuchien,

                COALESCE(k.trangthai, pn.trangthai, px.trangthai, pxtp.trangthai)
                    AS trangthai_chungtu,
                COALESCE(k.ghichu, pn.ghichu, px.ghichu, pxtp.ghichu, h.ghichu)
                    AS ghichu_chungtu,
                COALESCE(px.lydo, pxtp.lydo) AS lydo,

                worker.hoten AS nguoisanxuat,
                COALESCE(pn_creator.hoten, px_creator.hoten, pxtp_creator.hoten)
                    AS nguoilap,
                COALESCE(px_approver.hoten, pxtp_approver.hoten)
                    AS nguoiduyet,

                supplier.tenncc AS nhacungcap,
                customer.hoten AS khachhang,
                pxtp.diachi_giaohang,

                COALESCE(k.maycsx, px.maycsx) AS mayeucau,
                COALESCE(dk.madot_code, dx.madot_code) AS madot_code,
                COALESCE(dk.tendot, dx.tendot) AS tendot,
                b.mabom,
                b.phienban AS phienban_bom,

                COALESCE(k.ngayhoanthanh, pn.ngaylap, px.ngaylap, pxtp.ngaylap)
                    AS ngaylap_chungtu,
                COALESCE(px.ngayduyet, pxtp.ngayduyet) AS ngayduyet_chungtu,
                k.soluongloi AS soluongloi

            FROM lich_su_kho h

            LEFT JOIN nguyenvatlieu n
                ON h.loai_hang = 'NVL'
                AND n.manvl = h.ma_hang

            LEFT JOIN sanpham s
                ON h.loai_hang = 'THANH_PHAM'
                AND s.masp = h.ma_hang

            LEFT JOIN nhanvien v
                ON v.manv = h.nguoi_thuchien

            LEFT JOIN ketqua_sanxuat k
                ON h.bang_tham_chieu = 'ketqua_sanxuat'
                AND k.makqsx = h.ma_tham_chieu

            LEFT JOIN yeucausanxuat yk
                ON yk.maycsx = k.maycsx

            LEFT JOIN dot_san_xuat dk
                ON dk.madot = yk.madot

            LEFT JOIN nhanvien worker
                ON worker.manv = k.nguoithuchien

            LEFT JOIN phieunhap_nvl pn
                ON h.bang_tham_chieu = 'phieunhap_nvl'
                AND pn.mapn = h.ma_tham_chieu

            LEFT JOIN ncc supplier
                ON supplier.mancc = pn.mancc

            LEFT JOIN nhanvien pn_creator
                ON pn_creator.manv = pn.nguoilap

            LEFT JOIN phieuxuat_nvl px
                ON h.bang_tham_chieu = 'phieuxuat_nvl'
                AND px.mapx = h.ma_tham_chieu

            LEFT JOIN yeucausanxuat yx
                ON yx.maycsx = px.maycsx

            LEFT JOIN dot_san_xuat dx
                ON dx.madot = yx.madot

            LEFT JOIN nhanvien px_creator
                ON px_creator.manv = px.nguoilap

            LEFT JOIN nhanvien px_approver
                ON px_approver.manv = px.nguoiduyet

            LEFT JOIN phieuxuat_thanhpham pxtp
                ON h.bang_tham_chieu = 'phieuxuat_thanhpham'
                AND pxtp.mapxtp = h.ma_tham_chieu

            LEFT JOIN ct_phieuxuat_thanhpham ctpxtp
                ON ctpxtp.mapxtp = pxtp.mapxtp
                AND ctpxtp.masp = h.ma_hang

            LEFT JOIN khachhang customer
                ON customer.makh = pxtp.makh

            LEFT JOIN nhanvien pxtp_creator
                ON pxtp_creator.manv = pxtp.nguoilap

            LEFT JOIN nhanvien pxtp_approver
                ON pxtp_approver.manv = pxtp.nguoiduyet

            LEFT JOIN bom b
                ON b.mabom = COALESCE(ctpxtp.mabom, yk.mabom, yx.mabom)

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


// =====================================================
// TỒN KHO THEO KHOẢNG THỜI GIAN
//
// Tồn kho được tính tại cuối ngày "to".
//
// Ví dụ:
// - Tháng 10 -> tồn tại 31/10
// - Quý 3    -> tồn tại 30/09
// - Năm 2026 -> tồn tại 31/12
// =====================================================

    public Map<String, BigDecimal> stockAt(LocalDate to) {

        Map<String, BigDecimal> stock = new HashMap<>();

        LocalDateTime end =
                to.plusDays(1).atStartOfDay();


        // =====================================================
        // 1. TỒN NVL HIỆN TẠI Ở KHO TỔNG
        // =====================================================

        String currentNvlSql = """
        SELECT COALESCE(
            SUM(soluongton),
            0
        )
        FROM nguyenvatlieu
        """;

        BigDecimal currentNvl =
                jdbc.queryForObject(
                        currentNvlSql,
                        BigDecimal.class
                );

        if (currentNvl == null) {
            currentNvl = BigDecimal.ZERO;
        }


        // =====================================================
        // 2. GIAO DỊCH NVL SAU KỲ
        //
        // NHAP_NVL  -> kho tổng tăng
        // XUAT_NVL  -> kho tổng giảm
        //
        // Đảo ngược:
        //
        // tồn cuối kỳ
        // = tồn hiện tại
        // - nhập sau kỳ
        // + xuất sau kỳ
        // =====================================================

        String nvlAfterSql = """
        SELECT COALESCE(
            SUM(
                CASE
                    WHEN loai_giaodich = 'NHAP_NVL'
                        THEN soluong

                    WHEN loai_giaodich = 'XUAT_NVL'
                        THEN -soluong

                    ELSE 0
                END
            ),
            0
        )

        FROM lich_su_kho

        WHERE thoi_gian >= ?

          AND loai_giaodich IN (
              'NHAP_NVL',
              'XUAT_NVL'
          )
        """;

        BigDecimal nvlAfter =
                jdbc.queryForObject(
                        nvlAfterSql,
                        BigDecimal.class,
                        end
                );

        if (nvlAfter == null) {
            nvlAfter = BigDecimal.ZERO;
        }

        BigDecimal nvlStock =
                currentNvl.subtract(nvlAfter);


        // =====================================================
        // 3. TỒN THÀNH PHẨM HIỆN TẠI Ở KHO TỔNG
        // =====================================================

        String currentProductSql = """
        SELECT COALESCE(
            SUM(soluongton),
            0
        )
        FROM sanpham
        """;

        BigDecimal currentProduct =
                jdbc.queryForObject(
                        currentProductSql,
                        BigDecimal.class
                );

        if (currentProduct == null) {
            currentProduct = BigDecimal.ZERO;
        }


        // =====================================================
        // 4. GIAO DỊCH THÀNH PHẨM SAU KỲ
        //
        // NHAP_THANH_PHAM -> kho tổng tăng
        // XUAT_THANH_PHAM -> kho tổng giảm
        // =====================================================

        String productAfterSql = """
        SELECT COALESCE(
            SUM(
                CASE
                    WHEN loai_giaodich = 'NHAP_THANH_PHAM'
                        THEN soluong

                    WHEN loai_giaodich = 'XUAT_THANH_PHAM'
                        THEN -soluong

                    ELSE 0
                END
            ),
            0
        )

        FROM lich_su_kho

        WHERE thoi_gian >= ?

          AND loai_giaodich IN (
              'NHAP_THANH_PHAM',
              'XUAT_THANH_PHAM'
          )
        """;

        BigDecimal productAfter =
                jdbc.queryForObject(
                        productAfterSql,
                        BigDecimal.class,
                        end
                );

        if (productAfter == null) {
            productAfter = BigDecimal.ZERO;
        }

        BigDecimal productStock =
                currentProduct.subtract(productAfter);


        // =====================================================
        // 5. TRẢ KẾT QUẢ
        // =====================================================

        stock.put(
                "NVL_TON",
                nvlStock
        );

        stock.put(
                "THANH_PHAM_TON",
                productStock
        );

        return stock;
    }

    // =====================================================
    // HELPER CHUYỂN ĐỔI SỐ AN TOÀN
    // =====================================================
    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal bd) return bd;
        if (obj instanceof Number num) return new BigDecimal(num.toString());
        try {
            return new BigDecimal(obj.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    // =====================================================
    // 6. BÁO CÁO XUẤT - NHẬP - TỒN & GIÁ TRỊ VỐN CHI TIẾT THEO TỪNG SKU
    // =====================================================
    public List<Map<String, Object>> getInventoryDetails(
            LocalDate from,
            LocalDate to,
            String kind
    ) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();
        List<Map<String, Object>> list = new ArrayList<>();

        boolean includeNvl = kind == null || kind.isBlank() || "ALL".equalsIgnoreCase(kind) || "NVL".equalsIgnoreCase(kind);
        boolean includeTp = kind == null || kind.isBlank() || "ALL".equalsIgnoreCase(kind) || "THANH_PHAM".equalsIgnoreCase(kind);

        if (includeNvl) {
            String sqlNvl = """
                SELECT 
                    'NVL' AS loai_hang,
                    n.manvl AS ma_id,
                    n.manvl_code AS ma_code,
                    n.tennvl AS ten_hang,
                    n.donvitinh,
                    n.dongianhap AS don_gia,
                    n.soluongton AS ton_hien_tai,
                    n.ton_toithieu,
                    COALESCE(after_period.net_after, 0) AS net_after,
                    COALESCE(in_period.tong_nhap, 0) AS nhap_trong_ky,
                    COALESCE(in_period.tong_xuat, 0) AS xuat_trong_ky
                FROM nguyenvatlieu n
                LEFT JOIN (
                    SELECT 
                        ma_hang,
                        SUM(CASE WHEN loai_giaodich = 'NHAP_NVL' THEN soluong WHEN loai_giaodich = 'XUAT_NVL' THEN -soluong ELSE 0 END) AS net_after
                    FROM lich_su_kho
                    WHERE loai_hang = 'NVL' AND thoi_gian >= ?
                    GROUP BY ma_hang
                ) after_period ON after_period.ma_hang = n.manvl
                LEFT JOIN (
                    SELECT 
                        ma_hang,
                        SUM(CASE WHEN loai_giaodich = 'NHAP_NVL' THEN soluong ELSE 0 END) AS tong_nhap,
                        SUM(CASE WHEN loai_giaodich = 'XUAT_NVL' THEN soluong ELSE 0 END) AS tong_xuat
                    FROM lich_su_kho
                    WHERE loai_hang = 'NVL' AND thoi_gian >= ? AND thoi_gian < ?
                    GROUP BY ma_hang
                ) in_period ON in_period.ma_hang = n.manvl
                ORDER BY n.manvl ASC
                """;

            for (Map<String, Object> row : jdbc.queryForList(sqlNvl, end, start, end)) {
                BigDecimal current = toBigDecimal(row.get("ton_hien_tai"));
                BigDecimal netAfter = toBigDecimal(row.get("net_after"));
                BigDecimal nhap = toBigDecimal(row.get("nhap_trong_ky"));
                BigDecimal xuat = toBigDecimal(row.get("xuat_trong_ky"));
                BigDecimal donGia = toBigDecimal(row.get("don_gia"));
                BigDecimal tonToiThieu = toBigDecimal(row.get("ton_toithieu"));

                BigDecimal tonCuoi = current.subtract(netAfter);
                BigDecimal tonDau = tonCuoi.subtract(nhap).add(xuat);
                BigDecimal giaTriTon = tonCuoi.multiply(donGia);

                Map<String, Object> item = new HashMap<>(row);
                item.put("ton_dau_ky", tonDau);
                item.put("nhap_trong_ky", nhap);
                item.put("xuat_trong_ky", xuat);
                item.put("ton_cuoi_ky", tonCuoi);
                item.put("gia_tri_ton", giaTriTon);

                String canhBao = "AN_TOAN";
                if (tonCuoi.compareTo(BigDecimal.ZERO) <= 0) {
                    canhBao = "CANH_BAO_HET";
                } else if (tonToiThieu.compareTo(BigDecimal.ZERO) > 0 && tonCuoi.compareTo(tonToiThieu) <= 0) {
                    canhBao = "CANH_BAO_THAP";
                }
                item.put("canh_bao", canhBao);

                list.add(item);
            }
        }

        if (includeTp) {
            String sqlTp = """
                SELECT 
                    'THANH_PHAM' AS loai_hang,
                    s.masp AS ma_id,
                    CONCAT('LAPTOP-', LPAD(s.masp, 3, '0')) AS ma_code,
                    s.tensp AS ten_hang,
                    s.donvitinh,
                    s.giaban AS don_gia,
                    s.soluongton AS ton_hien_tai,
                    0 AS ton_toithieu,
                    COALESCE(after_period.net_after, 0) AS net_after,
                    COALESCE(in_period.tong_nhap, 0) AS nhap_trong_ky,
                    COALESCE(in_period.tong_xuat, 0) AS xuat_trong_ky
                FROM sanpham s
                LEFT JOIN (
                    SELECT 
                        ma_hang,
                        SUM(CASE WHEN loai_giaodich = 'NHAP_THANH_PHAM' THEN soluong WHEN loai_giaodich = 'XUAT_THANH_PHAM' THEN -soluong ELSE 0 END) AS net_after
                    FROM lich_su_kho
                    WHERE loai_hang = 'THANH_PHAM' AND thoi_gian >= ?
                    GROUP BY ma_hang
                ) after_period ON after_period.ma_hang = s.masp
                LEFT JOIN (
                    SELECT 
                        ma_hang,
                        SUM(CASE WHEN loai_giaodich = 'NHAP_THANH_PHAM' THEN soluong ELSE 0 END) AS tong_nhap,
                        SUM(CASE WHEN loai_giaodich = 'XUAT_THANH_PHAM' THEN soluong ELSE 0 END) AS tong_xuat
                    FROM lich_su_kho
                    WHERE loai_hang = 'THANH_PHAM' AND thoi_gian >= ? AND thoi_gian < ?
                    GROUP BY ma_hang
                ) in_period ON in_period.ma_hang = s.masp
                ORDER BY s.masp ASC
                """;

            for (Map<String, Object> row : jdbc.queryForList(sqlTp, end, start, end)) {
                BigDecimal current = toBigDecimal(row.get("ton_hien_tai"));
                BigDecimal netAfter = toBigDecimal(row.get("net_after"));
                BigDecimal nhap = toBigDecimal(row.get("nhap_trong_ky"));
                BigDecimal xuat = toBigDecimal(row.get("xuat_trong_ky"));
                BigDecimal donGia = toBigDecimal(row.get("don_gia"));

                BigDecimal tonCuoi = current.subtract(netAfter);
                BigDecimal tonDau = tonCuoi.subtract(nhap).add(xuat);
                BigDecimal giaTriTon = tonCuoi.multiply(donGia);

                Map<String, Object> item = new HashMap<>(row);
                item.put("ton_dau_ky", tonDau);
                item.put("nhap_trong_ky", nhap);
                item.put("xuat_trong_ky", xuat);
                item.put("ton_cuoi_ky", tonCuoi);
                item.put("gia_tri_ton", giaTriTon);

                String canhBao = "AN_TOAN";
                if (tonCuoi.compareTo(BigDecimal.ZERO) <= 0) {
                    canhBao = "CANH_BAO_HET";
                }
                item.put("canh_bao", canhBao);

                list.add(item);
            }
        }

        return list;
    }

    // =====================================================
    // TỔNG HỢP GIÁ TRỊ VỐN VÀ SỐ LƯỢNG KHO
    // =====================================================
    public Map<String, Object> getInventorySummary(List<Map<String, Object>> details) {
        BigDecimal tongNvlDau = BigDecimal.ZERO;
        BigDecimal tongNvlNhap = BigDecimal.ZERO;
        BigDecimal tongNvlXuat = BigDecimal.ZERO;
        BigDecimal tongNvlCuoi = BigDecimal.ZERO;
        BigDecimal tongGtNvl = BigDecimal.ZERO;

        BigDecimal tongTpDau = BigDecimal.ZERO;
        BigDecimal tongTpNhap = BigDecimal.ZERO;
        BigDecimal tongTpXuat = BigDecimal.ZERO;
        BigDecimal tongTpCuoi = BigDecimal.ZERO;
        BigDecimal tongGtTp = BigDecimal.ZERO;

        int soCanhBaoNvl = 0;

        for (Map<String, Object> item : details) {
            String loai = (String) item.get("loai_hang");
            BigDecimal dau = toBigDecimal(item.get("ton_dau_ky"));
            BigDecimal nhap = toBigDecimal(item.get("nhap_trong_ky"));
            BigDecimal xuat = toBigDecimal(item.get("xuat_trong_ky"));
            BigDecimal cuoi = toBigDecimal(item.get("ton_cuoi_ky"));
            BigDecimal gt = toBigDecimal(item.get("gia_tri_ton"));
            String canhBao = (String) item.get("canh_bao");

            if ("NVL".equalsIgnoreCase(loai)) {
                tongNvlDau = tongNvlDau.add(dau);
                tongNvlNhap = tongNvlNhap.add(nhap);
                tongNvlXuat = tongNvlXuat.add(xuat);
                tongNvlCuoi = tongNvlCuoi.add(cuoi);
                tongGtNvl = tongGtNvl.add(gt);
                if (!"AN_TOAN".equals(canhBao)) {
                    soCanhBaoNvl++;
                }
            } else {
                tongTpDau = tongTpDau.add(dau);
                tongTpNhap = tongTpNhap.add(nhap);
                tongTpXuat = tongTpXuat.add(xuat);
                tongTpCuoi = tongTpCuoi.add(cuoi);
                tongGtTp = tongGtTp.add(gt);
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("tong_nvl_dau", tongNvlDau);
        summary.put("tong_nvl_nhap", tongNvlNhap);
        summary.put("tong_nvl_xuat", tongNvlXuat);
        summary.put("tong_nvl_cuoi", tongNvlCuoi);
        summary.put("tong_gt_nvl", tongGtNvl);

        summary.put("tong_tp_dau", tongTpDau);
        summary.put("tong_tp_nhap", tongTpNhap);
        summary.put("tong_tp_xuat", tongTpXuat);
        summary.put("tong_tp_cuoi", tongTpCuoi);
        summary.put("tong_gt_tp", tongGtTp);

        summary.put("tong_gia_tri_kho", tongGtNvl.add(tongGtTp));
        summary.put("so_canh_bao_nvl", soCanhBaoNvl);

        return summary;
    }

    // =====================================================
    // 7. BÁO CÁO HIỆU SUẤT & CHẤT LƯỢNG SẢN XUẤT (OTIF, TỶ LỆ LỖI/PHẾ PHẨM)
    // =====================================================
    public Map<String, Object> getProductionPerformance(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        String sql = """
            SELECT 
                y.maycsx,
                y.madot,
                COALESCE(d.madot_code, CONCAT('YCSX-', y.maycsx)) AS madot_code,
                COALESCE(d.tendot, 'Đợt sản xuất') AS tendot,
                s.masp,
                s.tensp,
                y.nguoiphutrach,
                COALESCE(n.hoten, 'Chưa chỉ định') AS hoten_phutrach,
                y.soluongyeucau,
                y.soluonghoanthanh,
                COALESCE(kq.tongloi, 0) AS soluongloi,
                y.ngayyeucau,
                y.hanhoanthanh,
                kq.ngayhoanthanhcuoicung,
                y.trangthai
            FROM yeucausanxuat y
            JOIN sanpham s ON s.masp = y.masp
            LEFT JOIN nhanvien n ON n.manv = y.nguoiphutrach
            LEFT JOIN dot_san_xuat d ON d.madot = y.madot
            LEFT JOIN (
                SELECT 
                    maycsx, 
                    SUM(soluongloi) AS tongloi,
                    MAX(ngayhoanthanh) AS ngayhoanthanhcuoicung
                FROM ketqua_sanxuat
                WHERE trangthai IN ('DA_NHAP_KHO', 'CHO_XAC_NHAN')
                GROUP BY maycsx
            ) kq ON kq.maycsx = y.maycsx
            WHERE y.ngayyeucau >= ? AND y.ngayyeucau < ?
            ORDER BY y.maycsx DESC
            """;

        List<Map<String, Object>> rows = jdbc.queryForList(sql, start, end);

        int tongLenh = rows.size();
        int hoanThanh = 0;
        int dungHan = 0;
        int treHan = 0;
        int tongSanLuong = 0;
        int tongLoi = 0;

        LocalDate today = LocalDate.now();

        List<Map<String, Object>> orders = new ArrayList<>();
        Map<String, Map<String, Object>> productMap = new HashMap<>();
        Map<String, Map<String, Object>> workerMap = new HashMap<>();

        for (Map<String, Object> r : rows) {
            Map<String, Object> item = new HashMap<>(r);
            int yeuCau = ((Number) r.get("soluongyeucau")).intValue();
            int hThanh = ((Number) r.get("soluonghoanthanh")).intValue();
            int loi = ((Number) r.get("soluongloi")).intValue();
            String tt = (String) r.get("trangthai");
            Object hanObj = r.get("hanhoanthanh");
            Object htCuoiObj = r.get("ngayhoanthanhcuoicung");

            tongSanLuong += hThanh;
            tongLoi += loi;

            double tyLeLoi = (hThanh + loi > 0) ? (loi * 100.0) / (hThanh + loi) : 0.0;
            item.put("ty_le_loi", Math.round(tyLeLoi * 10.0) / 10.0);

            LocalDate han = null;
            if (hanObj instanceof java.sql.Date d) han = d.toLocalDate();
            else if (hanObj instanceof LocalDate ld) han = ld;

            LocalDate ngayHt = null;
            if (htCuoiObj instanceof java.sql.Timestamp ts) ngayHt = ts.toLocalDateTime().toLocalDate();
            else if (htCuoiObj instanceof LocalDateTime ldt) ngayHt = ldt.toLocalDate();

            String danhGia;
            if ("HOAN_THANH".equalsIgnoreCase(tt)) {
                hoanThanh++;
                if (han != null && ngayHt != null) {
                    if (!ngayHt.isAfter(han)) {
                        dungHan++;
                        danhGia = "Đúng hạn";
                    } else {
                        treHan++;
                        danhGia = "Trễ hạn";
                    }
                } else {
                    dungHan++;
                    danhGia = "Đúng hạn";
                }
            } else if ("HUY".equalsIgnoreCase(tt)) {
                danhGia = "Đã hủy";
            } else {
                if (han != null && han.isBefore(today)) {
                    treHan++;
                    danhGia = "Quá hạn";
                } else {
                    danhGia = "Đang tiến hành";
                }
            }
            item.put("danh_gia_tien_do", danhGia);
            orders.add(item);

            // Thống kê theo sản phẩm cho biểu đồ
            String spTen = (String) r.get("tensp");
            if (spTen != null) {
                productMap.putIfAbsent(spTen, new HashMap<>(Map.of("name", spTen, "san_luong", 0, "loi", 0)));
                Map<String, Object> pStat = productMap.get(spTen);
                pStat.put("san_luong", ((Number) pStat.get("san_luong")).intValue() + hThanh);
                pStat.put("loi", ((Number) pStat.get("loi")).intValue() + loi);
            }

            // Thống kê theo nhân sự cho biểu đồ
            String nvTen = (String) r.get("hoten_phutrach");
            if (nvTen != null) {
                workerMap.putIfAbsent(nvTen, new HashMap<>(Map.of("name", nvTen, "san_luong", 0, "loi", 0)));
                Map<String, Object> wStat = workerMap.get(nvTen);
                wStat.put("san_luong", ((Number) wStat.get("san_luong")).intValue() + hThanh);
                wStat.put("loi", ((Number) wStat.get("loi")).intValue() + loi);
            }
        }

        double tyLeOtif = (dungHan + treHan > 0) ? (dungHan * 100.0) / (dungHan + treHan) : (tongLenh > 0 ? 100.0 : 0.0);
        double tyLeLoiToanBo = (tongSanLuong + tongLoi > 0) ? (tongLoi * 100.0) / (tongSanLuong + tongLoi) : 0.0;

        Map<String, Object> result = new HashMap<>();
        result.put("tong_lenh", tongLenh);
        result.put("hoan_thanh", hoanThanh);
        result.put("dung_han", dungHan);
        result.put("tre_han", treHan);
        result.put("ty_le_otif", Math.round(tyLeOtif * 10.0) / 10.0);
        result.put("tong_san_luong", tongSanLuong);
        result.put("tong_loi", tongLoi);
        result.put("ty_le_loi", Math.round(tyLeLoiToanBo * 10.0) / 10.0);
        result.put("orders", orders);
        result.put("topProducts", new ArrayList<>(productMap.values()));
        result.put("workerStats", new ArrayList<>(workerMap.values()));

        return result;
    }

    // =====================================================
    // 8. BÁO CÁO CÂN ĐỐI CUNG - CẦU & THIẾU HỤT VẬT TƯ MRP
    // =====================================================
    public Map<String, Object> getMrpShortageReport() {
        String sql = """
            SELECT
                req.manvl,
                req.manvl_code,
                req.tennvl,
                req.donvitinh,
                req.dongianhap,
                req.lead_time_ngay,
                req.ton_toithieu,
                req.soyeucau,
                req.tongnhucau,
                COALESCE(k_tong.soluongton, 0) AS ton_kho_tong,
                COALESCE(k_sx.soluongton, 0) AS ton_kho_sx,
                (COALESCE(k_tong.soluongton, 0) + COALESCE(k_sx.soluongton, 0)) AS tong_kha_dung,
                GREATEST(
                    req.tongnhucau - (COALESCE(k_tong.soluongton, 0) + COALESCE(k_sx.soluongton, 0)),
                    0
                ) AS soluongthieu
            FROM (
                SELECT
                    n.manvl,
                    n.manvl_code,
                    n.tennvl,
                    n.donvitinh,
                    n.dongianhap,
                    n.lead_time_ngay,
                    n.ton_toithieu,
                    COUNT(DISTINCT y.maycsx) AS soyeucau,
                    SUM(
                        CEIL(
                            GREATEST(y.soluongyeucau - y.soluonghoanthanh, 0)
                            * c.soluong_dinhmuc
                            * (1 + c.tyle_haohut / 100)
                        )
                    ) AS tongnhucau
                FROM yeucausanxuat y
                JOIN bom_chitiet c ON c.mabom = y.mabom
                JOIN nguyenvatlieu n ON n.manvl = c.manvl
                WHERE y.trangthai IN ('DA_GIAO', 'DANG_SAN_XUAT')
                GROUP BY n.manvl, n.manvl_code, n.tennvl, n.donvitinh, n.dongianhap, n.lead_time_ngay, n.ton_toithieu
            ) req
            LEFT JOIN nguyenvatlieu k_tong ON k_tong.manvl = req.manvl
            LEFT JOIN kho_nvl_san_xuat k_sx ON k_sx.manvl = req.manvl
            ORDER BY soluongthieu DESC, req.manvl ASC
            """;

        List<Map<String, Object>> rows = jdbc.queryForList(sql);
        List<Map<String, Object>> items = new ArrayList<>();

        int soLoaiThieu = 0;
        BigDecimal tongChiPhiMua = BigDecimal.ZERO;

        for (Map<String, Object> r : rows) {
            Map<String, Object> item = new HashMap<>(r);
            BigDecimal thieu = toBigDecimal(r.get("soluongthieu"));
            BigDecimal donGia = toBigDecimal(r.get("dongianhap"));
            BigDecimal tonKhoTong = toBigDecimal(r.get("ton_kho_tong"));
            BigDecimal tonToiThieu = toBigDecimal(r.get("ton_toithieu"));

            BigDecimal chiPhi = thieu.multiply(donGia);
            item.put("chi_phi_mua_du_kien", chiPhi);

            String status;
            if (thieu.compareTo(BigDecimal.ZERO) > 0) {
                status = "THIEU_HANG";
                soLoaiThieu++;
                tongChiPhiMua = tongChiPhiMua.add(chiPhi);
            } else if (tonToiThieu.compareTo(BigDecimal.ZERO) > 0 && tonKhoTong.compareTo(tonToiThieu) <= 0) {
                status = "CANH_BAO_TON_THAP";
            } else {
                status = "DU_HANG";
            }
            item.put("trang_thai_cung_ung", status);
            items.add(item);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("items", items);
        res.put("so_loai_thieu", soLoaiThieu);
        res.put("tong_chi_phi_mua", tongChiPhiMua);
        return res;
    }

    // =====================================================
    // 9. BÁO CÁO ĐỐI SOÁT ĐỊNH MỨC BOM & HAO HỤT THỰC TẾ
    // =====================================================
    public List<Map<String, Object>> getBomVarianceReport(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        String sql = """
            SELECT 
                y.maycsx,
                COALESCE(d.madot_code, CONCAT('YCSX-', y.maycsx)) AS madot_code,
                s.tensp,
                b.phienban AS phienban_bom,
                y.soluongyeucau,
                y.soluonghoanthanh,
                y.trangthai,
                n.tennvl,
                n.donvitinh,
                c.soluong_dinhmuc,
                c.tyle_haohut AS tyle_haohut_dinhmuc,
                -- Định mức cho máy đã hoàn thành
                ROUND(y.soluonghoanthanh * c.soluong_dinhmuc, 2) AS dinhmuc_chuan_hoanthanh,
                ROUND(y.soluonghoanthanh * c.soluong_dinhmuc * (1 + c.tyle_haohut / 100), 2) AS dinhmuc_cho_phep_hoanthanh,
                -- Định mức kế hoạch cho toàn bộ yêu cầu
                ROUND(y.soluongyeucau * c.soluong_dinhmuc, 2) AS dinhmuc_chuan_kehoach,
                ROUND(y.soluongyeucau * c.soluong_dinhmuc * (1 + c.tyle_haohut / 100), 2) AS dinhmuc_cho_phep_kehoach,
                COALESCE(px.thuc_xuat, 0) AS thuc_xuat
            FROM yeucausanxuat y
            JOIN sanpham s ON s.masp = y.masp
            JOIN bom b ON b.mabom = y.mabom
            JOIN bom_chitiet c ON c.mabom = y.mabom
            JOIN nguyenvatlieu n ON n.manvl = c.manvl
            LEFT JOIN dot_san_xuat d ON d.madot = y.madot
            LEFT JOIN (
                SELECT 
                    p.maycsx, 
                    cp.manvl, 
                    SUM(cp.soluong) AS thuc_xuat
                FROM phieuxuat_nvl p
                JOIN ct_phieuxuat_nvl cp ON cp.mapx = p.mapx
                WHERE p.trangthai = 'DA_XUAT'
                GROUP BY p.maycsx, cp.manvl
            ) px ON px.maycsx = y.maycsx AND px.manvl = c.manvl
            WHERE y.trangthai <> 'HUY'
              AND (y.soluonghoanthanh > 0 OR COALESCE(px.thuc_xuat, 0) > 0)
              AND y.ngayyeucau >= ? AND y.ngayyeucau < ?
            ORDER BY y.maycsx DESC, n.tennvl ASC
            """;

        List<Map<String, Object>> rows = jdbc.queryForList(sql, start, end);
        Map<Long, Map<String, Object>> orderMap = new java.util.LinkedHashMap<>();

        for (Map<String, Object> r : rows) {
            long orderId = ((Number) r.get("maycsx")).longValue();

            if (!orderMap.containsKey(orderId)) {
                Map<String, Object> orderGroup = new HashMap<>();
                orderGroup.put("maycsx", orderId);
                orderGroup.put("madot_code", r.get("madot_code"));
                orderGroup.put("tensp", r.get("tensp"));
                orderGroup.put("phienban_bom", r.get("phienban_bom"));

                int yeuCau = ((Number) r.get("soluongyeucau")).intValue();
                int hoanThanh = ((Number) r.get("soluonghoanthanh")).intValue();
                String trangThai = (String) r.get("trangthai");
                boolean isCompleted = "HOAN_THANH".equalsIgnoreCase(trangThai) || (hoanThanh >= yeuCau && yeuCau > 0);

                orderGroup.put("soluongyeucau", yeuCau);
                orderGroup.put("soluonghoanthanh", hoanThanh);
                orderGroup.put("trangthai", trangThai);
                orderGroup.put("is_completed", isCompleted);
                orderGroup.put("tiendo_text", hoanThanh + " / " + yeuCau);
                orderGroup.put("has_violation", false);
                orderGroup.put("danh_gia_tong_quan", isCompleted ? "CHUẨN ĐỊNH MỨC" : "ĐANG TIẾN HÀNH (HỢP LÝ)");
                orderGroup.put("materials", new ArrayList<Map<String, Object>>());

                orderMap.put(orderId, orderGroup);
            }

            Map<String, Object> orderGroup = orderMap.get(orderId);
            boolean isCompleted = (boolean) orderGroup.get("is_completed");

            BigDecimal dinhmucChoPhepHoanThanh = toBigDecimal(r.get("dinhmuc_cho_phep_hoanthanh"));
            BigDecimal dinhmucChoPhepKeHoach = toBigDecimal(r.get("dinhmuc_cho_phep_kehoach"));
            BigDecimal thucXuat = toBigDecimal(r.get("thuc_xuat"));

            BigDecimal chenhLech;
            String danhGia;
            BigDecimal dinhMucHienThi;

            if (isCompleted) {
                dinhMucHienThi = dinhmucChoPhepHoanThanh;
                chenhLech = thucXuat.subtract(dinhmucChoPhepHoanThanh);

                if (chenhLech.compareTo(BigDecimal.ZERO) > 0) {
                    danhGia = "VƯỢT ĐỊNH MỨC";
                    orderGroup.put("danh_gia_tong_quan", "VƯỢT ĐỊNH MỨC");
                    orderGroup.put("has_violation", true);
                } else if (chenhLech.compareTo(BigDecimal.ZERO) < 0) {
                    danhGia = "TIẾT KIỆM";
                } else {
                    danhGia = "CHUẨN ĐỊNH MỨC";
                }
            } else {
                dinhMucHienThi = dinhmucChoPhepKeHoach;

                if (thucXuat.compareTo(dinhmucChoPhepKeHoach) > 0) {
                    chenhLech = thucXuat.subtract(dinhmucChoPhepKeHoach);
                    danhGia = "CẤP VƯỢT KẾ HOẠCH";
                    orderGroup.put("danh_gia_tong_quan", "CẤP VƯỢT KẾ HOẠCH");
                    orderGroup.put("has_violation", true);
                } else {
                    chenhLech = BigDecimal.ZERO;
                    danhGia = "ĐANG TIẾN HÀNH (HỢP LÝ)";
                }
            }

            BigDecimal nvlConLaiTrenChuyen = thucXuat.subtract(dinhmucChoPhepHoanThanh);
            if (nvlConLaiTrenChuyen.compareTo(BigDecimal.ZERO) < 0) {
                nvlConLaiTrenChuyen = BigDecimal.ZERO;
            }

            Map<String, Object> mat = new HashMap<>();
            mat.put("tennvl", r.get("tennvl"));
            mat.put("donvitinh", r.get("donvitinh"));
            mat.put("dinhmuc_cho_phep", dinhMucHienThi);
            mat.put("thuc_xuat", thucXuat);
            mat.put("nvl_con_lai_chuyen", nvlConLaiTrenChuyen);
            mat.put("chenh_lech", chenhLech);
            mat.put("danh_gia", danhGia);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> matList = (List<Map<String, Object>>) orderGroup.get("materials");
            matList.add(mat);
        }

        for (Map<String, Object> grp : orderMap.values()) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> matList = (List<Map<String, Object>>) grp.get("materials");
            grp.put("so_linh_kien", matList.size());
        }

        return new ArrayList<>(orderMap.values());
    }

    // =====================================================
    // 10. CHI TIẾT ĐỐI SOÁT ĐỊNH MỨC BOM CHO 1 LỆNH SẢN XUẤT
    // =====================================================
    public Map<String, Object> getBomVarianceDetail(Long maycsx) {
        if (maycsx == null) return null;

        String sql = """
            SELECT 
                y.maycsx,
                COALESCE(d.madot_code, CONCAT('YCSX-', y.maycsx)) AS madot_code,
                s.masp,
                s.tensp,
                b.mabom,
                b.phienban AS phienban_bom,
                y.soluongyeucau,
                y.soluonghoanthanh,
                y.trangthai,
                y.ngayyeucau,
                y.hanhoanthanh,
                nt.hoten AS nguoitao_ten,
                np.hoten AS nguoiphutrach_ten,
                n.manvl,
                n.tennvl,
                n.donvitinh,
                n.dongianhap AS dongia,
                c.soluong_dinhmuc,
                c.tyle_haohut AS tyle_haohut_dinhmuc,
                ROUND(y.soluonghoanthanh * c.soluong_dinhmuc, 2) AS dinhmuc_chuan_hoanthanh,
                ROUND(y.soluonghoanthanh * c.soluong_dinhmuc * (1 + c.tyle_haohut / 100), 2) AS dinhmuc_cho_phep_hoanthanh,
                ROUND(y.soluongyeucau * c.soluong_dinhmuc, 2) AS dinhmuc_chuan_kehoach,
                ROUND(y.soluongyeucau * c.soluong_dinhmuc * (1 + c.tyle_haohut / 100), 2) AS dinhmuc_cho_phep_kehoach,
                COALESCE(px.thuc_xuat, 0) AS thuc_xuat
            FROM yeucausanxuat y
            JOIN sanpham s ON s.masp = y.masp
            JOIN bom b ON b.mabom = y.mabom
            JOIN bom_chitiet c ON c.mabom = y.mabom
            JOIN nguyenvatlieu n ON n.manvl = c.manvl
            LEFT JOIN dot_san_xuat d ON d.madot = y.madot
            LEFT JOIN nhanvien nt ON nt.manv = y.nguoitao
            LEFT JOIN nhanvien np ON np.manv = y.nguoiphutrach
            LEFT JOIN (
                SELECT 
                    p.maycsx, 
                    cp.manvl, 
                    SUM(cp.soluong) AS thuc_xuat
                FROM phieuxuat_nvl p
                JOIN ct_phieuxuat_nvl cp ON cp.mapx = p.mapx
                WHERE p.trangthai = 'DA_XUAT' AND p.maycsx = ?
                GROUP BY p.maycsx, cp.manvl
            ) px ON px.manvl = c.manvl
            WHERE y.maycsx = ?
            ORDER BY n.tennvl ASC
            """;

        List<Map<String, Object>> rows = jdbc.queryForList(sql, maycsx, maycsx);
        if (rows.isEmpty()) {
            return null;
        }

        Map<String, Object> first = rows.get(0);
        Map<String, Object> orderDetail = new HashMap<>();
        orderDetail.put("maycsx", maycsx);
        orderDetail.put("madot_code", first.get("madot_code"));
        orderDetail.put("masp", first.get("masp"));
        orderDetail.put("tensp", first.get("tensp"));
        orderDetail.put("phienban_bom", first.get("phienban_bom"));
        orderDetail.put("ngayyeucau", first.get("ngayyeucau"));
        orderDetail.put("hanhoanthanh", first.get("hanhoanthanh"));
        orderDetail.put("nguoitao_ten", first.get("nguoitao_ten"));
        orderDetail.put("nguoiphutrach_ten", first.get("nguoiphutrach_ten"));

        int yeuCau = ((Number) first.get("soluongyeucau")).intValue();
        int hoanThanh = ((Number) first.get("soluonghoanthanh")).intValue();
        String trangThai = (String) first.get("trangthai");
        boolean isCompleted = "HOAN_THANH".equalsIgnoreCase(trangThai) || (hoanThanh >= yeuCau && yeuCau > 0);

        orderDetail.put("soluongyeucau", yeuCau);
        orderDetail.put("soluonghoanthanh", hoanThanh);
        orderDetail.put("trangthai", trangThai);
        orderDetail.put("is_completed", isCompleted);
        orderDetail.put("tiendo_text", hoanThanh + " / " + yeuCau);

        BigDecimal tongDinhMuc = BigDecimal.ZERO;
        BigDecimal tongThucXuat = BigDecimal.ZERO;
        BigDecimal tongConLai = BigDecimal.ZERO;
        BigDecimal tongTienVatTu = BigDecimal.ZERO;
        int countVuot = 0;
        int countTietKiem = 0;

        List<Map<String, Object>> materials = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            BigDecimal dinhmucChoPhepHoanThanh = toBigDecimal(r.get("dinhmuc_cho_phep_hoanthanh"));
            BigDecimal dinhmucChoPhepKeHoach = toBigDecimal(r.get("dinhmuc_cho_phep_kehoach"));
            BigDecimal thucXuat = toBigDecimal(r.get("thuc_xuat"));
            BigDecimal donGia = toBigDecimal(r.get("dongia"));

            BigDecimal chenhLech;
            String danhGia;
            BigDecimal dinhMucHienThi;

            if (isCompleted) {
                dinhMucHienThi = dinhmucChoPhepHoanThanh;
                chenhLech = thucXuat.subtract(dinhmucChoPhepHoanThanh);
                if (chenhLech.compareTo(BigDecimal.ZERO) > 0) {
                    danhGia = "VƯỢT ĐỊNH MỨC";
                    countVuot++;
                } else if (chenhLech.compareTo(BigDecimal.ZERO) < 0) {
                    danhGia = "TIẾT KIỆM";
                    countTietKiem++;
                } else {
                    danhGia = "CHUẨN ĐỊNH MỨC";
                }
            } else {
                dinhMucHienThi = dinhmucChoPhepKeHoach;
                if (thucXuat.compareTo(dinhmucChoPhepKeHoach) > 0) {
                    chenhLech = thucXuat.subtract(dinhmucChoPhepKeHoach);
                    danhGia = "CẤP VƯỢT KẾ HOẠCH";
                    countVuot++;
                } else {
                    chenhLech = BigDecimal.ZERO;
                    danhGia = "ĐANG TIẾN HÀNH (HỢP LÝ)";
                }
            }

            BigDecimal nvlConLaiTrenChuyen = thucXuat.subtract(dinhmucChoPhepHoanThanh);
            if (nvlConLaiTrenChuyen.compareTo(BigDecimal.ZERO) < 0) {
                nvlConLaiTrenChuyen = BigDecimal.ZERO;
            }

            tongDinhMuc = tongDinhMuc.add(dinhMucHienThi);
            tongThucXuat = tongThucXuat.add(thucXuat);
            tongConLai = tongConLai.add(nvlConLaiTrenChuyen);
            tongTienVatTu = tongTienVatTu.add(thucXuat.multiply(donGia));

            Map<String, Object> mat = new HashMap<>();
            mat.put("manvl", r.get("manvl"));
            mat.put("tennvl", r.get("tennvl"));
            mat.put("donvitinh", r.get("donvitinh"));
            mat.put("dongia", donGia);
            mat.put("soluong_dinhmuc", toBigDecimal(r.get("soluong_dinhmuc")));
            mat.put("tyle_haohut", toBigDecimal(r.get("tyle_haohut_dinhmuc")));
            mat.put("dinhmuc_cho_phep", dinhMucHienThi);
            mat.put("thuc_xuat", thucXuat);
            mat.put("nvl_con_lai_chuyen", nvlConLaiTrenChuyen);
            mat.put("chenh_lech", chenhLech);
            mat.put("danh_gia", danhGia);
            mat.put("thanh_tien", thucXuat.multiply(donGia));

            materials.add(mat);
        }

        String danhGiaTongQuan;
        if (countVuot > 0) {
            danhGiaTongQuan = isCompleted ? "VƯỢT ĐỊNH MỨC" : "CẤP VƯỢT KẾ HOẠCH";
        } else if (countTietKiem > 0 && isCompleted) {
            danhGiaTongQuan = "TIẾT KIỆM";
        } else if (isCompleted) {
            danhGiaTongQuan = "CHUẨN ĐỊNH MỨC";
        } else {
            danhGiaTongQuan = "ĐANG TIẾN HÀNH (HỢP LÝ)";
        }

        orderDetail.put("danh_gia_tong_quan", danhGiaTongQuan);
        orderDetail.put("materials", materials);
        orderDetail.put("so_linh_kien", materials.size());
        orderDetail.put("tong_dinh_muc", tongDinhMuc);
        orderDetail.put("tong_thuc_xuat", tongThucXuat);
        orderDetail.put("tong_con_lai", tongConLai);
        orderDetail.put("tong_tien_vat_tu", tongTienVatTu);
        orderDetail.put("count_vuot", countVuot);
        orderDetail.put("count_tiet_kiem", countTietKiem);

        // Lấy danh sách phiếu xuất kho đã cấp cho lệnh này
        String pxSql = """
            SELECT 
                p.mapx, 
                p.ngaylap, 
                p.ngayduyet, 
                p.trangthai,
                nl.hoten AS nguoilap_ten,
                nd.hoten AS nguoiduyet_ten,
                COALESCE(SUM(c.soluong), 0) AS tong_soluong
            FROM phieuxuat_nvl p
            LEFT JOIN ct_phieuxuat_nvl c ON c.mapx = p.mapx
            LEFT JOIN nhanvien nl ON nl.manv = p.nguoilap
            LEFT JOIN nhanvien nd ON nd.manv = p.nguoiduyet
            WHERE p.maycsx = ?
            GROUP BY p.mapx, p.ngaylap, p.ngayduyet, p.trangthai, nl.hoten, nd.hoten
            ORDER BY p.mapx DESC
            """;
        List<Map<String, Object>> phieuXuats = jdbc.queryForList(pxSql, maycsx);
        orderDetail.put("phieu_xuats", phieuXuats);

        return orderDetail;
    }
}
