package com.example.demo.Service.impl;

import com.example.demo.Entity.Taikhoan;
import com.example.demo.Repository.TaikhoanRepository;
import com.example.demo.Service.RegisterService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class RegisterServiceImpl implements RegisterService {

    @Autowired
    private TaikhoanRepository taikhoanRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public Taikhoan saveAccount(Taikhoan taikhoan) {

        String password=passwordEncoder.encode(taikhoan.getPassword());
        taikhoan.setPassword(password);

        Taikhoan newuser = taikhoanRepository.save(taikhoan);

        return newuser;
    }

    @Override
    public void removeSessionMessage() {
        HttpSession session = ((ServletRequestAttributes)(RequestContextHolder.getRequestAttributes())).getRequest().getSession();
        session.removeAttribute("msg");
    }

    @Override
    public boolean emailExists(String email) {
        return taikhoanRepository.findByEmail(email) != null ;
    }

}
