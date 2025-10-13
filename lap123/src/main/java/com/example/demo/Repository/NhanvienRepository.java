package com.example.demo.Repository;

import com.example.demo.Entity.Nhanvien;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NhanvienRepository extends JpaRepository<Nhanvien, Integer> {
}
