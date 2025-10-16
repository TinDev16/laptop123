package com.example.demo.Service;

import com.example.demo.Entity.Nhanvien;
import java.util.List;

public interface NhanvienService {

    List<Nhanvien> getAllNhanvien();

    Nhanvien createNhanvien(Nhanvien Nhanvien);

    Nhanvien saveNhanvien(Nhanvien nhanvien);

    Nhanvien getNhanvienById(int id);

    Nhanvien updateNhanvien(Nhanvien nhanvien);

    void deleteNhanvien(int id, String GHICHU);

}
