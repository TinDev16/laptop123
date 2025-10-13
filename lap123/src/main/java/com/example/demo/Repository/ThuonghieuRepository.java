package com.example.demo.Repository;

import com.example.demo.Entity.Thuonghieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThuonghieuRepository extends JpaRepository<Thuonghieu, Integer> {
}
