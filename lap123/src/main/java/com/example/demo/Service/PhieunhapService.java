package com.example.demo.Service;

import com.example.demo.Entity.Phieunhap;

import java.util.List;

public interface PhieunhapService {

    List<Phieunhap> getAllPhieunhap();

    Phieunhap getPhieunhapById(int MAPHIEUNHAP);

    void deletePhieunhap(int id);
}
