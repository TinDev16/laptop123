package com.example.demo.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminThuongHieuController {

    private final JdbcTemplate jdbc;

    public AdminThuongHieuController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/admin/thuong-hieu")
    public String danhSachThuongHieu(
            Authentication authentication,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String maThuongHieu,
            @RequestParam(defaultValue = "") String tenThuongHieu,
            @RequestParam(defaultValue = "") String trangThai,
            @RequestParam(defaultValue = "") String tuSoSanPham,
            @RequestParam(defaultValue = "") String denSoSanPham,
            @RequestParam(defaultValue = "mathuonghieu") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        // Chỉ ADMIN được truy cập.
        if (authentication == null
                || authentication.getAuthorities().stream()
                .noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            throw new AccessDeniedException(
                    "Chỉ ADMIN được xem danh sách thương hiệu."
            );
        }

        search = search.trim();
        maThuongHieu = maThuongHieu.trim();
        tenThuongHieu = tenThuongHieu.trim();
        trangThai = trangThai.trim();
        tuSoSanPham = tuSoSanPham.trim();
        denSoSanPham = denSoSanPham.trim();

        // Kiểm tra điều kiện lọc số lượng.
        Integer soSanPhamTu = parseSoLuong(tuSoSanPham, "Số sản phẩm từ");
        Integer soSanPhamDen = parseSoLuong(denSoSanPham, "Số sản phẩm đến");

        if (soSanPhamTu != null
                && soSanPhamDen != null
                && soSanPhamTu > soSanPhamDen) {
            String temp = tuSoSanPham;
            tuSoSanPham = denSoSanPham;
            denSoSanPham = temp;

            soSanPhamTu = parseSoLuong(tuSoSanPham, "Số sản phẩm từ");
            soSanPhamDen = parseSoLuong(denSoSanPham, "Số sản phẩm đến");
        }

        // Chỉ chấp nhận các trường sắp xếp được quy định.
        List<String> allowedSort = List.of(
                "mathuonghieu",
                "tenthuonghieu",
                "trangthai",
                "tongsanpham"
        );

        if (!allowedSort.contains(sort)) {
            sort = "mathuonghieu";
        }

        direction = "desc".equalsIgnoreCase(direction) ? "DESC" : "ASC";

        // Biểu thức sắp xếp được ánh xạ cố định, tránh SQL injection.
        String orderBy = switch (sort) {
            case "tenthuonghieu" -> "th.tenthuonghieu";
            case "trangthai" -> "th.trangthai";
            case "tongsanpham" -> "COALESCE(sp_count.tongsanpham, 0)";
            default -> "th.mathuonghieu";
        };

        /*
         * Đếm số sản phẩm gắn với mỗi thương hiệu.
         * Mỗi bản ghi trong sanpham được tính là một sản phẩm.
         * Không cộng số lượng tồn kho và không đếm từng chiếc hàng tồn.
         */
        String fromSql = """
                FROM thuonghieu th
                LEFT JOIN (
                    SELECT
                        sp.mathuonghieu,
                        COUNT(*) AS tongsanpham
                    FROM sanpham sp
                    GROUP BY sp.mathuonghieu
                ) sp_count
                    ON sp_count.mathuonghieu = th.mathuonghieu
                """;

        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();

        // Tìm kiếm nhanh theo mã hoặc tên thương hiệu.
        if (!search.isEmpty()) {
            where.append("""
                    AND (
                        CAST(th.mathuonghieu AS CHAR) LIKE ?
                        OR th.tenthuonghieu LIKE ?
                    )
                    """);

            String keyword = "%" + search + "%";
            params.add(keyword);
            params.add(keyword);
        }

        // Tìm kiếm nâng cao theo mã thương hiệu.
        if (!maThuongHieu.isEmpty()) {
            where.append(" AND CAST(th.mathuonghieu AS CHAR) LIKE ? ");
            params.add("%" + maThuongHieu + "%");
        }

        // Tìm kiếm nâng cao theo tên thương hiệu.
        if (!tenThuongHieu.isEmpty()) {
            where.append(" AND th.tenthuonghieu LIKE ? ");
            params.add("%" + tenThuongHieu + "%");
        }

        // Lọc trạng thái.
        if ("0".equals(trangThai) || "1".equals(trangThai)) {
            where.append(" AND th.trangthai = ? ");
            params.add(Integer.parseInt(trangThai));
        }

        // Lọc số sản phẩm từ.
        if (soSanPhamTu != null) {
            where.append(" AND COALESCE(sp_count.tongsanpham, 0) >= ? ");
            params.add(soSanPhamTu);
        }

        // Lọc số sản phẩm đến.
        if (soSanPhamDen != null) {
            where.append(" AND COALESCE(sp_count.tongsanpham, 0) <= ? ");
            params.add(soSanPhamDen);
        }

        // Đếm tổng số thương hiệu thỏa mãn tất cả bộ lọc.
        String countSql = """
                SELECT COUNT(*)
                """ + fromSql + where;

        Integer totalRecords = jdbc.queryForObject(
                countSql,
                Integer.class,
                params.toArray()
        );

        int total = totalRecords == null ? 0 : totalRecords;
        int pageSize = 10;
        int totalPages = Math.max(
                1,
                (int) Math.ceil(total / (double) pageSize)
        );

        page = Math.max(1, Math.min(page, totalPages));
        int offset = (page - 1) * pageSize;

        // Lấy danh sách thương hiệu cùng số sản phẩm.
        String sql = """
                SELECT
                    th.mathuonghieu,
                    th.tenthuonghieu,
                    th.trangthai,
                    th.ghichu,
                    COALESCE(sp_count.tongsanpham, 0) AS tongSanPham
                """ + fromSql + where
                + " ORDER BY " + orderBy + " " + direction
                + ", th.mathuonghieu ASC LIMIT ? OFFSET ?";

        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(pageSize);
        queryParams.add(offset);

        List<Map<String, Object>> thuongHieuList = jdbc.queryForList(
                sql,
                queryParams.toArray()
        );

        // Thống kê chung, không phụ thuộc bộ lọc.
        Integer tongThuongHieu = jdbc.queryForObject(
                "SELECT COUNT(*) FROM thuonghieu",
                Integer.class
        );

        Integer dangHoatDong = jdbc.queryForObject(
                "SELECT COUNT(*) FROM thuonghieu WHERE trangthai = 0",
                Integer.class
        );

        Integer ngungHoatDong = jdbc.queryForObject(
                "SELECT COUNT(*) FROM thuonghieu WHERE trangthai = 1",
                Integer.class
        );

        model.addAttribute("thuongHieuList", thuongHieuList);
        model.addAttribute(
                "tongThuongHieu",
                tongThuongHieu == null ? 0 : tongThuongHieu
        );
        model.addAttribute(
                "dangHoatDong",
                dangHoatDong == null ? 0 : dangHoatDong
        );
        model.addAttribute(
                "ngungHoatDong",
                ngungHoatDong == null ? 0 : ngungHoatDong
        );

        model.addAttribute("search", search);
        model.addAttribute("maThuongHieu", maThuongHieu);
        model.addAttribute("tenThuongHieu", tenThuongHieu);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("tuSoSanPham", tuSoSanPham);
        model.addAttribute("denSoSanPham", denSoSanPham);
        model.addAttribute("sort", sort);
        model.addAttribute(
                "direction",
                "DESC".equals(direction) ? "desc" : "asc"
        );

        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", total);
        model.addAttribute("pageSize", pageSize);

        return "admin/thuong-hieu";
    }

    private Integer parseSoLuong(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            int number = Integer.parseInt(value.trim());

            if (number < 0) {
                throw new IllegalArgumentException(
                        fieldName + " không được nhỏ hơn 0."
                );
            }

            return number;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    fieldName + " phải là số nguyên không âm.",
                    ex
            );
        }
    }
}