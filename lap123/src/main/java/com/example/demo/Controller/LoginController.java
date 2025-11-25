package com.example.demo.Controller;

import com.example.demo.Entity.Taikhoan;
import com.example.demo.Service.LoginService;
import com.example.demo.Util.CommonUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.demo.Service.TaikhoanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Controller
public class LoginController {

    @Autowired
    private LoginService loginService;

    @GetMapping("/")
    public String Index() {
        return "index";
    }

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private TaikhoanService taikhoanService;

    @GetMapping("/signin")
    public String login(){
        return "login";
    }

    @GetMapping("/forgotPassword")
    public String forgotPassword(HttpSession session, Model model) {
        if (session.getAttribute("succMsg") != null) {
            model.addAttribute("succMsg", session.getAttribute("succMsg"));
            session.removeAttribute("succMsg");
        }
        if (session.getAttribute("errorMsg") != null) {
            model.addAttribute("errorMsg", session.getAttribute("errorMsg"));
            session.removeAttribute("errorMsg");
        }
        return "forgotPassword";
    }


    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, HttpSession session, HttpServletRequest request)
            throws UnsupportedEncodingException, MessagingException {

        Taikhoan taikhoan = taikhoanService.getByEmail(email);

        if (taikhoan == null) {
            session.setAttribute("errorMsg", "Email không hợp lệ");
        } else {

            // tạo reset token
            String resetToken = UUID.randomUUID().toString();
            taikhoan.setResetToken(resetToken);
            taikhoanService.updateTaikhoan(taikhoan); // cập nhật token vào DB

            // tạo link reset
            String url = CommonUtil.generateUrl(request) + "/reset-password?token=" + resetToken;

            Boolean sendMail = commonUtil.sendMail(url, email);

            if (sendMail) {
                session.setAttribute("succMsg", "Vui lòng kiểm tra email. Link đặt lại mật khẩu đã được gửi.");
            } else {
                session.setAttribute("errorMsg", "Có lỗi xảy ra trên server! Email không gửi được.");
            }
        }

        return "redirect:/forgotPassword";
    }

    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam String token, HttpSession session, Model model) {

        Taikhoan taikhoan = taikhoanService.getByResetToken(token);

        if (taikhoan == null) {
            model.addAttribute("msg", "Link của bạn không hợp lệ hoặc đã hết hạn!");
            return "message";
        }
        model.addAttribute("token", token);
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token, @RequestParam String password, HttpSession session,
                                Model model) {

        Taikhoan taikhoan = taikhoanService.getByResetToken(token);

        if (taikhoan == null) {
            model.addAttribute("errorMsg", "Link của bạn không hợp lệ hoặc đã hết hạn!");
            return "message";
        } else {
            // mã hóa mật khẩu mới và reset token
            taikhoan.setPassword(passwordEncoder.encode(password));
            taikhoan.setResetToken(null);
            taikhoanService.updateTaikhoan(taikhoan);

            model.addAttribute("msg", "Đổi mật khẩu thành công");
            return "message";
        }
    }




}
