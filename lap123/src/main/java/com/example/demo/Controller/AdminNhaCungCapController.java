package com.example.demo.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/nha-cung-cap")
public class AdminNhaCungCapController {

    private final JdbcTemplate jdbc;

    public AdminNhaCungCapController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String maNcc,
            @RequestParam(defaultValue = "") String tenNcc,
            @RequestParam(defaultValue = "") String diaChi,
            @RequestParam(defaultValue = "") String sdt,
            @RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "") String trangThai,
            @RequestParam(defaultValue = "") String tuSoNvl,
            @RequestParam(defaultValue = "") String denSoNvl,
            @RequestParam(defaultValue = "") String tuTongTien,
            @RequestParam(defaultValue = "") String denTongTien,
            @RequestParam(defaultValue = "mancc") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        final int pageSize = 10;

        // Thống kê tổng quan nhà cung cấp
        Long totalAll = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ncc",
                Long.class);

        Long activeCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ncc WHERE COALESCE(trangthai, 0) = 0",
                Long.class);

        Long inactiveCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ncc WHERE COALESCE(trangthai, 0) <> 0",
                Long.class);

        /*
         * Tổng hợp phiếu nhập đã hoàn tất theo từng nhà cung cấp.
         *
         * tongSoNvl:
         *   Tổng soluong trong chi tiết các phiếu DA_NHAP.
         *
         * tongTienNhap:
         *   Tổng thanhtien trong chi tiết các phiếu DA_NHAP.
         *
         * Tổng hợp riêng trước khi JOIN để không nhân bản dữ liệu NCC.
         */
        String receiptSummaryJoin = """
                LEFT JOIN (
                    SELECT
                        pn.mancc,
                        COALESCE(SUM(ct.soluong), 0) AS tongSoNvl,
                        COALESCE(SUM(ct.thanhtien), 0) AS tongTienNhap
                    FROM phieunhap_nvl pn
                    LEFT JOIN ct_phieunhap_nvl ct
                        ON ct.mapn = pn.mapn
                    WHERE pn.trangthai = 'DA_NHAP'
                    GROUP BY pn.mancc
                ) nhap ON nhap.mancc = n.mancc
                """;

        // Điều kiện tìm kiếm và lọc
        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<Object> args = new ArrayList<>();

        if (!search.isBlank()) {
            where.append("""
                    AND (
                        CAST(n.mancc AS CHAR) LIKE ?
                        OR COALESCE(n.tenncc, '') LIKE ?
                        OR COALESCE(n.sdt, '') LIKE ?
                        OR COALESCE(n.email, '') LIKE ?
                    )
                    """);

            String keyword = "%" + search.trim() + "%";
            args.add(keyword);
            args.add(keyword);
            args.add(keyword);
            args.add(keyword);
        }

        if (!maNcc.isBlank()) {
            where.append(" AND CAST(n.mancc AS CHAR) LIKE ? ");
            args.add("%" + maNcc.trim() + "%");
        }

        if (!tenNcc.isBlank()) {
            where.append(" AND COALESCE(n.tenncc, '') LIKE ? ");
            args.add("%" + tenNcc.trim() + "%");
        }

        if (!diaChi.isBlank()) {
            where.append(" AND COALESCE(n.diachi, '') LIKE ? ");
            args.add("%" + diaChi.trim() + "%");
        }

        if (!sdt.isBlank()) {
            where.append(" AND COALESCE(n.sdt, '') LIKE ? ");
            args.add("%" + sdt.trim() + "%");
        }

        if (!email.isBlank()) {
            where.append(" AND COALESCE(n.email, '') LIKE ? ");
            args.add("%" + email.trim() + "%");
        }

        if ("0".equals(trangThai)) {
            where.append(" AND COALESCE(n.trangthai, 0) = 0 ");
        } else if ("1".equals(trangThai)) {
            where.append(" AND COALESCE(n.trangthai, 0) <> 0 ");
        }

        // Lọc tổng số lượng NVL đã nhập
        BigDecimal minNvl = parseNonNegativeDecimal(tuSoNvl);
        BigDecimal maxNvl = parseNonNegativeDecimal(denSoNvl);

        if (minNvl != null) {
            where.append(" AND COALESCE(nhap.tongSoNvl, 0) >= ? ");
            args.add(minNvl);
        }

        if (maxNvl != null) {
            where.append(" AND COALESCE(nhap.tongSoNvl, 0) <= ? ");
            args.add(maxNvl);
        }

        // Lọc tổng tiền nhập NVL
        BigDecimal minTien = parseNonNegativeDecimal(tuTongTien);
        BigDecimal maxTien = parseNonNegativeDecimal(denTongTien);

        if (minTien != null) {
            where.append(" AND COALESCE(nhap.tongTienNhap, 0) >= ? ");
            args.add(minTien);
        }

        if (maxTien != null) {
            where.append(" AND COALESCE(nhap.tongTienNhap, 0) <= ? ");
            args.add(maxTien);
        }

        // Đếm tổng số kết quả sau lọc
        String countSql = """
                SELECT COUNT(*)
                FROM ncc n
                """ + receiptSummaryJoin + where;

        Long totalItemsResult = jdbc.queryForObject(
                countSql,
                Long.class,
                args.toArray());

        long totalItems = totalItemsResult == null ? 0L : totalItemsResult;

        int totalPages = Math.max(
                1,
                (int) Math.ceil(totalItems / (double) pageSize));

        page = Math.max(1, Math.min(page, totalPages));
        int offset = (page - 1) * pageSize;

        // Chỉ cho phép sắp xếp theo các cột được khai báo ở đây
        Map<String, String> sortColumns = Map.of(
                "mancc", "n.mancc",
                "tenncc", "n.tenncc",
                "diachi", "n.diachi",
                "sdt", "n.sdt",
                "email", "n.email",
                "trangthai", "n.trangthai",
                "tongnvl", "COALESCE(nhap.tongSoNvl, 0)",
                "tongtiennhap", "COALESCE(nhap.tongTienNhap, 0)"
        );

        if (!sortColumns.containsKey(sort)) {
            sort = "mancc";
        }

        direction = "desc".equalsIgnoreCase(direction) ? "desc" : "asc";
        String orderColumn = sortColumns.get(sort);

        // Lấy dữ liệu nhà cung cấp cho trang hiện tại
        String dataSql = """
                SELECT
                    n.mancc AS mancc,
                    n.tenncc AS tenncc,
                    n.diachi AS diachi,
                    n.sdt AS sdt,
                    n.email AS email,
                    n.trangthai AS trangthai,
                    n.ghichu AS ghichu,
                    COALESCE(nhap.tongSoNvl, 0) AS tongSoNvl,
                    COALESCE(nhap.tongTienNhap, 0) AS tongTienNhap
                FROM ncc n
                """ + receiptSummaryJoin
                + where
                + " ORDER BY " + orderColumn + " " + direction
                + ", n.mancc ASC LIMIT ? OFFSET ?";

        List<Object> dataArgs = new ArrayList<>(args);
        dataArgs.add(pageSize);
        dataArgs.add(offset);

        List<Map<String, Object>> nhaCungCapList =
                jdbc.queryForList(dataSql, dataArgs.toArray());

        // Dữ liệu cho giao diện
        model.addAttribute("today", LocalDate.now());

        model.addAttribute("totalAll", totalAll == null ? 0 : totalAll);
        model.addAttribute("activeCount", activeCount == null ? 0 : activeCount);
        model.addAttribute("inactiveCount", inactiveCount == null ? 0 : inactiveCount);

        model.addAttribute("nhaCungCapList", nhaCungCapList);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        model.addAttribute("search", search);
        model.addAttribute("maNcc", maNcc);
        model.addAttribute("tenNcc", tenNcc);
        model.addAttribute("diaChi", diaChi);
        model.addAttribute("sdt", sdt);
        model.addAttribute("email", email);
        model.addAttribute("trangThai", trangThai);

        // Giữ lại giá trị bộ lọc khi tìm kiếm, sắp xếp hoặc chuyển trang
        model.addAttribute("tuSoNvl", tuSoNvl);
        model.addAttribute("denSoNvl", denSoNvl);
        model.addAttribute("tuTongTien", tuTongTien);
        model.addAttribute("denTongTien", denTongTien);

        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        return "admin/nha-cung-cap";
    }

    /**
     * Đọc số không âm từ bộ lọc.
     * Nếu để trống hoặc nhập sai định dạng thì bỏ qua điều kiện tương ứng.
     */
    private BigDecimal parseNonNegativeDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            BigDecimal number = new BigDecimal(value.trim());

            if (number.signum() < 0) {
                return null;
            }

            return number;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}