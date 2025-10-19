package com.example.demo.Service;

import com.example.demo.Entity.Nhanvien;
import com.example.demo.Entity.Taikhoan;
import java.util.List;

public interface QuanlytaikhoanService {
    List<Nhanvien> getNhanvienChuaCoTaiKhoan();
    void ganQuyenNhanvien(int nhanvienId, int taikhoanId);
    List<Taikhoan> getAllTaiKhoan();
    void xoaTaiKhoan(int id);
    Taikhoan taoTaiKhoanMoi(Taikhoan taikhoan, int vaitroId);
}
