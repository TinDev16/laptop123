package com.example.demo.Service.impl;

import com.example.demo.Entity.Sanpham;
import com.example.demo.Repository.SanphamRepository;
import com.example.demo.Service.SanphamService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SanphamServiceImpl implements SanphamService {

    private SanphamRepository sanphamRepository;

    public SanphamServiceImpl(SanphamRepository sanphamRepository) {
        super();
        this.sanphamRepository = sanphamRepository;
    }

    @Override
    public List<Sanpham> getAllSanpham() {
        return sanphamRepository.findAll();
    }

    @Override
    public Sanpham saveSanpham(Sanpham sanpham) {
        return sanphamRepository.save(sanpham);
    }

}
