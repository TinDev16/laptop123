package com.example.demo.Service;
import com.example.demo.Entity.Taikhoan;


public interface TaikhoanService {

    public void updateTaikhoanResetToken(String email, String resetToken);

    public Taikhoan getByResetToken(String token);

    public Taikhoan updateTaikhoan(Taikhoan taikhoan);

    public Taikhoan getByEmail(String email);

}
