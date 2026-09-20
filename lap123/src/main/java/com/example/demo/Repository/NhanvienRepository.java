package com.example.demo.Repository;

import com.example.demo.Entity.Nhanvien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import java.util.List;

public interface NhanvienRepository extends JpaRepository<Nhanvien, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from Nhanvien item where item.MANV = :id")
    Optional<Nhanvien> findForDeletion(@Param("id") int id);
    List<Nhanvien> findByTaikhoanIsNull();
    List<Nhanvien> findByTaikhoanIsNullAndTRANGTHAI(int trangthai);


}
