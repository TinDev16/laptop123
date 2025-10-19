package com.example.demo.Repository;

import com.example.demo.Entity.Taikhoan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaikhoanRepository extends JpaRepository<Taikhoan, Integer> {
    public Taikhoan findByEmail(String email);
}
