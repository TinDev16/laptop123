package com.example.demo.Service;

import com.example.demo.Entity.Sanpham;

import java.util.List;

public interface SanphamService {
    List<Sanpham> getAllSanpham();

    Sanpham saveSanpham(Sanpham sanpham);

}
