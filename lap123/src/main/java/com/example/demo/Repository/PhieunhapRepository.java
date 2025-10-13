package com.example.demo.Repository;

import com.example.demo.Entity.CT_Phieunhap;
import com.example.demo.Entity.Phieunhap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhieunhapRepository extends JpaRepository<Phieunhap, Integer> {
}
