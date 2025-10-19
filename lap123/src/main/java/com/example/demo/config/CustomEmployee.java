package com.example.demo.config;

import com.example.demo.Entity.Taikhoan;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;

public class CustomEmployee implements UserDetails {

    private Taikhoan taikhoan;

    public CustomEmployee(Taikhoan taikhoan){
        super();
        this.taikhoan = taikhoan;
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
        String roleName = taikhoan.getVaitro().getTen();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);
        return Arrays.asList(authority);
    }


    @Override
    public String getPassword() {
        return taikhoan.getPassword();
    }

    @Override
    public String getUsername() {
        return taikhoan.getEmail();
    }
}
