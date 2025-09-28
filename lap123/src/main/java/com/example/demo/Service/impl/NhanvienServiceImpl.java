package com.example.demo.Service.impl;

import com.example.demo.Entity.Nhanvien;
import com.example.demo.Repository.NhanvienRepository;
import com.example.demo.Service.NhanvienService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NhanvienServiceImpl implements NhanvienService {

    private NhanvienRepository NhanvienRepository;

    public NhanvienServiceImpl(NhanvienRepository NhanvienRepository) {
        super();
        this.NhanvienRepository  = NhanvienRepository;
    }

    @Override
    public List<Nhanvien> getAllNhanvien() {
        return NhanvienRepository.findAll();
    };
}
