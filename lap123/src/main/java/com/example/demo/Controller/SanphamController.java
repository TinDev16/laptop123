package com.example.demo.Controller;

import com.example.demo.Entity.Sanpham;
import com.example.demo.Service.SanphamService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class SanphamController {

    private SanphamService sanphamService;

    public SanphamController(SanphamService sanphamService) {
        super();
        this.sanphamService = sanphamService;
    }

    @GetMapping("/sanpham")
    public String listSanpham(Model model) {
        model.addAttribute("sanpham", sanphamService.getAllSanpham());
        return "index";
    }

    @GetMapping("/sanpham/create")
    public String newSanpham(Model model) {
        Sanpham sanpham = new Sanpham();
        model.addAttribute("sanpham", sanpham);
        return "create_Sanpham";
    }

    @PostMapping("/sanpham")
    public String createSanpham(@ModelAttribute("sanpham") Sanpham sanpham,
                                @RequestParam("HINHANHSP") MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            String fileName = file.getOriginalFilename();

            // Thư mục static/images
            Path uploadDir = Paths.get("src/main/resources/static/images");

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Ghi file vào static/images
            Path filePath = uploadDir.resolve(fileName);
            Files.write(filePath, file.getBytes());

            // Lưu tên file vào DB
            sanpham.setHINHANHSP(fileName);
        }

        sanphamService.createSanpham(sanpham);
        return "redirect:/sanpham";
    }




}
