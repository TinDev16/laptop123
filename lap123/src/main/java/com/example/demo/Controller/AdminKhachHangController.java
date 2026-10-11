package com.example.demo.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class AdminKhachHangController {

    private final JdbcTemplate jdbc;

    public AdminKhachHangController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/admin/khach-hang")
    public String danhSachKhachHang(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String maKh,
            @RequestParam(defaultValue = "") String hoTen,
            @RequestParam(defaultValue = "") String gioiTinh,
            @RequestParam(defaultValue = "") String trangThai,
            @RequestParam(defaultValue = "") String tuNgaySinh,
            @RequestParam(defaultValue = "") String denNgaySinh,
            @RequestParam(defaultValue = "") String tuNgayThamGia,
            @RequestParam(defaultValue = "") String denNgayThamGia,
            @RequestParam(defaultValue = "") String tuTongTien,
            @RequestParam(defaultValue = "") String denTongTien,
            @RequestParam(defaultValue = "") String tuTongSanPham,
            @RequestParam(defaultValue = "") String denTongSanPham,
            @RequestParam(defaultValue = "makh") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "1") int page,
            Authentication authentication,
            Model model) {

        // Kiểm tra quyền ADMIN.
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getAuthorities().stream()
                .noneMatch(a ->
                        "ROLE_ADMIN".equals(a.getAuthority()))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bạn không có quyền truy cập trang này."
            );
        }

        // Các cột được phép sắp xếp.
        List<String> allowedSort = List.of(
                "makh",
                "hoten",
                "gioitinh",
                "ngaysinh",
                "sdt",
                "ngaythamgia",
                "trangthai",
                "tongtien",
                "tongsanpham"
        );

        if (!allowedSort.contains(sort)) {
            sort = "makh";
        }

        direction = "desc".equalsIgnoreCase(direction)
                ? "desc" : "asc";

        /*
         * Tổng tiền và tổng sản phẩm được tính từ các phiếu xuất
         * thành phẩm đã xuất thực tế: DA_XUAT.
         *
         * Mỗi khách hàng chỉ có một dòng tổng hợp, tránh nhân bản
         * dữ liệu khi khách hàng có nhiều phiếu xuất.
         */
        String fromSql = """
                FROM khachhang kh
                LEFT JOIN (
                    SELECT
                        px.makh,
                        COALESCE(SUM(ct.thanhtien), 0) AS tongtien,
                        COALESCE(SUM(ct.soluong), 0) AS tongsanpham
                    FROM phieuxuat_thanhpham px
                    INNER JOIN ct_phieuxuat_thanhpham ct
                        ON ct.mapxtp = px.mapxtp
                    WHERE px.trangthai = 'DA_XUAT'
                      AND px.makh IS NOT NULL
                    GROUP BY px.makh
                ) tk ON tk.makh = kh.makh
                """;

        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();

        // Tìm kiếm nhanh.
        if (search != null && !search.trim().isEmpty()) {
            where.append("""
                    AND (
                        CAST(kh.makh AS CHAR) LIKE ?
                        OR kh.hoten LIKE ?
                        OR kh.sdt LIKE ?
                    )
                    """);

            String keyword = "%" + search.trim() + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        // Mã khách hàng.
        if (maKh != null && !maKh.trim().isEmpty()) {
            where.append(" AND CAST(kh.makh AS CHAR) LIKE ? ");
            params.add("%" + maKh.trim() + "%");
        }

        // Họ tên.
        if (hoTen != null && !hoTen.trim().isEmpty()) {
            where.append(" AND kh.hoten LIKE ? ");
            params.add("%" + hoTen.trim() + "%");
        }

        // Giới tính.
        if (gioiTinh != null && !gioiTinh.trim().isEmpty()) {
            where.append(" AND kh.gioitinh = ? ");
            params.add(gioiTinh.trim());
        }

        // Trạng thái.
        if ("0".equals(trangThai) || "1".equals(trangThai)) {
            where.append(" AND kh.trangthai = ? ");
            params.add(Integer.parseInt(trangThai));
        }

        // Ngày sinh.
        if (coGiaTri(tuNgaySinh)) {
            where.append(" AND DATE(kh.ngaysinh) >= ? ");
            params.add(tuNgaySinh.trim());
        }

        if (coGiaTri(denNgaySinh)) {
            where.append(" AND DATE(kh.ngaysinh) <= ? ");
            params.add(denNgaySinh.trim());
        }

        // Ngày tham gia.
        if (coGiaTri(tuNgayThamGia)) {
            where.append(" AND DATE(kh.ngaythamgia) >= ? ");
            params.add(tuNgayThamGia.trim());
        }

        if (coGiaTri(denNgayThamGia)) {
            where.append(" AND DATE(kh.ngaythamgia) <= ? ");
            params.add(denNgayThamGia.trim());
        }

        // Tổng tiền đã mua: lọc theo khoảng tiền.
        if (coGiaTri(tuTongTien)) {
            where.append(
                    " AND COALESCE(tk.tongtien, 0) >= ? "
            );
            params.add(parseSo(tuTongTien, "Tổng tiền từ"));
        }

        if (coGiaTri(denTongTien)) {
            where.append(
                    " AND COALESCE(tk.tongtien, 0) <= ? "
            );
            params.add(parseSo(denTongTien, "Tổng tiền đến"));
        }

        // Tổng số sản phẩm đã mua: lọc theo khoảng số lượng.
        if (coGiaTri(tuTongSanPham)) {
            where.append(
                    " AND COALESCE(tk.tongsanpham, 0) >= ? "
            );
            params.add(parseSo(tuTongSanPham, "Số sản phẩm từ"));
        }

        if (coGiaTri(denTongSanPham)) {
            where.append(
                    " AND COALESCE(tk.tongsanpham, 0) <= ? "
            );
            params.add(parseSo(denTongSanPham, "Số sản phẩm đến"));
        }

        String whereSql = where.toString();

        // Thống kê tổng số khách hàng.
        Integer tongKhachHang = jdbc.queryForObject(
                "SELECT COUNT(*) FROM khachhang",
                Integer.class
        );

        Integer dangHoatDong = jdbc.queryForObject(
                "SELECT COUNT(*) FROM khachhang WHERE trangthai = 0",
                Integer.class
        );

        Integer ngungHoatDong = jdbc.queryForObject(
                "SELECT COUNT(*) FROM khachhang WHERE trangthai = 1",
                Integer.class
        );

        // Tổng số kết quả sau khi lọc.
        Long totalRecords = jdbc.queryForObject(
                "SELECT COUNT(*) " + fromSql + whereSql,
                Long.class,
                params.toArray()
        );

        if (totalRecords == null) {
            totalRecords = 0L;
        }

        // Phân trang.
        int pageSize = 10;

        int totalPages = Math.max(
                1,
                (int) Math.ceil(totalRecords / (double) pageSize)
        );

        page = Math.max(1, Math.min(page, totalPages));

        int offset = (page - 1) * pageSize;

        // Ánh xạ tên sắp xếp sang cột SQL đã xác định.
        String sortColumn;

        switch (sort) {
            case "hoten" ->
                    sortColumn = "kh.hoten";
            case "gioitinh" ->
                    sortColumn = "kh.gioitinh";
            case "ngaysinh" ->
                    sortColumn = "kh.ngaysinh";
            case "sdt" ->
                    sortColumn = "kh.sdt";
            case "ngaythamgia" ->
                    sortColumn = "kh.ngaythamgia";
            case "trangthai" ->
                    sortColumn = "kh.trangthai";
            case "tongtien" ->
                    sortColumn = "COALESCE(tk.tongtien, 0)";
            case "tongsanpham" ->
                    sortColumn = "COALESCE(tk.tongsanpham, 0)";
            default ->
                    sortColumn = "kh.makh";
        }

        // Lấy dữ liệu khách hàng và hai chỉ số mua hàng.
        String sql = """
                SELECT
                    kh.makh,
                    kh.hoten,
                    kh.gioitinh,
                    kh.ngaysinh,
                    kh.sdt,
                    kh.ngaythamgia,
                    kh.trangthai,
                    kh.ghichu,
                    COALESCE(tk.tongtien, 0) AS tongTienDaMua,
                    COALESCE(tk.tongsanpham, 0) AS tongSanPhamDaMua
                """ + fromSql
                + whereSql
                + " ORDER BY " + sortColumn + " " + direction
                + ", kh.makh ASC LIMIT ? OFFSET ?";

        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(pageSize);
        queryParams.add(offset);

        List<Map<String, Object>> khachHangList =
                jdbc.queryForList(sql, queryParams.toArray());

        // Dữ liệu trang.
        model.addAttribute("khachHangList", khachHangList);

        model.addAttribute(
                "tongKhachHang",
                tongKhachHang == null ? 0 : tongKhachHang
        );

        model.addAttribute(
                "dangHoatDong",
                dangHoatDong == null ? 0 : dangHoatDong
        );

        model.addAttribute(
                "ngungHoatDong",
                ngungHoatDong == null ? 0 : ngungHoatDong
        );

        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);

        // Giữ nguyên các điều kiện lọc.
        model.addAttribute("search", search);
        model.addAttribute("maKh", maKh);
        model.addAttribute("hoTen", hoTen);
        model.addAttribute("gioiTinh", gioiTinh);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("tuNgaySinh", tuNgaySinh);
        model.addAttribute("denNgaySinh", denNgaySinh);
        model.addAttribute("tuNgayThamGia", tuNgayThamGia);
        model.addAttribute("denNgayThamGia", denNgayThamGia);

        model.addAttribute("tuTongTien", tuTongTien);
        model.addAttribute("denTongTien", denTongTien);
        model.addAttribute("tuTongSanPham", tuTongSanPham);
        model.addAttribute("denTongSanPham", denTongSanPham);

        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        return "admin/khach-hang";
    }

    private boolean coGiaTri(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private java.math.BigDecimal parseSo(String value, String tenTruong) {
        try {
            java.math.BigDecimal number =
                    new java.math.BigDecimal(value.trim());

            if (number.signum() < 0) {
                throw new IllegalArgumentException(
                        tenTruong + " không được nhỏ hơn 0."
                );
            }

            return number;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    tenTruong + " không hợp lệ: " + value
            );
        }
    }
}