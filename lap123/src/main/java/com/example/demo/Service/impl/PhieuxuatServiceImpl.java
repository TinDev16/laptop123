package com.example.demo.Service.impl;

import com.example.demo.Entity.Phieuxuat;
import com.example.demo.Repository.PhieuxuatRepository;
import com.example.demo.Service.PhieuxuatService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhieuxuatServiceImpl implements PhieuxuatService {

    PhieuxuatRepository phieuxuatRepository;

    public PhieuxuatServiceImpl (PhieuxuatRepository phieuxuatRepository) {
        super();
        this.phieuxuatRepository = phieuxuatRepository;
    }

    @Override
    public List<Phieuxuat> getAllPhieuxuat() {
        return phieuxuatRepository.findAll();
    }


}
