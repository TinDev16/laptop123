package com.example.demo.Repository;

import com.example.demo.Entity.Nhanvien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface NhanvienRepository
        extends JpaRepository<Nhanvien, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from Nhanvien item where item.MANV = :id")
    Optional<Nhanvien> findForDeletion(
            @Param("id") int id
    );

    List<Nhanvien> findByTaikhoanIsNull();

    List<Nhanvien> findByTaikhoanIsNullAndTRANGTHAI(
            int trangthai
    );

    @Query("""
        select n
        from Nhanvien n
        left join n.taikhoan tk
        left join tk.vaitro vt
        where vt.maVaitro is null
           or vt.maVaitro <> 'ADMIN'
    """)
    List<Nhanvien> findAllExceptAdmin();
}