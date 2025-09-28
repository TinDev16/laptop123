package com.example.demo.Controller;

import com.example.demo.Entity.Sanpham;
import com.example.demo.Service.SanphamService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class SanphamController {

    private SanphamService sanphamService;

    public SanphamController(SanphamService sanphamService) {
        super();
        this.sanphamService = sanphamService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/sanpham_add")
    public String sanpham_edit() {
        return "sanpham/add";
    }

    @GetMapping("/sanpham")
    public String sanpham(Model model) {
        model.addAttribute("sanpham", sanphamService.getAllSanpham());
        return "sanpham/index";
    }

    @PostMapping("/register")
    public String list(Model model) {
         return "sanpham/add";
    }



    @GetMapping("/sanpham/create")
    public String createSanpham(Model model) {
        Sanpham sanpham = new Sanpham();
//        model.addAttribute("sanpham", sanpham);
        return "create_Sanpham";
    }

//    @PostMapping("/sanpham")
//    public String saveSanpham(@ModelAttribute("sanpham") Sanpham sanpham,
//                              @RequestParam("file") MultipartFile file) {
//        try {
//            if (!file.isEmpty()) {
//                // Tạo thư mục lưu file
//                String uploadDir = "uploads/";
//
//                // Lấy tên file gốc
//                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
//
//                // Tạo path lưu
//                Path path = Paths.get(uploadDir + fileName);
//
//                // Nếu thư mục chưa có thì tạo
//                Files.createDirectories(path.getParent());
//
//                // Lưu file
//                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//
//                // Lưu đường dẫn vào entity
//                sanpham.setHINHANHSP(fileName);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        sanphamService.saveSanpham(sanpham);
//        return "redirect:/sanpham";
//    }
//





}
