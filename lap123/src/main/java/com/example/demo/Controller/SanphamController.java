package com.example.demo.Controller;

import com.example.demo.Entity.Sanpham;
import com.example.demo.Entity.Thuonghieu;
import com.example.demo.Service.SanphamService;
import com.example.demo.Service.ThuonghieuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.Service.BomService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Controller
public class SanphamController {

    @Autowired
    private SanphamService sanphamService;

    @Autowired
    private BomService bomService;

    @Autowired
    private ThuonghieuService thuonghieuservice;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @GetMapping("/sanpham")
    public String sanpham(Model model) {
        model.addAttribute("sanpham", sanphamService.getAllSanpham());
        return "sanpham/index";
    }

    @GetMapping("/sanpham/create")
    public String newSanpham(Model model) {
        model.addAttribute("sanpham", new Sanpham());
        model.addAttribute("thuonghieuList", thuonghieuservice.getAllThuonghieu());
        return "sanpham/add";
    }

    @PostMapping("/sanpham")
    public String saveSanpham(@ModelAttribute("sanpham") Sanpham sanpham,
                              @RequestParam(value = "file", required = false) MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            sanpham.setHINHANHSP(storeImage(file));
        }
        sanphamService.saveSanpham(sanpham);
        return "redirect:/sanpham";
    }

    @GetMapping("/edit-sanpham/{id}")
    public String editSanpham(@PathVariable("id") int id, Model model) {
        Sanpham sanpham = sanphamService.getSanphamById(id);
        if (sanpham.getThuonghieu() == null) {
            sanpham.setThuonghieu(new Thuonghieu());
        }
        model.addAttribute("sanpham", sanpham);
        model.addAttribute("thuonghieuList", thuonghieuservice.getAllThuonghieu());
        return "sanpham/edit";
    }

    @PostMapping("/update-sanpham")
    public String updateSanpham(@ModelAttribute Sanpham sanpham,
                                @RequestParam(value = "file", required = false) MultipartFile file) {
        Sanpham existing = sanphamService.getSanphamById(sanpham.getMASP());
        if (file != null && !file.isEmpty()) {
            existing.setHINHANHSP(storeImage(file));
        }

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

    @GetMapping("/delete-sanpham/{id}")
    public String showDeleteForm(@PathVariable int id, Model model) {
        model.addAttribute("sanpham", sanphamService.getSanphamById(id));
        return "sanpham/delete";
    }

    @PostMapping("/delete-sanpham")
    public String deleteSanpham(@RequestParam("id") int id,
                                @RequestParam("GHICHU") String GHICHU) {
        sanphamService.deleteSanpham(id, GHICHU);
        return "redirect:/sanpham";
    }

    private String storeImage(MultipartFile file) {
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = System.currentTimeMillis() + "_" + safeName;
        Path root = Path.of(uploadDir).toAbsolutePath().normalize();
        Path target = root.resolve(fileName).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Tên tệp hình ảnh không hợp lệ.");
        }
        try {
            Files.createDirectories(root);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException exception) {
            throw new IllegalStateException("Không thể lưu hình ảnh sản phẩm.", exception);
        }
    }
}
