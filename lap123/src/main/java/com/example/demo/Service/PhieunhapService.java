package com.example.demo.Service;

import com.example.demo.Entity.Phieunhap;

import java.util.List;

public interface PhieunhapService {

    List<Phieunhap> getAllPhieunhap();

    void deletePhieunhap(int id);
}
