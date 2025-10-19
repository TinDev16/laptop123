package com.example.demo.Service.impl;

import com.example.demo.Entity.CT_Phieunhap;
import com.example.demo.Repository.CT_PhieunhapRepository;
import com.example.demo.Service.CT_PhieunhapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CT_PhieunhapSericeImpl implements CT_PhieunhapService {


    @Autowired
    CT_PhieunhapRepository CT_PhieunhapRepository;

    @Override
    public void save(CT_Phieunhap ct){
        CT_PhieunhapRepository.save(ct);
    }

    @Override
    public CT_Phieunhap getById(int id) {
        return CT_PhieunhapRepository.findById(id).orElse(null);
    }
}
