package com.example.demo.Service.impl;

import com.example.demo.Entity.Nhanvien;
import com.example.demo.Entity.Sanpham;
import com.example.demo.Repository.NhanvienRepository;
import com.example.demo.Service.NhanvienService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NhanvienServiceImpl implements NhanvienService {

    private final NhanvienRepository nhanvienRepository;
    private NhanvienRepository NhanvienRepository;

    public NhanvienServiceImpl(NhanvienRepository NhanvienRepository, NhanvienRepository nhanvienRepository) {
        super();
        this.NhanvienRepository  = NhanvienRepository;
        this.nhanvienRepository = nhanvienRepository;
    }

    @Override
    public List<Nhanvien> getAllNhanvien() {
        return NhanvienRepository.findAll();
    };

    @Override
    public Nhanvien createNhanvien(Nhanvien Nhanvien) {
        return NhanvienRepository.save(Nhanvien);
    }

    @Override
    public Nhanvien saveNhanvien(Nhanvien Nhanvien) {
        return NhanvienRepository.save(Nhanvien);
    }

    @Override
    public void deleteNhanvien(int id) {
        NhanvienRepository.deleteById(id);
    }

    @Override
    public Nhanvien getNhanvienById(int MANV) {
        Optional<Nhanvien> nhanvien = nhanvienRepository.findById(MANV);
        if (nhanvien.isPresent()) {
            return nhanvien.get();
        } else {
            return null;
        }
    }

    @Override
    public Nhanvien updateNhanvien(Nhanvien Nhanvien) {
        return NhanvienRepository.save(Nhanvien);
    }


}
