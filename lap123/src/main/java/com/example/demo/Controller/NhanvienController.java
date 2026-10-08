package com.example.demo.Controller;

import com.example.demo.Service.NhanvienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NhanvienController {

    @Autowired
    private NhanvienService nhanvienService;

    @GetMapping("/nhanvien")
    public String listNhanvien(Model model) {
        model.addAttribute(
                "nhanvien",
                nhanvienService.getAllNhanvien()
        );

        return "nhanvien/index";
    }
}