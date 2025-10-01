package com.example.demo.Service.impl;

import com.example.demo.Entity.Phieunhap;
import com.example.demo.Repository.PhieunhapRepository;
import com.example.demo.Service.PhieunhapService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhieunhapServiceImpl implements PhieunhapService {

    PhieunhapRepository phieunhapRepository;

    public PhieunhapServiceImpl (PhieunhapRepository phieunhapRepository) {
        super();
        this.phieunhapRepository = phieunhapRepository;
    }


    @Override
    public List<Phieunhap> getAllPhieunhap() {
        return phieunhapRepository.findAll();
    }



    @Override
    public void deletePhieunhap(int id) {
        phieunhapRepository.deleteById(id);
    }

}
