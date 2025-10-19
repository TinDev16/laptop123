package com.example.demo.config;

import com.example.demo.Entity.Taikhoan;
import com.example.demo.Repository.TaikhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomEmployeeDetailService implements UserDetailsService {

    @Autowired
    private TaikhoanRepository taikhoanRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(username);

        if (taikhoan == null) {
            throw new UsernameNotFoundException("Tài khoản không tồn tại");
        }

        return new CustomEmployee(taikhoan);
    }
}
