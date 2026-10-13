package com.example.demo.Controller;

import com.example.demo.Entity.Khachhang;
import com.example.demo.Service.KhachhangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class KhachhangController {

    @Autowired
    private KhachhangService khachhangService;

    // Khách hàng đang hoạt động
    @GetMapping("/khachhang")
    public String khachhang(Model model) {
        model.addAttribute("khachhang", new Khachhang());
        model.addAttribute("danhSachKhachHang",
                khachhangService.getAllKhachhang());
        model.addAttribute("trangThaiDanhSach", 0);

        return "khachhang/index";
    }

    // Khách hàng không còn sử dụng
    @GetMapping("/khachhang/ngung-su-dung")
    public String khachhangNgungSuDung(Model model) {
        model.addAttribute("khachhang", new Khachhang());
        model.addAttribute("danhSachKhachHang",
                khachhangService.getAllKhachhang());
        model.addAttribute("trangThaiDanhSach", 1);

        return "khachhang/index";
    }

    @GetMapping("/khachhang/create")
    public String khachhangCreate(Model model) {
        model.addAttribute("khachhang", new Khachhang());
        return "khachhang/add";
    }

    @PostMapping("/khachhang")
    public String saveKhachhang(
            @ModelAttribute("khachhang") Khachhang khachhang) {

        khachhangService.saveKhachhang(khachhang);
        return "redirect:/khachhang";
    }

    @GetMapping("/edit-khachhang/{id}")
    public String editKhachhang(
            @PathVariable("id") int id, Model model) {

        model.addAttribute("khachhang",
                khachhangService.getKhachhangById(id));

        return "khachhang/edit";
    }

    @PostMapping("/update-khachhang")
    public String updateKhachhang(
            @ModelAttribute Khachhang khachhang) {

        khachhangService.updateKhachhang(khachhang);
        return "redirect:/khachhang";
    }

    // Ngừng sử dụng khách hàng
    @PostMapping("/delete-khachhang")
    public String deleteKhachhang(
            @RequestParam("id") int id,
            @RequestParam("GHICHU") String GHICHU) {

        System.out.println("MAKH nhận được: " + id);
        System.out.println("GHICHU nhận được: [" + GHICHU + "]");

        khachhangService.deleteKhachhang(id, GHICHU);

        return "redirect:/khachhang";
    }

    // Khôi phục khách hàng
    @PostMapping("/khachhang/khoi-phuc")
    public String khoiPhucKhachhang(
            @RequestParam("id") int id) {

        khachhangService.restoreKhachhang(id);

        return "redirect:/khachhang";
    }
}