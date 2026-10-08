package com.example.demo.Controller;

import com.example.demo.Service.QuanlytaikhoanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/quanlytaikhoan")
public class QuanlytaikhoanController {

    private final QuanlytaikhoanService accounts;

    public QuanlytaikhoanController(
            QuanlytaikhoanService accounts) {

        this.accounts = accounts;
    }

    @GetMapping
    public String list(Model model) {

        model.addAttribute(
                "listTaiKhoan",
                accounts.getAllTaiKhoan()
        );

        return "quanlytaikhoan/quanlytaikhoan";
    }
}