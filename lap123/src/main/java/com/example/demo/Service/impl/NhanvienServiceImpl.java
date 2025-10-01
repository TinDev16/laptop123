package com.example.demo.Service.impl;

import com.example.demo.Entity.Nhanvien;
import com.example.demo.Repository.NhanvienRepository;
import com.example.demo.Service.NhanvienService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NhanvienServiceImpl implements NhanvienService {

    private final NhanvienRepository nhanvienRepository;

    public NhanvienServiceImpl(NhanvienRepository nhanvienRepository) {
        super();
        this.nhanvienRepository = nhanvienRepository;
    }

    @Override
    public List<Nhanvien> getAllNhanvien() {
        return nhanvienRepository.findAll();
    }

    @Override
    public Nhanvien createNhanvien(Nhanvien Nhanvien) {
        return nhanvienRepository.save(Nhanvien);
    }

    @Override
    public Nhanvien saveNhanvien(Nhanvien nhanvien) {
        return nhanvienRepository.save(nhanvien);
    }

    @Override
    public void deleteNhanvien(int id) {
        nhanvienRepository.deleteById(id);
    }

    @Override
    public Nhanvien getNhanvienById(int MANV) {
        Optional<Nhanvien> nhanvien = nhanvienRepository.findById(MANV);
        return nhanvien.orElse(null);
    }

    @Override
    public Nhanvien updateNhanvien(Nhanvien nhanvien) {
        return nhanvienRepository.save(nhanvien);
    }
}
