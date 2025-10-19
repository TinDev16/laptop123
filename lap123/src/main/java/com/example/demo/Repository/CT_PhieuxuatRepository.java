package com.example.demo.Repository;

import com.example.demo.Entity.CT_Phieuxuat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CT_PhieuxuatRepository extends JpaRepository<CT_Phieuxuat, Integer> {
}
