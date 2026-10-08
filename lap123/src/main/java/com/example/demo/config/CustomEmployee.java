package com.example.demo.config;

import com.example.demo.Entity.Taikhoan;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomEmployee implements UserDetails {

    private final Taikhoan taikhoan;

    public CustomEmployee(Taikhoan taikhoan) {
        this.taikhoan = taikhoan;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        String role = null;

        if (taikhoan.getVaitro() != null) {
            role = taikhoan.getVaitro().getMaVaitro();
        }

        if (!"ADMIN".equals(role)
                && !"QUAN_LY_KHO".equals(role)
                && !"NHAN_VIEN".equals(role)) {

            throw new IllegalStateException(
                    "Tài khoản không có vai trò hợp lệ."
            );
        }

        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
    }

    @Override
    public String getPassword() {
        return taikhoan.getPassword();
    }

    @Override
    public String getUsername() {
        return taikhoan.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !taikhoan.isLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {

        if (taikhoan.getNhanvien() == null) {
            return true;
        }

        if (taikhoan.getNhanvien().getTRANGTHAI() == 0) {
            return true;
        }

        return taikhoan.getVaitro() != null
                && "QUAN_LY_KHO".equals(
                taikhoan.getVaitro().getMaVaitro()
        );
    }
}