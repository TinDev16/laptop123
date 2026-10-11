package com.example.demo.Controller;

import com.example.demo.Service.MaterialCatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/nguyen-vat-lieu")
public class AdminNguyenVatLieuController {

    private final MaterialCatalogService materialCatalogService;

    public AdminNguyenVatLieuController(
            MaterialCatalogService materialCatalogService) {
        this.materialCatalogService = materialCatalogService;
    }

    @GetMapping
    public String danhSach(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String ma,
            @RequestParam(defaultValue = "") String ten,
            @RequestParam(defaultValue = "") String loai,
            @RequestParam(defaultValue = "") String donVi,
            @RequestParam(defaultValue = "") String trangThai,
            @RequestParam(defaultValue = "") String tonKho,
            @RequestParam(defaultValue = "") String giaTu,
            @RequestParam(defaultValue = "") String giaDen,
            @RequestParam(defaultValue = "manvl_code") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        // Lấy dữ liệu hiện có, không tạo hoặc sửa dữ liệu.
        List<Map<String, Object>> allMaterials =
                materialCatalogService.list("");

        List<Map<String, Object>> types =
                materialCatalogService.types();

        String keyword = normalize(search);
        String codeKeyword = normalize(ma);
        String nameKeyword = normalize(ten);
        String unitKeyword = normalize(donVi);

        // Khoảng giá nhập: từ giaTu đến giaDen.
        BigDecimal minPrice = parseOptionalNumber(giaTu);
        BigDecimal maxPrice = parseOptionalNumber(giaDen);

        List<Map<String, Object>> filtered = allMaterials.stream()
                .filter(item -> {
                    String code = normalize(item.get("manvl_code"));
                    String name = normalize(item.get("tennvl"));
                    String unit = normalize(item.get("donvitinh"));
                    String typeId = String.valueOf(item.get("maloai"));
                    String typeName = normalize(item.get("tenloai"));

                    boolean matchSearch = keyword.isEmpty()
                            || code.contains(keyword)
                            || name.contains(keyword)
                            || typeName.contains(keyword);

                    boolean matchCode = codeKeyword.isEmpty()
                            || code.contains(codeKeyword);

                    boolean matchName = nameKeyword.isEmpty()
                            || name.contains(nameKeyword);

                    boolean matchUnit = unitKeyword.isEmpty()
                            || unit.contains(unitKeyword);

                    boolean matchType = loai.isEmpty()
                            || typeId.equals(loai);

                    String status = String.valueOf(item.get("trangthai"));

                    boolean matchStatus = trangThai.isEmpty()
                            || status.equals(trangThai);

                    BigDecimal stock = number(item.get("soluongton"));
                    BigDecimal minimum = number(item.get("ton_toithieu"));

                    boolean matchStock = tonKho.isEmpty()
                            || ("het".equals(tonKho)
                            && stock.compareTo(BigDecimal.ZERO) <= 0)
                            || ("thap".equals(tonKho)
                            && stock.compareTo(BigDecimal.ZERO) > 0
                            && stock.compareTo(minimum) < 0)
                            || ("du".equals(tonKho)
                            && stock.compareTo(minimum) >= 0);

                    // Lọc theo đơn giá nhập.
                    BigDecimal price = number(item.get("dongianhap"));

                    boolean matchMinPrice = minPrice == null
                            || price.compareTo(minPrice) >= 0;

                    boolean matchMaxPrice = maxPrice == null
                            || price.compareTo(maxPrice) <= 0;

                    return matchSearch
                            && matchCode
                            && matchName
                            && matchUnit
                            && matchType
                            && matchStatus
                            && matchStock
                            && matchMinPrice
                            && matchMaxPrice;
                })
                .collect(Collectors.toList());

        // Sắp xếp theo cột cho phép.
        Comparator<Map<String, Object>> comparator;

        switch (sort) {
            case "tennvl":
                comparator = Comparator.comparing(
                        item -> normalize(item.get("tennvl")));
                break;

            case "tenloai":
                comparator = Comparator.comparing(
                        item -> normalize(item.get("tenloai")));
                break;

            case "donvitinh":
                comparator = Comparator.comparing(
                        item -> normalize(item.get("donvitinh")));
                break;

            case "soluongton":
                comparator = Comparator.comparing(
                        item -> number(item.get("soluongton")));
                break;

            case "ton_toithieu":
                comparator = Comparator.comparing(
                        item -> number(item.get("ton_toithieu")));
                break;

            case "dongianhap":
                comparator = Comparator.comparing(
                        item -> number(item.get("dongianhap")));
                break;

            case "trangthai":
                comparator = Comparator.comparingInt(
                        item -> intValue(item.get("trangthai")));
                break;

            default:
                sort = "manvl_code";
                comparator = Comparator.comparing(
                        item -> normalize(item.get("manvl_code")));
                break;
        }

        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
            direction = "desc";
        } else {
            direction = "asc";
        }

        filtered.sort(comparator);

        // Thống kê toàn bộ danh mục.
        long activeCount = allMaterials.stream()
                .filter(item -> intValue(item.get("trangthai")) == 1)
                .count();

        long outOfStockCount = allMaterials.stream()
                .filter(item -> number(item.get("soluongton"))
                        .compareTo(BigDecimal.ZERO) <= 0)
                .count();

        // Phân trang.
        int pageSize = 10;
        int totalItems = filtered.size();

        int totalPages = Math.max(
                1,
                (int) Math.ceil(totalItems / (double) pageSize));

        page = Math.max(1, Math.min(page, totalPages));

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        List<Map<String, Object>> pageItems =
                filtered.subList(fromIndex, toIndex);

        // Dữ liệu hiển thị.
        model.addAttribute("materials", pageItems);
        model.addAttribute("types", types);

        model.addAttribute("totalAll", allMaterials.size());
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("outOfStockCount", outOfStockCount);

        // Giữ lại giá trị tìm kiếm và bộ lọc trên giao diện.
        model.addAttribute("search", search);
        model.addAttribute("ma", ma);
        model.addAttribute("ten", ten);
        model.addAttribute("loai", loai);
        model.addAttribute("donVi", donVi);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("tonKho", tonKho);
        model.addAttribute("giaTu", giaTu);
        model.addAttribute("giaDen", giaDen);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        return "admin/nguyen-vat-lieu";
    }

    /**
     * Chuẩn hóa chuỗi để tìm kiếm không phân biệt chữ hoa,
     * chữ thường và dấu tiếng Việt.
     */
    private static String normalize(Object value) {
        if (value == null) {
            return "";
        }

        String text = String.valueOf(value)
                .trim()
                .toLowerCase();

        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd');
    }

    /**
     * Chuyển giá trị về BigDecimal.
     * Giá trị null hoặc không hợp lệ được xem là 0.
     */
    private static BigDecimal number(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }

        try {
            return new BigDecimal(String.valueOf(value).trim());
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Giá trị bộ lọc giá không nhập thì trả về null,
     * nghĩa là không giới hạn ở phía đó.
     */
    private static BigDecimal parseOptionalNumber(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            BigDecimal result = new BigDecimal(value.trim());

            // Không nhận giá âm.
            if (result.compareTo(BigDecimal.ZERO) < 0) {
                return null;
            }

            return result;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static int intValue(Object value) {
        return number(value).intValue();
    }
}