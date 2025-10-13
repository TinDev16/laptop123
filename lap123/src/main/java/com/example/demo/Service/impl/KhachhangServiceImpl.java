package com.example.demo.Service.impl;

import com.example.demo.Entity.Khachhang;
import com.example.demo.Entity.Nhanvien;
import com.example.demo.Repository.KhachhangRepository;
import com.example.demo.Service.KhachhangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KhachhangServiceImpl implements KhachhangService {

    private KhachhangRepository khachhangRepository;

    public KhachhangServiceImpl(KhachhangRepository khachhangRepository) {
        super();
        this.khachhangRepository = khachhangRepository;
    }

    @Override
    public List<Khachhang> getAllKhachhang() {
        return khachhangRepository.findAll();
    }

    @Override
    public Khachhang createKhachhang(Khachhang khachhang) {
        return khachhangRepository.save(khachhang);
    }

    @Override
    public Khachhang saveKhachhang(Khachhang khachhang) {
        return khachhangRepository.save(khachhang);
    }

    @Override
    public Khachhang getKhachhangById(int MAKH) {
        Optional<Khachhang> nhanvien = khachhangRepository.findById(MAKH);
        if (nhanvien.isPresent()) {
            return nhanvien.get();
        } else {
            return null;
        }
    }

    @Override
    public Khachhang updateKhachhang(Khachhang khachhang) {
        return khachhangRepository.save(khachhang);
    }

    @Override
    public void deleteKhachhang(int id, String GHICHU) {
        Khachhang kh = khachhangRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với id=" + id));

        kh.setTRANGTHAI(1);
        kh.setGHICHU(GHICHU);
        khachhangRepository.save(kh);
    }

}
