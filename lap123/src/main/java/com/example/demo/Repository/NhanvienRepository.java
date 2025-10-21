package com.example.demo.Repository;

import com.example.demo.Entity.Nhanvien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NhanvienRepository extends JpaRepository<Nhanvien, Integer> {
    List<Nhanvien> findByTaikhoanIsNull();
    List<Nhanvien> findByTaikhoanIsNullAndTRANGTHAI(int trangthai);


}
