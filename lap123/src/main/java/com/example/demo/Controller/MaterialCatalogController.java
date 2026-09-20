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

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("materials", service.list(q));
        model.addAttribute("q", q);
        return "mrp/materials";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("suppliers", service.suppliers());
        return "mrp/material-form";
    }

    @GetMapping("/{id}")
    public String editForm(@PathVariable long id, Model model) {
        model.addAttribute("material", service.get(id));
        model.addAttribute("suppliers", service.suppliers());
        return "mrp/material-form";
    }

    @PostMapping
    public String save(@RequestParam(required = false) Long id, @RequestParam String code,
                       @RequestParam String name, @RequestParam(required = false) Integer supplierId,
                       @RequestParam String unit, @RequestParam BigDecimal price,
                       @RequestParam int minimum, @RequestParam int leadDays,
                       @RequestParam String status, @RequestParam(required = false) String note,
                       RedirectAttributes flash) {
        try {
            service.save(id, code, name, supplierId, unit, price, minimum, leadDays, status, note);
            flash.addFlashAttribute("success", "Đã lưu nguyên vật liệu.");
            return "redirect:/nguyen-vat-lieu";
        } catch (IllegalArgumentException | DataAccessException exception) {
            flash.addFlashAttribute("error", exception instanceof DataAccessException ? "Không lưu được NVL. Kiểm tra mã NVL và nhà cung cấp." : exception.getMessage());
            return id == null ? "redirect:/nguyen-vat-lieu/create" : "redirect:/nguyen-vat-lieu/" + id;
        }
    }
}
