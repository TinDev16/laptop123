package com.example.demo.Entity;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import javax.lang.model.element.Name;
import java.util.Date;

@Entity
@Table(name = "nhanvien")
public class Nhanvien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int MANV;

    @Column(name = "HOTEN")
    private String HOTEN;

    @Column(name = "GIOITINH")
    private String GIOITINH;


    @Column(name = "NGAYSINH")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date NGAYSINH;

    @Column(name = "SDT")
    private String SDT;

    @Column(name = "NGAYTHAMGIA")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date NGAYTHAMGIA;

    @Column(name = "TRANGTHAI")
    private int TRANGTHAI;

    @Column(name = "GHICHU")
    private String GHICHU;

    @OneToMany(mappedBy = "nhanvien", fetch = FetchType.LAZY)
    private List<Taikhoan> danhSachTaiKhoan = new ArrayList<>();

    @Column(name = "email", unique = true)
    private String EMAIL;

    public String getEMAIL() {
        return EMAIL;
    }

    public void setEMAIL(String EMAIL) {
        this.EMAIL = EMAIL;
    }

    public Nhanvien() {

    }

    public Nhanvien(String HOTEN, String GIOITINH, Date NGAYSINH, String SDT, Date NGAYTHAMGIA, int TRANGTHAI, String GHICHU) {
        this.HOTEN = HOTEN;
        this.GIOITINH = GIOITINH;
        this.NGAYSINH = NGAYSINH;
        this.SDT = SDT;
        this.NGAYTHAMGIA = NGAYTHAMGIA;
        this.TRANGTHAI = TRANGTHAI;
        this.GHICHU = GHICHU;
    }

    public int getMANV() {
        return MANV;
    }

    public void setMANV(int MANV) {
        this.MANV = MANV;
    }

    public String getHOTEN() {
        return HOTEN;
    }

    public void setHOTEN(String HOTEN) {
        this.HOTEN = HOTEN;
    }

    public String getGIOITINH() {
        return GIOITINH;
    }

    public void setGIOITINH(String GIOITINH) {
        this.GIOITINH = GIOITINH;
    }

    public Date getNGAYSINH() {
        return NGAYSINH;
    }

    public void setNGAYSINH(Date NGAYSINH) {
        this.NGAYSINH = NGAYSINH;
    }

    public String getSDT() {
        return SDT;
    }

    public void setSDT(String SDT) {
        this.SDT = SDT;
    }

    public Date getNGAYTHAMGIA() {
        return NGAYTHAMGIA;
    }

    public void setNGAYTHAMGIA(Date NGAYTHAMGIA) {
        this.NGAYTHAMGIA = NGAYTHAMGIA;
    }

    public int getTRANGTHAI() {
        return TRANGTHAI;
    }

    public void setTRANGTHAI(int TRANGTHAI) {
        this.TRANGTHAI = TRANGTHAI;
    }

    public List<Taikhoan> getDanhSachTaiKhoan() {
        return danhSachTaiKhoan;
    }

    public void setDanhSachTaiKhoan(List<Taikhoan> danhSachTaiKhoan) {
        this.danhSachTaiKhoan = danhSachTaiKhoan;
    }

    /**
     * Lấy tài khoản chưa khóa nếu có.
     * Nếu tất cả tài khoản đều khóa thì lấy tài khoản có ID lớn nhất.
     * Giữ lại để tương thích với code cũ đang gọi getTaikhoan().
     */
    @Transient
    public Taikhoan getTaikhoan() {
        if (danhSachTaiKhoan == null || danhSachTaiKhoan.isEmpty()) {
            return null;
        }

        return danhSachTaiKhoan.stream()
                .filter(tk -> !tk.isLocked())
                .max(Comparator.comparingInt(Taikhoan::getTaikhoan_id))
                .orElseGet(() -> danhSachTaiKhoan.stream()
                        .max(Comparator.comparingInt(
                                Taikhoan::getTaikhoan_id))
                        .orElse(null));
    }

    /**
     * Giữ tương thích với code cũ nếu có chỗ gọi setTaikhoan().
     */
    @Transient
    public void setTaikhoan(Taikhoan taikhoan) {
        if (danhSachTaiKhoan == null) {
            danhSachTaiKhoan = new ArrayList<>();
        }

        if (taikhoan != null) {
            danhSachTaiKhoan.add(taikhoan);
            taikhoan.setNhanvien(this);
        }
    }

    public String getGHICHU() {
        return GHICHU;
    }

    public void setGHICHU(String GHICHU) {
        this.GHICHU = GHICHU;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nhanvien)) return false;
        Nhanvien that = (Nhanvien) o;
        return MANV == that.MANV;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(MANV);
    }


}
