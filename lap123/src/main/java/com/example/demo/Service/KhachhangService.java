package com.example.demo.Service;

import com.example.demo.Entity.Khachhang;

import java.util.List;

public interface KhachhangService {

    List<Khachhang> getAllKhachhang();

    Khachhang createKhachhang(Khachhang khachhang);

    Khachhang saveKhachhang(Khachhang khachhang);

    Khachhang getKhachhangById(int id);

    Khachhang updateKhachhang(Khachhang khachhang);

    void deleteKhachhang(int id, String GHICHU);
}
