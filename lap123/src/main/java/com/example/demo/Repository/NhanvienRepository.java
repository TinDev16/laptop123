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
    @Query("SELECT n FROM Nhanvien n WHERE n.MANV = :id")
    Optional<Nhanvien> findForDeletion(
            @Param("id") int id
    );

    // Nhân viên chưa từng có tài khoản
    @Query("""
        SELECT n
        FROM Nhanvien n
        WHERE NOT EXISTS (
            SELECT tk.taikhoan_id
            FROM Taikhoan tk
            WHERE tk.nhanvien = n
        )
    """)
    List<Nhanvien> findNhanVienChuaCoTaiKhoan();

    // Nhân viên đang làm việc, chưa từng có tài khoản
    @Query("""
        SELECT n
        FROM Nhanvien n
        WHERE n.TRANGTHAI = :trangthai
          AND NOT EXISTS (
              SELECT tk.taikhoan_id
              FROM Taikhoan tk
              WHERE tk.nhanvien = n
          )
    """)
    List<Nhanvien> findNhanVienChuaCoTaiKhoanTheoTrangThai(
            @Param("trangthai") int trangthai
    );

    // Loại nhân viên có tài khoản mang vai trò ADMIN
    @Query("""
        SELECT DISTINCT n
        FROM Nhanvien n
        WHERE NOT EXISTS (
            SELECT tk.taikhoan_id
            FROM Taikhoan tk
            JOIN tk.vaitro vt
            WHERE tk.nhanvien = n
              AND vt.maVaitro = 'ADMIN'
        )
    """)
    List<Nhanvien> findAllExceptAdmin();
}