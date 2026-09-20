package com.example.demo.Controller;

import com.example.demo.Entity.Taikhoan;
import com.example.demo.Service.QuanlytaikhoanService;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/quanlytaikhoan")
public class QuanlytaikhoanController {
    private final QuanlytaikhoanService accounts;

    public QuanlytaikhoanController(QuanlytaikhoanService accounts) { this.accounts = accounts; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("listTaiKhoan", accounts.getAllTaiKhoan());
        return "quanlytaikhoan/quanlytaikhoan";
    }

    @GetMapping("/themtaikhoanquantri")
    public String createForm(Model model) {
        model.addAttribute("employees", accounts.getNhanvienChuaCoTaiKhoan());
        return "quanlytaikhoan/themtaikhoanquantri";
    }

    @PostMapping("/newAccount")
    public String create(@RequestParam String email, @RequestParam String password,
                         @RequestParam(defaultValue = "NHAN_VIEN") String roleCode,
                         @RequestParam int nhanvienId, RedirectAttributes flash) {
        try {
            Taikhoan account = new Taikhoan();
            account.setEmail(email);
            account.setPassword(password);
            accounts.taoTaiKhoanMoi(account, roleCode, nhanvienId);
            flash.addFlashAttribute("success", "Tạo tài khoản thành công.");
            return "redirect:/quanlytaikhoan";
        } catch (IllegalArgumentException | IllegalStateException error) {
            flash.addFlashAttribute("error", error.getMessage());
            return "redirect:/quanlytaikhoan/themtaikhoanquantri";
        }
    }

    @PostMapping("/doi-vai-tro")
    public String changeRole(@RequestParam int id, @RequestParam String roleCode, Authentication auth, RedirectAttributes flash) {
        try { accounts.doiVaiTro(id, roleCode, auth.getName()); flash.addFlashAttribute("success", "Đã cập nhật vai trò."); }
        catch (IllegalArgumentException error) { flash.addFlashAttribute("error", error.getMessage()); }
        return "redirect:/quanlytaikhoan";
    }

    @PostMapping("/khoa")
    public String lock(@RequestParam int id, @RequestParam boolean locked, Authentication auth, RedirectAttributes flash) {
        accounts.datTrangThaiKhoa(id, locked, auth.getName());
        flash.addFlashAttribute("success", locked ? "Đã khóa tài khoản." : "Đã mở khóa tài khoản.");
        return "redirect:/quanlytaikhoan";
    }

    @PostMapping("/xoataikhoan")
    public String delete(@RequestParam int id, Authentication auth) {
        accounts.xoaTaiKhoan(id, auth.getName());
        return "redirect:/quanlytaikhoan";
    }

    @GetMapping("/capquyennhanvien")
    public String assignForm(@RequestParam("id") int accountId, Model model) {
        model.addAttribute("listNv", accounts.getNhanvienChuaCoTaiKhoan());
        model.addAttribute("taikhoanId", accountId);
        return "quanlytaikhoan/capquyennhanvien";
    }

    @PostMapping("/luucapquyen")
    public String assign(@RequestParam int nhanvienId, @RequestParam int taikhoanId) {
        accounts.ganQuyenNhanvien(nhanvienId, taikhoanId);
        return "redirect:/quanlytaikhoan";
    }
}