package com.example.demo.Controller;

import com.example.demo.Entity.Taikhoan;
import com.example.demo.Repository.TaikhoanRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PersonalAccountController {
    private final TaikhoanRepository accounts;

    public PersonalAccountController(TaikhoanRepository accounts) { this.accounts = accounts; }

    @GetMapping("/tai-khoan/ca-nhan")
    public String profile(Authentication authentication, Model model) {
        Taikhoan account = accounts.findByEmail(authentication.getName());
        if (account == null) throw new IllegalArgumentException("Tài khoản không tồn tại.");
        model.addAttribute("account", account);
        return "quanlytaikhoan/ca-nhan";
    }
}