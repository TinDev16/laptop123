package com.example.demo.Service.impl;

import com.example.demo.Entity.CT_Phieuxuat;
import com.example.demo.Repository.CT_PhieuxuatRepository;
import com.example.demo.Service.CT_PhieuxuatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CT_PhieuxuatServiceImpl implements CT_PhieuxuatService {

    @Autowired
    CT_PhieuxuatRepository ctPhieuxuatRepository;

    @Override
    public void save(CT_Phieuxuat ct) {
        ctPhieuxuatRepository.save(ct);
    }

    @Override
    public CT_Phieuxuat getById(int id) {
        return ctPhieuxuatRepository.findById(id).orElse(null);
    }
}
