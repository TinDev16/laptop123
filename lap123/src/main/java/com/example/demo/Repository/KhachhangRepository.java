package com.example.demo.Repository;

import com.example.demo.Entity.Khachhang;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KhachhangRepository extends JpaRepository<Khachhang, Integer> {
}
