package com.example.demo.Controller;

import com.example.demo.Service.MaterialCatalogService;

import java.math.BigDecimal;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/nguyen-vat-lieu")
public class MaterialCatalogController {

    private final MaterialCatalogService service;

    public MaterialCatalogController(MaterialCatalogService service) {
        this.service = service;
    }

    // =========================
    // DANH SÁCH NGUYÊN VẬT LIỆU
    // =========================

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "") String q,
            Model model) {

        model.addAttribute("materials", service.list(q));
        model.addAttribute("q", q);

        return "mrp/materials";
    }

    // =========================
    // FORM THÊM NGUYÊN VẬT LIỆU
    // =========================

    @GetMapping("/create")
    public String createForm(Model model) {

        /*
         * Khi tạo mới:
         * Chỉ lấy các loại NVL đang hoạt động.
         */
        model.addAttribute(
                "types",
                service.activeTypes()
        );

        return "mrp/material-form";
    }

    // =========================
    // FORM SỬA NGUYÊN VẬT LIỆU
    // =========================

    @GetMapping("/{id}")
    public String editForm(
            @PathVariable long id,
            Model model,
            RedirectAttributes flash) {

        try {

            /*
             * Lấy thông tin NVL đang sửa.
             */
            model.addAttribute(
                    "material",
                    service.get(id)
            );

            /*
             * Khi sửa:
             *
             * - Hiển thị tất cả loại đang hoạt động.
             * - Nếu loại hiện tại của NVL đã ngừng sử dụng
             *   thì vẫn hiển thị loại đó để giữ dữ liệu cũ.
             */
            model.addAttribute(
                    "types",
                    service.typesForEdit(id)
            );

            return "mrp/material-form";

        } catch (IllegalArgumentException exception) {

            flash.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );

            return "redirect:/nguyen-vat-lieu";
        }
    }

    // =========================
    // LƯU NGUYÊN VẬT LIỆU
    // =========================

    @PostMapping
    public String save(
            @RequestParam(required = false) Long id,
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam Integer typeId,
            @RequestParam String unit,
            @RequestParam BigDecimal price,
            @RequestParam int minimum,
            @RequestParam String status,
            @RequestParam(required = false) String note,
            RedirectAttributes flash) {

        try {

            service.save(
                    id,
                    code,
                    name,
                    typeId,
                    unit,
                    price,
                    minimum,
                    status,
                    note
            );

            flash.addFlashAttribute(
                    "success",
                    "Đã lưu nguyên vật liệu."
            );

            return "redirect:/nguyen-vat-lieu";

        } catch (IllegalArgumentException | DataAccessException exception) {

            String message =
                    exception instanceof DataAccessException
                            ? "Không lưu được NVL. Kiểm tra mã NVL và loại NVL."
                            : exception.getMessage();

            flash.addFlashAttribute(
                    "error",
                    message
            );

            return id == null
                    ? "redirect:/nguyen-vat-lieu/create"
                    : "redirect:/nguyen-vat-lieu/" + id;
        }
    }

    // =========================
    // TRANG QUẢN LÝ LOẠI NVL
    // =========================

    @GetMapping("/loai")
    public String types(Model model) {

        model.addAttribute(
                "types",
                service.types()
        );

        return "mrp/material-types";
    }

    // =========================
    // FORM SỬA LOẠI NVL
    // =========================

    @GetMapping("/loai/{id}")
    public String editType(
            @PathVariable int id,
            Model model,
            RedirectAttributes flash) {

        try {

            model.addAttribute(
                    "type",
                    service.getType(id)
            );

            return "mrp/material-type-form";

        } catch (IllegalArgumentException exception) {

            flash.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );

            return "redirect:/nguyen-vat-lieu/loai";
        }
    }

    // =========================
    // THÊM LOẠI NVL
    // =========================

    @PostMapping("/loai")
    public String addType(
            @RequestParam String name,
            RedirectAttributes flash) {

        try {

            service.addType(name);

            flash.addFlashAttribute(
                    "success",
                    "Đã thêm loại nguyên vật liệu."
            );

        } catch (IllegalArgumentException | DataAccessException exception) {

            String message =
                    exception instanceof DataAccessException
                            ? "Không thể thêm loại. Tên loại có thể đã tồn tại."
                            : exception.getMessage();

            flash.addFlashAttribute(
                    "error",
                    message
            );
        }

        return "redirect:/nguyen-vat-lieu/loai";
    }

    // =========================
    // CẬP NHẬT LOẠI NVL
    // =========================

    @PostMapping("/loai/update")
    public String updateType(
            @RequestParam int id,
            @RequestParam String name,
            @RequestParam int status,
            RedirectAttributes flash) {

        try {

            service.updateType(
                    id,
                    name,
                    status
            );

            flash.addFlashAttribute(
                    "success",
                    "Đã cập nhật loại nguyên vật liệu."
            );

        } catch (IllegalArgumentException | DataAccessException exception) {

            String message =
                    exception instanceof DataAccessException
                            ? "Không thể cập nhật loại nguyên vật liệu."
                            : exception.getMessage();

            flash.addFlashAttribute(
                    "error",
                    message
            );
        }

        return "redirect:/nguyen-vat-lieu/loai";
    }
}