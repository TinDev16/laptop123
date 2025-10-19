package com.example.demo.Service.impl;

import com.example.demo.Entity.Taikhoan;
import com.example.demo.Repository.TaikhoanRepository;
import com.example.demo.Service.LoginService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private TaikhoanRepository taikhoanRepository;

    @Override
    public void handleNewPassword(String email, HttpSession session) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email);

        if (taikhoan == null) {
            session.setAttribute("msg", "Email không tồn tại");
        } else {
            session.setAttribute("msg", "Email đã được xác minh, hãy kiểm tra email để đặt lại mật khẩu.");
        }
    }
}
