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
import java.nio.file.StandardCopyOption;

@Controller
public class SanphamController {

    private SanphamService sanphamService;

    public SanphamController(SanphamService sanphamService) {
        super();
        this.sanphamService = sanphamService;
    }

//    index
    @GetMapping("/")
    public String home() {
        return "index";
    }

//    trang chủ sanpham
    @GetMapping("/sanpham")
    public String sanpham(Model model) {
        model.addAttribute("sanpham", sanphamService.getAllSanpham());
        return "sanpham/index";
    }

    @GetMapping("/sanpham/create")
    public String newSanpham(Model model) {
        Sanpham sanpham = new Sanpham();
        model.addAttribute("sanpham", sanpham);
        return "sanpham/add";
    }


    @PostMapping("/sanpham")
    public String saveSanpham(@ModelAttribute("sanpham") Sanpham sanpham,
                              @RequestParam("file") MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                // Tạo thư mục lưu file
                String uploadDir = "uploads/";

                // Lấy tên file gốc
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

                // Tạo path lưu
                Path path = Paths.get(uploadDir + fileName);

                // Nếu thư mục chưa có thì tạo
                Files.createDirectories(path.getParent());

                // Lưu file
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                // Lưu đường dẫn vào entity
                sanpham.setHINHANHSP(fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        sanphamService.saveSanpham(sanpham);
        return "redirect:/sanpham";
    }

    @GetMapping("/edit/{id}")
    public String editSanpham(@PathVariable("id") int id, Model model) {
        model.addAttribute("sanpham", sanphamService.getSanphamById(id));
        return "sanpham/edit";
    }

    @PostMapping("/update")
    public String updateSanpham(@ModelAttribute Sanpham sanpham) {
        sanphamService.updateSanpham(sanpham);
        return "redirect:/sanpham";
    }

    @GetMapping("/view/{id}")
    public String viewSanpham(@PathVariable("id") int id, Model model) {
        model.addAttribute("sanpham", sanphamService.getSanphamById(id));
        return "sanpham/detail";
    }

    @GetMapping("/delete/{id}")
    public String deleteSanpham(@PathVariable("id") int id) {
        sanphamService.deleteSanpham(id);
        return "redirect:/sanpham";
    }

//    @PostMapping("/update/{id}")
//    public String updateSanpham(@PathVariable("id") int id,
//                                @ModelAttribute("sanpham") Sanpham sanpham,
//                                @RequestParam("file") MultipartFile file) {
//        try {
//            // Lấy sản phẩm cũ từ DB
//            Sanpham existing = sanphamService.getSanphamById(id);
//
//            // Cập nhật các field
//            existing.setTENSP(sanpham.getTENSP());
//            existing.setXUATXU(sanpham.getXUATXU());
//            existing.setCHIP(sanpham.getCHIP());
//            existing.setHEDIEUHANH(sanpham.getHEDIEUHANH());
//            existing.setKICHTHUOCMAN(sanpham.getKICHTHUOCMAN());
//            existing.setTHUONGHIEU(sanpham.getTHUONGHIEU());
//            existing.setTRANGTHAI(sanpham.getTRANGTHAI());
//
//            // Nếu có file ảnh mới thì lưu, ngược lại giữ ảnh cũ
//            if (!file.isEmpty()) {
//                String uploadDir = "uploads/";
//                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
//                Path path = Paths.get(uploadDir + fileName);
//
//                Files.createDirectories(path.getParent());
//                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//
//                existing.setHINHANHSP(fileName);
//            }
//
//            // Lưu lại vào DB
//            sanphamService.updateSanpham(existing);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return "redirect:/sanpham";
//    }




}
