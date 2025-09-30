package com.example.demo.Controller;

import com.example.demo.Entity.Khachhang;
import com.example.demo.Service.KhachhangService;
import com.example.demo.Service.impl.KhachhangServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class KhachhangController {

    private KhachhangService khachhangService;
    public KhachhangController(KhachhangService khachhangService) {
        super();
        this.khachhangService = khachhangService;
    }

    @GetMapping("/khachhang")
    public String khachhang(Model model) {
        model.addAttribute("khachhang", new Khachhang()); // tránh null
        model.addAttribute("khachhang", khachhangService.getAllKhachhang());
        return "khachhang/index";
    }

    @GetMapping("/khachhang/create")
    public String khachhangCreate(Model model) {
        Khachhang khachhang = new Khachhang();
        model.addAttribute("khachhang", khachhang);
        return "khachhang/add";
    }

    @PostMapping("/khachhang")
    public String saveKhachhang(@ModelAttribute("khachhang") Khachhang khachhang) {
        khachhangService.saveKhachhang(khachhang);
        return "redirect:/khachhang";
    }

    @GetMapping("/edit-khachhang/{id}")
    public String editKhachhang(@PathVariable int id, Model model) {
        model.addAttribute("khachhang", khachhangService.getKhachhangById(id));
        return "khachhang/edit";
    }

    @PostMapping("/update-khachhang")
    public String updateKhachhang(@ModelAttribute Khachhang khachhang) {
        khachhangService.updateKhachhang(khachhang);
        return "redirect:/khachhang";
    }

    @GetMapping("/delete-khachhang/{id}")
    public String deleteKhachhang(@PathVariable("id") int id) {
        khachhangService.deleteKhachhangById(id);
        return "redirect:/khachhang";
    }
}
