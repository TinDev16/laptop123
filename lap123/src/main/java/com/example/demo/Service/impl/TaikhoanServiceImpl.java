package com.example.demo.Service.impl;

import com.example.demo.Entity.Taikhoan;
import com.example.demo.Repository.TaikhoanRepository;
import com.example.demo.Service.TaikhoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaikhoanServiceImpl implements TaikhoanService {

    @Autowired
    private TaikhoanRepository taikhoanRepository;

    @Override
    public void updateTaikhoanResetToken(String email, String resetToken) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email);
        if (taikhoan != null) {
            taikhoan.setResetToken(resetToken);
            taikhoanRepository.save(taikhoan);
        }
    }

    @Override
    public Taikhoan getByResetToken(String token) {
        return taikhoanRepository.findByResetToken(token);
    }

    @Override
    public Taikhoan updateTaikhoan(Taikhoan taikhoan) {
        return taikhoanRepository.save(taikhoan);
    }

    @Override
    public Taikhoan getByEmail(String email) {
        return taikhoanRepository.findByEmail(email);
    }

}