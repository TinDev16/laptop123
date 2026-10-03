package com.example.demo.Controller;

import com.example.demo.Service.BomService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bom")
public class BomController {

    private final BomService service;

    public BomController(BomService service) {
        this.service = service;
    }

    // =========================
    // DANH SÁCH BOM
    // =========================

    @GetMapping
    public String index(Model model) {
        model.addAttribute("boms", service.list());
        return "mrp/bom-list";
    }

    // =========================
    // FORM TẠO BOM
    // =========================

    @GetMapping("/create")
    public String createForm(Model model) {

        var materials = service.materials();

        model.addAttribute(
                "products",
                service.products()
        );

        model.addAttribute(
                "materials",
                materials
        );

        model.addAttribute(
                "materialGroups",
                service.groupMaterialsByType(materials)
        );

        return "mrp/bom-form";
    }

    // =========================
    // AJAX:
    // LẤY CÁC VERSION BOM
    // THEO THÀNH PHẨM
    // =========================

    @GetMapping("/versions")
    @ResponseBody
    public List<Map<String, Object>> versions(
            @RequestParam int productId) {
        return service.versions(productId);
    }

    // =========================
    // AJAX:
    // LẤY NVL THEO BOM
    // =========================

    @GetMapping("/materials-by-bom")
    @ResponseBody
    public List<Map<String, Object>> materialsByBom(
            @RequestParam long bomId) {
        return service.materialsByBom(bomId);
    }

    // =========================
    // LƯU BOM MỚI
    // =========================

    @PostMapping
    public String create(
            Authentication authentication,

            @RequestParam int productId,

            @RequestParam String version,

            @RequestParam(required = false)
            String note,

            @RequestParam
            List<Long> materialId,

            @RequestParam
            List<BigDecimal> quantity,

            @RequestParam
            List<BigDecimal> waste,

            RedirectAttributes redirect) {

        try {

            long id = service.create(
                    authentication.getName(),
                    productId,
                    version,
                    note,
                    materialId,
                    quantity,
                    waste
            );

            return "redirect:/bom/" + id;

        } catch (
                IllegalArgumentException |
                DataAccessException exception) {

            redirect.addFlashAttribute(
                    "error",
                    exception instanceof DataAccessException
                            ? "Không lưu được BOM. Kiểm tra sản phẩm, nguyên vật liệu và phiên bản."
                            : exception.getMessage()
            );

            return "redirect:/bom/create";
        }
    }

    // =========================
    // CHI TIẾT BOM
    // =========================

    @GetMapping("/{id}")
    public String detail(
            @PathVariable long id,
            @RequestParam(required = false, defaultValue = "") String from,
            Model model) {

        model.addAttribute(
                "bom",
                service.get(id)
        );

        model.addAttribute(
                "details",
                service.details(id)
        );

        String backUrl;

        if ("sanpham".equalsIgnoreCase(from)) {
            backUrl = "/sanpham";
        } else {
            backUrl = "/bom";
        }

        model.addAttribute("backUrl", backUrl);

        System.out.println("DEBUG from = " + from);
        System.out.println("DEBUG backUrl = " + backUrl);

        return "mrp/bom-detail";
    }

    // =========================
    // NGỪNG SỬ DỤNG BOM
    // =========================

    @PostMapping("/{id}/disable")
    public String disable(
            @PathVariable long id,
            RedirectAttributes redirect) {

        try {

            service.disable(id);

        } catch (IllegalArgumentException exception) {

            redirect.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );
        }

        return "redirect:/bom/" + id;
    }
}