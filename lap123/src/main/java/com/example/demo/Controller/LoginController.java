package com.example.demo.Controller;

import com.example.demo.Entity.Taikhoan;
import com.example.demo.Repository.TaikhoanRepository;
import com.example.demo.Service.LoginService;
import com.example.demo.Service.RegisterService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private LoginService loginService;

    @GetMapping("/")
    public String Index() {
        return "index";
    }

    @GetMapping("/signin")
    public String login(){
        return "login";
    }

    @GetMapping("/forgotPassword")
    public String forgotPassword(HttpSession session, Model model){
        // Lấy msg từ session nếu có
        String msg = (String) session.getAttribute("msg");
        if(msg != null){
            model.addAttribute("msg", msg);
            session.removeAttribute("msg"); // Xóa để lần sau không hiển thị nữa
        }
        return "forgotPassword";
    }

    @PostMapping("/newPassword")
    public String newPassword(@RequestParam("email") String email, HttpSession session) {
        loginService.handleNewPassword(email, session);
        return "redirect:/forgotPassword";
    }

}
