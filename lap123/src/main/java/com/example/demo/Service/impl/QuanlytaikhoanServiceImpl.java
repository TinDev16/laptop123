package com.example.demo.Service.impl;

import com.example.demo.Entity.Nhanvien;
import com.example.demo.Entity.Taikhoan;
import com.example.demo.Entity.Vaitro;
import com.example.demo.Repository.NhanvienRepository;
import com.example.demo.Repository.TaikhoanRepository;
import com.example.demo.Repository.VaitroRepository;
import com.example.demo.Service.QuanlytaikhoanService;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuanlytaikhoanServiceImpl implements QuanlytaikhoanService {
    private final TaikhoanRepository accounts;
    private final NhanvienRepository employees;
    private final VaitroRepository roles;
    private final BCryptPasswordEncoder passwordEncoder;

    public QuanlytaikhoanServiceImpl(TaikhoanRepository accounts, NhanvienRepository employees,
                                      VaitroRepository roles, BCryptPasswordEncoder passwordEncoder) {
        this.accounts = accounts;
        this.employees = employees;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
    }

    @Override public List<Nhanvien> getNhanvienChuaCoTaiKhoan() { return employees.findNhanVienChuaCoTaiKhoan(); }
    @Override public List<Taikhoan> getAllTaiKhoan() { return accounts.findAll(); }

    @Override
    public void ganQuyenNhanvien(int nhanvienId, int taikhoanId) {
        Taikhoan account = accounts.findById(taikhoanId).orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại."));
        Nhanvien employee = employees.findById(nhanvienId).orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại."));
        if (employee.getTaikhoan() != null) throw new IllegalArgumentException("Nhân viên đã có tài khoản.");
        account.setNhanvien(employee);
        accounts.save(account);
    }

    @Override
    public void xoaTaiKhoan(int id, String actorEmail) {
        otherAccount(id, actorEmail);
        accounts.deleteById(id);
    }

    @Override
    @Transactional
    public Taikhoan taoTaiKhoanMoi(Taikhoan account, String roleCode, int nhanvienId) {
        if (account.getEmail() == null || account.getEmail().isBlank() || account.getPassword() == null || account.getPassword().isBlank())
            throw new IllegalArgumentException("Cần nhập tài khoản và mật khẩu.");
        if (accounts.findByEmail(account.getEmail()) != null) throw new IllegalArgumentException("Tài khoản đã tồn tại.");
        Nhanvien employee = employees.findById(nhanvienId).orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại."));
        if (employee.getTaikhoan() != null) throw new IllegalArgumentException("Nhân viên đã có tài khoản.");
        account.setNhanvien(employee);
        account.setVaitro(role(roleCode));
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        return accounts.save(account);
    }

    @Override
    public void doiVaiTro(int accountId, String roleCode, String actorEmail) {
        Taikhoan account = otherAccount(accountId, actorEmail);
        account.setVaitro(role(roleCode));
        accounts.save(account);
    }

    @Override
    public void datTrangThaiKhoa(int accountId, boolean locked, String actorEmail) {
        Taikhoan account = otherAccount(accountId, actorEmail);
        account.setLocked(locked);
        accounts.save(account);
    }

    private Taikhoan otherAccount(int id, String actorEmail) {
        Taikhoan account = accounts.findById(id).orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại."));
        if (account.getEmail().equals(actorEmail)) throw new org.springframework.security.access.AccessDeniedException("Không thể sửa tài khoản đang đăng nhập.");
        return account;
    }

    private Vaitro role(String code) {
        if (!"QUAN_LY_KHO".equals(code) && !"NHAN_VIEN".equals(code))
            throw new IllegalArgumentException("Vai trò không hợp lệ.");
        return roles.findByMaVaitro(code).orElseThrow(() -> new IllegalStateException("Vai trò chưa được tạo trong database."));
    }
}