package com.example.demo.config;

import com.example.demo.Entity.Nhanvien;
import com.example.demo.Repository.NhanvienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomEmployeeDetailService implements UserDetailsService {

    @Autowired
    private NhanvienRepository nhanvienRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        Nhanvien nhanvien = nhanvienRepository.findByEmail(username);

        if(nhanvien == null){
            throw new UsernameNotFoundException("Employee not found");
        }else {
            return new CustomEmployee(nhanvien);
        }
    }
}
