
package com.example.demo.Controller;

import com.example.demo.Service.MaterialCatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/loai-nvl")
public class AdminLoaiNvlController {

    private final MaterialCatalogService materialCatalogService;

    public AdminLoaiNvlController(
            MaterialCatalogService materialCatalogService) {
        this.materialCatalogService = materialCatalogService;
    }

    @GetMapping
    public String danhSachLoaiNvl(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String maLoai,
            @RequestParam(defaultValue = "") String tenLoai,
            @RequestParam(defaultValue = "") String trangThai,
            @RequestParam(defaultValue = "maloai") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        // Lấy dữ liệu từ bảng loai_nguyen_vat_lieu
        List<Map<String, Object>> allTypes =
                materialCatalogService.types();

        // Chuẩn hóa tham số tìm kiếm
        String keyword = search.trim().toLowerCase();
        String ma = maLoai.trim().toLowerCase();
        String ten = tenLoai.trim().toLowerCase();

        // Tìm kiếm và lọc
        List<Map<String, Object>> filtered = allTypes.stream()
                .filter(item -> {
                    String id = String.valueOf(item.get("maloai"));
                    String name = String.valueOf(item.get("tenloai"))
                            .toLowerCase();

                    boolean matchSearch =
                            keyword.isEmpty()
                                    || id.contains(keyword)
                                    || name.contains(keyword);

                    boolean matchMa =
                            ma.isEmpty()
                                    || id.toLowerCase().contains(ma);

                    boolean matchTen =
                            ten.isEmpty()
                                    || name.contains(ten);

                    String status = String.valueOf(item.get("trangthai"));

                    boolean matchStatus =
                            trangThai.isEmpty()
                                    || status.equals(trangThai);

                    return matchSearch
                            && matchMa
                            && matchTen
                            && matchStatus;
                })
                .collect(Collectors.toList());

        // Chỉ cho phép sắp xếp theo các trường được quy định
        Comparator<Map<String, Object>> comparator;

        switch (sort) {
            case "tenloai":
                comparator = Comparator.comparing(
                        item -> String.valueOf(item.get("tenloai")),
                        String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "trangthai":
                comparator = Comparator.comparingInt(
                        item -> Integer.parseInt(
                                String.valueOf(item.get("trangthai"))
                        )
                );
                break;

            default:
                comparator = Comparator.comparingLong(
                        item -> Long.parseLong(
                                String.valueOf(item.get("maloai"))
                        )
                );
                sort = "maloai";
        }

        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
            direction = "desc";
        } else {
            direction = "asc";
        }

        filtered.sort(comparator);

        // Phân trang
        int pageSize = 10;
        int totalItems = filtered.size();
        int totalPages = Math.max(
                1,
                (int) Math.ceil(totalItems / (double) pageSize)
        );

        page = Math.max(1, Math.min(page, totalPages));

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        List<Map<String, Object>> pageItems =
                filtered.subList(fromIndex, toIndex);

        // Dữ liệu hiển thị
        model.addAttribute("loaiNvlList", pageItems);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalAllTypes", allTypes.size());

        model.addAttribute("activeCount",
                allTypes.stream()
                        .filter(item -> "1".equals(
                                String.valueOf(item.get("trangthai"))))
                        .count());

        model.addAttribute("inactiveCount",
                allTypes.stream()
                        .filter(item -> "0".equals(
                                String.valueOf(item.get("trangthai"))))
                        .count());

        // Giữ nguyên điều kiện lọc khi chuyển trang hoặc sắp xếp
        model.addAttribute("search", search);
        model.addAttribute("maLoai", maLoai);
        model.addAttribute("tenLoai", tenLoai);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("today", LocalDate.now());

        return "admin/loai-nvl";
    }
}
