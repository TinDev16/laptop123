package com.example.demo.config;

import com.example.demo.Entity.Nhanvien;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CustomEmployee implements UserDetails {

    private Nhanvien nhanvien;

    public CustomEmployee(Nhanvien nhanvien){
        super();
        this.nhanvien = nhanvien;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(nhanvien.getRole());
        return Arrays.asList(authority);
    }

    @Override
    public String getPassword() {
        return nhanvien.getPassword();
    }

    @Override
    public String getUsername() {
        return nhanvien.getEmail();
    }
}
