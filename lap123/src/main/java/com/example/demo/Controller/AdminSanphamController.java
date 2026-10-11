package com.example.demo.Controller;

import com.example.demo.Entity.Sanpham;
import com.example.demo.Service.BomService;
import com.example.demo.Service.SanphamService;
import com.example.demo.Service.ThuonghieuService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/sanpham")
public class AdminSanphamController {

    private final SanphamService sanphamService;
    private final ThuonghieuService thuonghieuService;
    private final BomService bomService;

    public AdminSanphamController(
            SanphamService sanphamService,
            ThuonghieuService thuonghieuService,
            BomService bomService) {

        this.sanphamService = sanphamService;
        this.thuonghieuService = thuonghieuService;
        this.bomService = bomService;
    }

    @GetMapping({"", "/"})
    public String danhSachSanpham(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String qDangBan,
            @RequestParam(defaultValue = "") String qNgungBan,

            @RequestParam(defaultValue = "") String trangThai,
            @RequestParam(defaultValue = "") String thuongHieu,
            @RequestParam(defaultValue = "") String heDieuHanh,

            @RequestParam(defaultValue = "") String giaTu,
            @RequestParam(defaultValue = "") String giaDen,

            @RequestParam(defaultValue = "") String tonKhoTu,
            @RequestParam(defaultValue = "") String tonKhoDen,

            @RequestParam(defaultValue = "MASP") String sort,
            @RequestParam(defaultValue = "asc") String direction,

            @RequestParam(defaultValue = "1") int pageDangBan,
            @RequestParam(defaultValue = "1") int pageNgungBan,

            Model model) {

        // =====================================================
        // 1. LẤY DANH SÁCH SẢN PHẨM
        // =====================================================

        List<Sanpham> all = sanphamService.getAllSanpham();

        // =====================================================
        // 2. TÍNH KHOẢNG GIÁ BOM CỦA TỪNG SẢN PHẨM
        // =====================================================
        // Giá BOM được lấy từ tong_gia_nvl của BomService.
        // Mỗi sản phẩm có thể có nhiều phiên bản BOM.
        // Khoảng giá = giá BOM thấp nhất đến giá BOM cao nhất.

        Map<Integer, BomPriceRange> priceRanges = new HashMap<>();

        for (Sanpham sp : all) {

            List<Map<String, Object>> versions =
                    bomService.versions(sp.getMASP());

            BigDecimal min = null;
            BigDecimal max = null;

            if (versions != null) {
                for (Map<String, Object> version : versions) {

                    BigDecimal price =
                            toBigDecimal(version.get("tong_gia_nvl"));

                    if (price == null || price.signum() < 0) {
                        continue;
                    }

                    if (min == null || price.compareTo(min) < 0) {
                        min = price;
                    }

                    if (max == null || price.compareTo(max) > 0) {
                        max = price;
                    }
                }
            }

            priceRanges.put(
                    sp.getMASP(),
                    new BomPriceRange(min, max)
            );
        }

        // =====================================================
        // 3. CHUẨN HÓA CÁC ĐIỀU KIỆN TÌM KIẾM
        // =====================================================

        String keyword = normalize(q);

        String activeKeyword = keyword.isEmpty()
                ? normalize(qDangBan)
                : keyword;

        String archivedKeyword = keyword.isEmpty()
                ? normalize(qNgungBan)
                : keyword;

        String normalizedStatus = normalize(trangThai);
        String normalizedBrand = normalize(thuongHieu);
        String normalizedOS = normalize(heDieuHanh);

        BigDecimal minPrice = parsePrice(giaTu);
        BigDecimal maxPrice = parsePrice(giaDen);

        Integer minStock = parseInteger(tonKhoTu);
        Integer maxStock = parseInteger(tonKhoDen);

        // Nếu người dùng nhập ngược khoảng thì đổi vị trí
        // để khoảng lọc luôn hợp lệ.
        if (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            BigDecimal temp = minPrice;
            minPrice = maxPrice;
            maxPrice = temp;

            giaTu = minPrice.toPlainString();
            giaDen = maxPrice.toPlainString();
        }

        if (minStock != null && maxStock != null
                && minStock > maxStock) {

            Integer temp = minStock;
            minStock = maxStock;
            maxStock = temp;

            tonKhoTu = minStock.toString();
            tonKhoDen = maxStock.toString();
        }

        final BigDecimal finalMinPrice = minPrice;
        final BigDecimal finalMaxPrice = maxPrice;

        final Integer finalMinStock = minStock;
        final Integer finalMaxStock = maxStock;

        // =====================================================
        // 4. LỌC SẢN PHẨM ĐANG BÁN
        // =====================================================

        List<Sanpham> dangBan = all.stream()

                .filter(sp -> sp.getTRANGTHAI() == 0)

                .filter(sp -> matchesKeyword(
                        sp,
                        activeKeyword
                ))

                .filter(sp -> matchesStatus(
                        sp,
                        normalizedStatus
                ))

                .filter(sp -> matchesBrand(
                        sp,
                        normalizedBrand
                ))

                .filter(sp -> matchesOperatingSystem(
                        sp,
                        normalizedOS
                ))

                .filter(sp -> matchesPrice(
                        priceRanges.get(sp.getMASP()),
                        finalMinPrice,
                        finalMaxPrice
                ))

                .filter(sp -> matchesStock(
                        sp,
                        finalMinStock,
                        finalMaxStock
                ))

                .collect(Collectors.toCollection(ArrayList::new));

        // =====================================================
        // 5. LỌC SẢN PHẨM ĐÃ NGỪNG BÁN
        // =====================================================

        List<Sanpham> ngungBan = all.stream()

                .filter(sp -> sp.getTRANGTHAI() == 1)

                .filter(sp -> matchesKeyword(
                        sp,
                        archivedKeyword
                ))

                .filter(sp -> matchesStatus(
                        sp,
                        normalizedStatus
                ))

                .filter(sp -> matchesBrand(
                        sp,
                        normalizedBrand
                ))

                .filter(sp -> matchesOperatingSystem(
                        sp,
                        normalizedOS
                ))

                .filter(sp -> matchesPrice(
                        priceRanges.get(sp.getMASP()),
                        finalMinPrice,
                        finalMaxPrice
                ))

                .filter(sp -> matchesStock(
                        sp,
                        finalMinStock,
                        finalMaxStock
                ))

                .collect(Collectors.toCollection(ArrayList::new));

        // =====================================================
        // 6. KIỂM TRA TIÊU CHÍ SẮP XẾP
        // =====================================================

        List<String> allowedSorts = List.of(
                "MASP",
                "TENSP",
                "THUONGHIEU",
                "TRANGTHAI",
                "SOLUONGTON",
                "GIA_BOM_MIN",
                "GIA_BOM_MAX"
        );

        if (!allowedSorts.contains(sort)) {
            sort = "MASP";
        }

        direction = "desc".equalsIgnoreCase(direction)
                ? "desc"
                : "asc";

        Comparator<Sanpham> comparator =
                getComparator(sort, priceRanges);

        if ("desc".equals(direction)) {
            comparator = comparator.reversed();
        }

        dangBan.sort(comparator);
        ngungBan.sort(comparator);

        // =====================================================
        // 7. PHÂN TRANG
        // =====================================================

        int pageSize = 10;

        addPagedData(
                model,
                "dangBan",
                dangBan,
                pageDangBan,
                pageSize
        );

        addPagedData(
                model,
                "ngungBan",
                ngungBan,
                pageNgungBan,
                pageSize
        );

        // =====================================================
        // 8. TRUYỀN ĐIỀU KIỆN TÌM KIẾM SANG THYMELEAF
        // =====================================================

        model.addAttribute("q", q);
        model.addAttribute("qDangBan", qDangBan);
        model.addAttribute("qNgungBan", qNgungBan);

        model.addAttribute("trangThai", trangThai);
        model.addAttribute("thuongHieu", thuongHieu);
        model.addAttribute("heDieuHanh", heDieuHanh);

        model.addAttribute("giaTu", giaTu);
        model.addAttribute("giaDen", giaDen);

        model.addAttribute("tonKhoTu", tonKhoTu);
        model.addAttribute("tonKhoDen", tonKhoDen);

        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        // Khoảng giá BOM dùng để hiển thị trên giao diện.
        model.addAttribute(
                "giaBomTheoSanpham",
                priceRanges
        );

        // Danh sách thương hiệu cho ô chọn.
        model.addAttribute(
                "thuonghieuList",
                thuonghieuService.getAllThuonghieu()
        );

        // =====================================================
        // 9. THỐNG KÊ TỔNG SẢN PHẨM
        // =====================================================

        model.addAttribute(
                "tongSanpham",
                all.size()
        );

        model.addAttribute(
                "tongDangBan",
                all.stream()
                        .filter(sp -> sp.getTRANGTHAI() == 0)
                        .count()
        );

        model.addAttribute(
                "tongNgungBan",
                all.stream()
                        .filter(sp -> sp.getTRANGTHAI() == 1)
                        .count()
        );

        return "admin/sanpham";
    }

    // =========================================================
    // API: LẤY CÁC PHIÊN BẢN BOM CỦA SẢN PHẨM
    // =========================================================

    @GetMapping("/bom-versions")
    @ResponseBody
    public List<Map<String, Object>> bomVersions(
            @RequestParam int productId) {

        return bomService.versions(productId);
    }

    // =========================================================
    // TÌM KIẾM THEO TỪ KHÓA
    // =========================================================

    private boolean matchesKeyword(
            Sanpham sp,
            String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String q = keyword.trim().toLowerCase(Locale.ROOT);

        return String.valueOf(sp.getMASP()).contains(q)
                || contains(sp.getTENSP(), q)
                || contains(
                sp.getThuonghieu() != null
                        ? sp.getThuonghieu()
                        .getTENTHUONGHIEU()
                        : null,
                q
        );
    }

    // =========================================================
    // LỌC THEO TRẠNG THÁI
    // =========================================================
    // Giá trị dự kiến:
    // ""  = tất cả
    // "0" = đang bán
    // "1" = ngừng bán

    private boolean matchesStatus(
            Sanpham sp,
            String trangThai) {

        if (trangThai == null || trangThai.isBlank()) {
            return true;
        }

        try {
            int status = Integer.parseInt(trangThai);

            return sp.getTRANGTHAI() == status;

        } catch (NumberFormatException e) {
            return true;
        }
    }

    // =========================================================
    // LỌC THEO THƯƠNG HIỆU
    // =========================================================
    // Ô chọn thương hiệu nên gửi tên thương hiệu.
    // Giá trị rỗng nghĩa là tất cả thương hiệu.

    private boolean matchesBrand(
            Sanpham sp,
            String thuongHieu) {

        if (thuongHieu == null || thuongHieu.isBlank()) {
            return true;
        }

        if (sp.getThuonghieu() == null) {
            return false;
        }

        String productBrand =
                sp.getThuonghieu().getTENTHUONGHIEU();

        return productBrand != null
                && productBrand.equalsIgnoreCase(thuongHieu);
    }

    // =========================================================
    // LỌC THEO HỆ ĐIỀU HÀNH
    // =========================================================

    private boolean matchesOperatingSystem(
            Sanpham sp,
            String heDieuHanh) {

        if (heDieuHanh == null || heDieuHanh.isBlank()) {
            return true;
        }

        return sp.getHEDIEUHANH() != null
                && sp.getHEDIEUHANH()
                .equalsIgnoreCase(heDieuHanh);
    }

    // =========================================================
    // LỌC THEO KHOẢNG GIÁ BOM
    // =========================================================
    // Sản phẩm được giữ lại nếu khoảng giá BOM của nó
    // giao với khoảng giá người dùng tìm kiếm.
    //
    // Ví dụ:
    // Giá BOM sản phẩm: 8.000.000 - 11.000.000
    // Giá tìm kiếm:     9.000.000 - 10.000.000
    // Kết quả: phù hợp.

    private boolean matchesPrice(
            BomPriceRange range,
            BigDecimal giaTu,
            BigDecimal giaDen) {

        boolean hasPriceFilter =
                giaTu != null || giaDen != null;

        if (!hasPriceFilter) {
            return true;
        }

        // Có lọc giá nhưng sản phẩm chưa có BOM hợp lệ.
        if (range == null
                || range.min() == null
                || range.max() == null) {
            return false;
        }

        // Giá BOM cao nhất phải >= giá tìm kiếm thấp nhất.
        if (giaTu != null
                && range.max().compareTo(giaTu) < 0) {
            return false;
        }

        // Giá BOM thấp nhất phải <= giá tìm kiếm cao nhất.
        if (giaDen != null
                && range.min().compareTo(giaDen) > 0) {
            return false;
        }

        return true;
    }

    // =========================================================
    // LỌC THEO TỒN KHO
    // =========================================================

    private boolean matchesStock(
            Sanpham sp,
            Integer tonKhoTu,
            Integer tonKhoDen) {

        int stock = sp.getSOLUONGTON() != null
                ? sp.getSOLUONGTON()
                : 0;

        if (tonKhoTu != null && stock < tonKhoTu) {
            return false;
        }

        if (tonKhoDen != null && stock > tonKhoDen) {
            return false;
        }

        return true;
    }

    // =========================================================
    // HÀM KIỂM TRA CHUỖI
    // =========================================================

    private boolean contains(
            String value,
            String keyword) {

        return value != null
                && value.toLowerCase(Locale.ROOT)
                .contains(keyword);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    // =========================================================
    // SẮP XẾP DANH SÁCH SẢN PHẨM
    // =========================================================

    private Comparator<Sanpham> getComparator(
            String sort,
            Map<Integer, BomPriceRange> priceRanges) {

        switch (sort) {

            case "TENSP":
                return Comparator.comparing(
                        Sanpham::getTENSP,
                        Comparator.nullsLast(
                                String.CASE_INSENSITIVE_ORDER
                        )
                );

            case "THUONGHIEU":
                return Comparator.comparing(
                        (Sanpham sp) ->
                                sp.getThuonghieu() != null
                                        ? sp.getThuonghieu()
                                        .getTENTHUONGHIEU()
                                        : null,
                        Comparator.nullsLast(
                                String.CASE_INSENSITIVE_ORDER
                        )
                );

            case "TRANGTHAI":
                return Comparator.comparingInt(
                        Sanpham::getTRANGTHAI
                );

            case "SOLUONGTON":
                return Comparator.comparing(
                        (Sanpham sp) -> sp.getSOLUONGTON(),
                        Comparator.nullsLast(Integer::compareTo)
                );

            case "GIA_BOM_MIN":
                return Comparator.comparing(
                        (Sanpham sp) -> {
                            BomPriceRange range =
                                    priceRanges.get(sp.getMASP());

                            return range != null
                                    ? range.min()
                                    : null;
                        },
                        Comparator.nullsLast(BigDecimal::compareTo)
                );

            case "GIA_BOM_MAX":
                return Comparator.comparing(
                        (Sanpham sp) -> {
                            BomPriceRange range =
                                    priceRanges.get(sp.getMASP());

                            return range != null
                                    ? range.max()
                                    : null;
                        },
                        Comparator.nullsLast(BigDecimal::compareTo)
                );

            case "MASP":
            default:
                return Comparator.comparingInt(
                        Sanpham::getMASP
                );
        }
    }

    // =========================================================
    // CHUYỂN GIÁ TỪ REQUEST PARAM THÀNH BIGDECIMAL
    // =========================================================

    private BigDecimal parsePrice(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            BigDecimal price = new BigDecimal(value.trim());

            if (price.signum() < 0) {
                return null;
            }

            return price;

        } catch (NumberFormatException e) {
            return null;
        }
    }

    // =========================================================
    // CHUYỂN TỒN KHO TỪ REQUEST PARAM THÀNH INTEGER
    // =========================================================

    private Integer parseInteger(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            int number = Integer.parseInt(value.trim());

            return number >= 0 ? number : null;

        } catch (NumberFormatException e) {
            return null;
        }
    }

    // =========================================================
    // CHUYỂN KIỂU DỮ LIỆU GIÁ BOM THÀNH BIGDECIMAL
    // =========================================================

    private BigDecimal toBigDecimal(Object value) {

        if (value == null) {
            return null;
        }

        try {
            if (value instanceof BigDecimal) {
                return (BigDecimal) value;
            }

            if (value instanceof Number) {
                return new BigDecimal(value.toString());
            }

            String text = value.toString().trim();

            if (text.isEmpty()) {
                return null;
            }

            return new BigDecimal(text);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    // =========================================================
    // PHÂN TRANG
    // =========================================================

    private void addPagedData(
            Model model,
            String prefix,
            List<Sanpham> data,
            int requestedPage,
            int pageSize) {

        int totalItems = data.size();

        int totalPages = Math.max(
                1,
                (int) Math.ceil(totalItems / (double) pageSize)
        );

        int page = Math.max(
                1,
                Math.min(requestedPage, totalPages)
        );

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, totalItems);

        model.addAttribute(
                prefix + "List",
                data.subList(start, end)
        );

        model.addAttribute(prefix + "Total", totalItems);
        model.addAttribute(prefix + "Page", page);
        model.addAttribute(prefix + "TotalPages", totalPages);
    }

    // =========================================================
    // KHOẢNG GIÁ BOM CỦA MỘT SẢN PHẨM
    // =========================================================

    private record BomPriceRange(
            BigDecimal min,
            BigDecimal max) {
    }
}