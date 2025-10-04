package com.example.demo.Entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "phieuxuat")
public class Phieuxuat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int MAPHIEUXUAT;

    @ManyToOne
    @JoinColumn(name = "MAKH")
    private Khachhang MAKH;

    @ManyToOne
    @JoinColumn(name = "MANV")
    private Nhanvien MANV;

    @Column(name = "THOIGIAN")
    private Date THOIGIAN;

    @Column(name = "TONGTIEN")
    private Float TONGTIEN;

    @Column(name = "TRANGTHAI")
    private int TRANGTHAI;

    public int getMAPHIEUXUAT() {
        return MAPHIEUXUAT;
    }

    public void setMAPHIEUXUAT(int MAPHIEUXUAT) {
        this.MAPHIEUXUAT = MAPHIEUXUAT;
    }

    public Khachhang getMAKH() {
        return MAKH;
    }

    public void setMAKH(Khachhang MAKH) {
        this.MAKH = MAKH;
    }

    public Nhanvien getMANV() {
        return MANV;
    }

    public void setMANV(Nhanvien MANV) {
        this.MANV = MANV;
    }

    public Date getTHOIGIAN() {
        return THOIGIAN;
    }

    public void setTHOIGIAN(Date THOIGIAN) {
        this.THOIGIAN = THOIGIAN;
    }

    public Float getTONGTIEN() {
        return TONGTIEN;
    }

    public void setTONGTIEN(Float TONGTIEN) {
        this.TONGTIEN = TONGTIEN;
    }

    public int getTRANGTHAI() {
        return TRANGTHAI;
    }

    public void setTRANGTHAI(int TRANGTHAI) {
        this.TRANGTHAI = TRANGTHAI;
    }
}
