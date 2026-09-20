package com.example.demo.Service;

import com.example.demo.Entity.Nhanvien;
import com.example.demo.Entity.Taikhoan;
import java.util.List;

public interface QuanlytaikhoanService {
    List<Nhanvien> getNhanvienChuaCoTaiKhoan();
    void ganQuyenNhanvien(int nhanvienId, int taikhoanId);
    List<Taikhoan> getAllTaiKhoan();
    void xoaTaiKhoan(int id, String actorEmail);
    Taikhoan taoTaiKhoanMoi(Taikhoan taikhoan, String roleCode, int nhanvienId);
    void doiVaiTro(int accountId, String roleCode, String actorEmail);
    void datTrangThaiKhoa(int accountId, boolean locked, String actorEmail);
}