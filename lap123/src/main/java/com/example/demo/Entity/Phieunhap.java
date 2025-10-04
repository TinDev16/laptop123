package com.example.demo.Entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "phieunhap")
public class Phieunhap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int MAPHIEUNHAP;


    @ManyToOne
    @JoinColumn(name = "MANCC")
    private NCC MANCC;

    @ManyToOne
    @JoinColumn(name = "MANV")
    private Nhanvien MANV;

    @Column(name = "THOIGIAN")
    private Date THOIGIAN;

    @Column(name = "TONGTIEN")
    private float TONGTIEN;

    @Column(name = "TRANGTHAI")
    private int TRANGTHAI;


    public int getMAPHIEUNHAP() {
        return MAPHIEUNHAP;
    }

    public void setMAPHIEUNHAP(int MAPHIEUNHAP) {
        this.MAPHIEUNHAP = MAPHIEUNHAP;
    }

    public NCC getMANCC() {
        return MANCC;
    }

    public void setMANCC(NCC MANCC) {
        this.MANCC = MANCC;
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

    public float getTONGTIEN() {
        return TONGTIEN;
    }

    public void setTONGTIEN(float TONGTIEN) {
        this.TONGTIEN = TONGTIEN;
    }

    public int getTRANGTHAI() {
        return TRANGTHAI;
    }

    public void setTRANGTHAI(int TRANGTHAI) {
        this.TRANGTHAI = TRANGTHAI;
    }
}
