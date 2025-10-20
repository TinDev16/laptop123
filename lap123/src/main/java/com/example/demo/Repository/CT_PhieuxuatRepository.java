package com.example.demo.Repository;

import com.example.demo.Entity.CT_Phieuxuat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface CT_PhieuxuatRepository extends JpaRepository<CT_Phieuxuat, Integer> {
    @Query("SELECT c.giaxuat FROM CT_Phieuxuat c WHERE c.sanpham.MASP = :masp ORDER BY c.phieuxuat.THOIGIAN DESC LIMIT 1")
    BigDecimal findLastGiaxuatBySanphamId(@Param("masp") int masp);
}
