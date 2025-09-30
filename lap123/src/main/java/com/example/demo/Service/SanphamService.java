package com.example.demo.Service;

import com.example.demo.Entity.Nhanvien;
import com.example.demo.Entity.Sanpham;

import java.util.List;

public interface SanphamService {
    List<Sanpham> getAllSanpham();

    Sanpham createSanpham(Sanpham sanpham);

    Sanpham saveSanpham(Sanpham sanpham);

    Sanpham getSanphamById(int id);

    Sanpham updateSanpham(Sanpham sanpham);

    void deleteSanpham(int id);
}
