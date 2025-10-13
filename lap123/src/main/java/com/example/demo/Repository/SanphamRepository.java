package com.example.demo.Repository;

import com.example.demo.Entity.Sanpham;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanphamRepository extends JpaRepository<Sanpham, Integer> {
}
