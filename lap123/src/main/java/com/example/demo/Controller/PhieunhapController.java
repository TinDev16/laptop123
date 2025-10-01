package com.example.demo.Controller;

import com.example.demo.Entity.Phieunhap;
import com.example.demo.Service.PhieunhapService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PhieunhapController {

    PhieunhapService phieunhapService;

    public PhieunhapController(PhieunhapService phieunhapService) {
        super();
        this.phieunhapService = phieunhapService;
    }

    @GetMapping("/phieunhap")
    public String phieunhap(Model model) {
        model.addAttribute("phieunhap", new Phieunhap());
        return "phieunhap/index";
    }

    @GetMapping("/phieunhap/create")
    public String phieunhapCreate(Model model) {
        model.addAttribute("phieunhap", new Phieunhap());
        return "phieunhap/add";
    }

    @GetMapping("/delete-phieunhap/{id}")
    public String deletePhieunhap(@PathVariable("id") int id) {
        phieunhapService.deletePhieunhap(id);
        return "redirect:/phieunhap";
    }
}
