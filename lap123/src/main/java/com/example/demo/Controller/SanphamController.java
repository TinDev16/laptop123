package com.example.demo.Controller;

import com.example.demo.Entity.Sanpham;
import com.example.demo.Entity.Thuonghieu;
import com.example.demo.Service.SanphamService;
import com.example.demo.Service.ThuonghieuService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private SanphamService sanphamService;

    @Autowired
    private ThuonghieuService thuonghieuservice;

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
        Sanpham sanpham = sanphamService.getSanphamById(id);
        if (sanpham.getThuonghieu() == null) {
            sanpham.setThuonghieu(new Thuonghieu());
        }

        return "sanpham/edit";
    }

    @PostMapping("/update-sanpham")
    public String updateSanpham(@ModelAttribute Sanpham sanpham,
                                @RequestParam(value = "file", required = false) MultipartFile file) {

        // Lấy sản phẩm cũ
        Sanpham existing = sanphamService.getSanphamById(sanpham.getMASP());

        // Nếu có upload ảnh mới
        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = "src/main/resources/static/uploads/";
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

                Path path = Paths.get(uploadDir + fileName);
                Files.createDirectories(path.getParent());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                existing.setHINHANHSP(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        // Cập nhật các trường khác
        existing.setTENSP(sanpham.getTENSP());
        existing.setXUATXU(sanpham.getXUATXU());
        existing.setCHIP(sanpham.getCHIP());
        existing.setHEDIEUHANH(sanpham.getHEDIEUHANH());
        existing.setKICHTHUOCMAN(sanpham.getKICHTHUOCMAN());
        existing.setThuonghieu(sanpham.getThuonghieu());
        existing.setTRANGTHAI(sanpham.getTRANGTHAI());
        existing.setGHICHU(sanpham.getGHICHU());

        sanphamService.updateSanpham(existing);
        return "redirect:/sanpham";
    }

    @GetMapping("/view-sanpham/{id}")
    public String viewSanpham(@PathVariable("id") int id, Model model) {
        model.addAttribute("sanpham", sanphamService.getSanphamById(id));
        return "sanpham/detail";
    }
//

    // GET - Hiển thị form xác nhận xóa (nếu bạn có form nhập lý do xóa)
    @GetMapping("/delete-sanpham/{id}")
    public String showDeleteForm(@PathVariable int id, Model model) {
        Sanpham sanpham = sanphamService.getSanphamById(id);
        model.addAttribute("sanpham", sanpham);
        return "sanpham/delete"; // Trang confirm xóa có ô nhập ghi chú
    }

    // POST - Xử lý khi người dùng xác nhận xóa
    @PostMapping("/delete-sanpham")
    public String deleteSanpham(@RequestParam("id") int id,
                                @RequestParam("GHICHU") String GHICHU) {
        sanphamService.deleteSanpham(id, GHICHU);
        return "redirect:/sanpham";
    }








}
