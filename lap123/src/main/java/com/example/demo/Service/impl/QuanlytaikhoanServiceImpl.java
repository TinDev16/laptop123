package com.example.demo.Service.impl;

import com.example.demo.Entity.Nhanvien;
import com.example.demo.Entity.Taikhoan;
import com.example.demo.Entity.Vaitro;
import com.example.demo.Repository.NhanvienRepository;
import com.example.demo.Repository.TaikhoanRepository;
import com.example.demo.Repository.VaitroRepository;
import com.example.demo.Service.QuanlytaikhoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class QuanlytaikhoanServiceImpl implements QuanlytaikhoanService {

    @Autowired
    private TaikhoanRepository taikhoanRepository;

    @Autowired
    private NhanvienRepository nhanvienRepository;

    @Autowired
    private VaitroRepository vaitroRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public List<Nhanvien> getNhanvienChuaCoTaiKhoan() {
        return nhanvienRepository.findByTaikhoanIsNullAndTRANGTHAI(0);
    }


    @Override
    public void ganQuyenNhanvien(int nhanvienId, int taikhoanId) {
        Nhanvien nhanvien = nhanvienRepository.findById(nhanvienId).orElse(null);
        Taikhoan taikhoan = taikhoanRepository.findById(taikhoanId).orElse(null);

        if (nhanvien != null && taikhoan != null) {
            taikhoan.setNhanvien(nhanvien);
            taikhoanRepository.save(taikhoan);
        }
    }

    @Override
    public List<Taikhoan> getAllTaiKhoan() {
        return taikhoanRepository.findAll();
    }

    @Override
    public void xoaTaiKhoan(int id) {
        Taikhoan taikhoan = taikhoanRepository.findById(id).orElse(null);
        if (taikhoan != null) {
            if (taikhoan.getNhanvien() != null) {
                Nhanvien nv = taikhoan.getNhanvien();
                nv.setTaikhoan(null);
                nhanvienRepository.save(nv);
            }
            taikhoanRepository.delete(taikhoan);
        }
    }

    @Override
    public Taikhoan taoTaiKhoanMoi(Taikhoan taikhoan, int vaitroId) {
        Vaitro vaitro = vaitroRepository.findById(vaitroId).orElse(null);
        taikhoan.setVaitro(vaitro);
        String encodedPassword = passwordEncoder.encode(taikhoan.getPassword());
        taikhoan.setPassword(encodedPassword);
        return taikhoanRepository.save(taikhoan);
    }
}
