//package com.example.demo.Entity;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "ct_phieunhap")
//public class CT_Phieunhap {
//
//    @ManyToOne
//    @JoinColumn(name = "MAPHIEUNHAP")
//    private Phieunhap phieunhap;
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private int id;
//
//    @ManyToOne
//    @JoinColumn(name = "MAPHIEUNHAP")
//    private Phieunhap MAPHIEUNHAP;
//
//    @ManyToOne
//    @JoinColumn(name = "MASP")
//    private Sanpham sanpham;
//
//    @ManyToOne
//    @JoinColumn(name = "MANCC")
//    private NCC ncc;
//
//    @ManyToOne
//    @JoinColumn(name = "MANV")
//    private Nhanvien nhanvien;
//
//    @Column(name = "SOLUONG")
//    private int SOLUONG;
//
//    @Column(name = "GIANHAP")
//    private float GIANHAP;
//
//    @Column(name = "TRANGTHAI")
//    private int TRANGTHAI;
//
//    public Phieunhap getPhieunhap() {
//        return phieunhap;
//    }
//
//    public void setPhieunhap(Phieunhap phieunhap) {
//        this.phieunhap = phieunhap;
//    }
//
//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public Phieunhap getMAPHIEUNHAP() {
//        return MAPHIEUNHAP;
//    }
//
//    public void setMAPHIEUNHAP(Phieunhap MAPHIEUNHAP) {
//        this.MAPHIEUNHAP = MAPHIEUNHAP;
//    }
//
//    public Sanpham getSanpham() {
//        return sanpham;
//    }
//
//    public void setSanpham(Sanpham sanpham) {
//        this.sanpham = sanpham;
//    }
//
//    public NCC getNcc() {
//        return ncc;
//    }
//
//    public void setNcc(NCC ncc) {
//        this.ncc = ncc;
//    }
//
//    public Nhanvien getNhanvien() {
//        return nhanvien;
//    }
//
//    public void setNhanvien(Nhanvien nhanvien) {
//        this.nhanvien = nhanvien;
//    }
//
//    public int getSOLUONG() {
//        return SOLUONG;
//    }
//
//    public void setSOLUONG(int SOLUONG) {
//        this.SOLUONG = SOLUONG;
//    }
//
//    public float getGIANHAP() {
//        return GIANHAP;
//    }
//
//    public void setGIANHAP(float GIANHAP) {
//        this.GIANHAP = GIANHAP;
//    }
//
//    public int getTRANGTHAI() {
//        return TRANGTHAI;
//    }
//
//    public void setTRANGTHAI(int TRANGTHAI) {
//        this.TRANGTHAI = TRANGTHAI;
//    }
//}
