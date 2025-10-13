package com.example.demo.Controller;

import com.example.demo.Service.PhieuxuatService;
import jakarta.persistence.GeneratedValue;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PhieuxuatController {

    PhieuxuatService phieuxuatService;

    public PhieuxuatController(PhieuxuatService phieuxuatService) {
        this.phieuxuatService = phieuxuatService;
    }

    @GetMapping("/phieuxuat")
    public String phieuxuat(Model model) {
        model.addAttribute("phieuxuat", phieuxuatService.getAllPhieuxuat());
        return "phieuxuat/index";
    }
}
