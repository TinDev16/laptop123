package com.example.demo.Controller;

import com.example.demo.Entity.Taikhoan;
import com.example.demo.Service.QuanlytaikhoanService;
import com.example.demo.Service.RegisterService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/quanlytaikhoan")
public class QuanlytaikhoanController {

    @Autowired
    private QuanlytaikhoanService quanlytaikhoanService;

    @Autowired
    private RegisterService registerService;

    @GetMapping("/capquyennhanvien")
    public String capQuyenForm(@RequestParam("id") int taikhoanId, Model model) {
        model.addAttribute("listNv", quanlytaikhoanService.getNhanvienChuaCoTaiKhoan());
        model.addAttribute("taikhoanId", taikhoanId);
        return "quanlytaikhoan/capquyennhanvien";
    }

    @PostMapping("/luucapquyen")
    public String luuCapQuyen(@RequestParam("nhanvienId") int nhanvienId,
                              @RequestParam("taikhoanId") int taikhoanId) {
        quanlytaikhoanService.ganQuyenNhanvien(nhanvienId, taikhoanId);
        return "redirect:/quanlytaikhoan";
    }

    @GetMapping("/themtaikhoanquantri")
    public String themtaikhoan() {
        return "quanlytaikhoan/themtaikhoanquantri";
    }

    @GetMapping("")
    public String quanlytaikhoan(Model model) {
        model.addAttribute("listTaiKhoan", quanlytaikhoanService.getAllTaiKhoan());
        return "quanlytaikhoan/quanlytaikhoan";
    }

    @PostMapping("/xoataikhoan")
    public String xoataikhoan(@RequestParam int id) {
        quanlytaikhoanService.xoaTaiKhoan(id);
        return "redirect:/quanlytaikhoan";
    }

    @PostMapping("/newAccount")
    public String newAccount(@ModelAttribute Taikhoan taikhoan, HttpSession session,
                             @RequestParam("vaitro_id") int vaitroid) {

        if (registerService.emailExists(taikhoan.getEmail())) {
            session.setAttribute("msg", "Email đã tồn tại!");
            return "redirect:/quanlytaikhoan/themtaikhoanquantri";
        }

        Taikhoan u = quanlytaikhoanService.taoTaiKhoanMoi(taikhoan, vaitroid);

        session.setAttribute("msg", u != null ?
                "Tạo tài khoản thành công" : "Lỗi hệ thống");

        return "redirect:/quanlytaikhoan/themtaikhoanquantri";
    }
}
