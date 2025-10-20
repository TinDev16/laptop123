package com.example.demo.Repository;

import com.example.demo.Entity.CT_Phieunhap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface CT_PhieunhapRepository extends JpaRepository<CT_Phieunhap, Integer> {
    @Query("SELECT c.gianhap FROM CT_Phieunhap c WHERE c.sanpham.MASP = :masp ORDER BY c.phieunhap.THOIGIAN DESC LIMIT 1")
    BigDecimal findLastGianhapBySanphamId(@Param("masp") int masp);

}
