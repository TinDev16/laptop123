package com.example.demo.Repository;

import com.example.demo.Entity.Sanpham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SanphamRepository extends JpaRepository<Sanpham, Integer> {

}
