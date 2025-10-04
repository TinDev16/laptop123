package com.example.demo.Controller;

import com.example.demo.Entity.Sanpham;
import com.example.demo.Entity.Thuonghieu;
import com.example.demo.Service.SanphamService;
import com.example.demo.Service.ThuonghieuService;
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
    private ThuonghieuService thuonghieuservice;

    public SanphamController(SanphamService sanphamService, ThuonghieuService thuonghieuservice) {
        super();
        this.sanphamService = sanphamService;
        this.thuonghieuservice = thuonghieuservice;
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

//    TẠO SP MỚI
    @GetMapping("/sanpham/create")
    public String newSanpham(Model model) {
        Sanpham sanpham = new Sanpham();
        model.addAttribute("sanpham", sanpham);
        model.addAttribute("thuonghieuList", thuonghieuservice.getAllThuonghieu());
        return "sanpham/add";
    }

    @PostMapping("/sanpham")
    public String saveSanpham(@ModelAttribute("sanpham") Sanpham sanpham,
                              @RequestParam("file") MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                // Thư mục trong static
                String uploadDir = "src/main/resources/static/uploads/";

                // Lấy tên file gốc + gắn timestamp tránh trùng
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

                // Tạo path đầy đủ
                Path path = Paths.get(uploadDir + fileName);

                // Nếu thư mục chưa có thì tạo
                Files.createDirectories(path.getParent());

                // Lưu file
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                // Lưu tên file (không cần cả path) vào DB
                sanpham.setHINHANHSP(fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        sanphamService.saveSanpham(sanpham);
        return "redirect:/sanpham";
    }
//

//  SỬA SP
    @GetMapping("/edit-sanpham/{id}")
    public String editSanpham(@PathVariable("id") int id, Model model) {
        model.addAttribute("sanpham", sanphamService.getSanphamById(id));
        model.addAttribute("thuonghieuList", thuonghieuservice.getAllThuonghieu());
        Sanpham sp = sanphamService.getSanphamById(id);
        System.out.println("Sản phẩm load được: " + sp);
        model.addAttribute("sanpham", sp);

        return "sanpham/edit";
    }

    @PostMapping("/update-sanpham")
    public String updateSanpham(@ModelAttribute Sanpham sanpham) {
        sanphamService.updateSanpham(sanpham);
        return "redirect:/sanpham";
    }

    @GetMapping("/view/{id}")
    public String viewSanpham(@PathVariable("id") int id, Model model) {
        model.addAttribute("sanpham", sanphamService.getSanphamById(id));
        return "sanpham/detail";
    }
//

//    XÓA SP
    @GetMapping("/delete-sanpham/{id}")
    public String deleteSanpham(@PathVariable("id") int id) {
        sanphamService.deleteSanpham(id);
        return "redirect:/sanpham";
    }





}
