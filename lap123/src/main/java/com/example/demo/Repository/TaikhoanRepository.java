package com.example.demo.Repository;

import com.example.demo.Entity.Taikhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaikhoanRepository extends JpaRepository<Taikhoan, Integer> {

    @Query("""
        SELECT tk
        FROM Taikhoan tk
        LEFT JOIN FETCH tk.nhanvien
        LEFT JOIN FETCH tk.vaitro
        WHERE tk.email = :email
    """)
    Taikhoan findByEmail(@Param("email") String email);

    Taikhoan findByResetToken(String resetToken);
}