package com.example.demo.Entity;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "phieuxuat")
public class Phieuxuat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int MAPHIEUXUAT;

    @OneToMany(mappedBy = "phieuxuat", cascade = CascadeType.ALL)
    private List<CT_Phieuxuat> chitietPhieuxuat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MAKH")
    private Khachhang khachhang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MANV")
    private Nhanvien nhanvien;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "THOIGIAN")
    private Date THOIGIAN;

    @Column(name = "TONGTIEN")
    private BigDecimal TONGTIEN;

    @Column(name = "TRANGTHAI")
    private int TRANGTHAI;

    public int getMAPHIEUXUAT() {
        return MAPHIEUXUAT;
    }

    public void setMAPHIEUXUAT(int MAPHIEUXUAT) {
        this.MAPHIEUXUAT = MAPHIEUXUAT;
    }

    public Khachhang getKhachhang() {
        return khachhang;
    }

    public void setKhachhang(Khachhang khachhang) {
        this.khachhang = khachhang;
    }

    public Nhanvien getNhanvien() {
        return nhanvien;
    }

    public void setNhanvien(Nhanvien nhanvien) {
        this.nhanvien = nhanvien;
    }

    public Date getTHOIGIAN() {
        return THOIGIAN;
    }

    public void setTHOIGIAN(Date THOIGIAN) {
        this.THOIGIAN = THOIGIAN;
    }

    public List<CT_Phieuxuat> getChitietPhieuxuat() {
        return chitietPhieuxuat;
    }

    public void setChitietPhieuxuat(List<CT_Phieuxuat> chitietPhieuxuat) {
        this.chitietPhieuxuat = chitietPhieuxuat;
    }

    public BigDecimal getTONGTIEN() {
        return TONGTIEN;
    }

    public void setTONGTIEN(BigDecimal TONGTIEN) {
        this.TONGTIEN = TONGTIEN;
    }

    public int getTRANGTHAI() {
        return TRANGTHAI;
    }

    public void setTRANGTHAI(int TRANGTHAI) {
        this.TRANGTHAI = TRANGTHAI;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Phieuxuat)) return false;
        Phieuxuat that = (Phieuxuat) o;
        return MAPHIEUXUAT == that.MAPHIEUXUAT;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(MAPHIEUXUAT);
    }
}
