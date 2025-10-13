package com.example.demo.Repository;

import com.example.demo.Entity.CT_Phieunhap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CT_PhieunhapRepository extends JpaRepository<CT_Phieunhap, Integer> {
}
