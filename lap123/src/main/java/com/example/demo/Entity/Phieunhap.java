package com.example.demo.Entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "phieunhap")
public class Phieunhap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int MAPHIEUNHAP;

    @OneToMany(mappedBy = "phieunhap", cascade = CascadeType.ALL)
    private List<CT_Phieunhap> chiTietPhieuNhap;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MANCC")
    private NCC ncc;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MANV")
    private Nhanvien nhanvien;

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

    public NCC getNcc() {
        return ncc;
    }

    public void setNcc(NCC ncc) {
        this.ncc = ncc;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Phieunhap)) return false;
        Phieunhap that = (Phieunhap) o;
        return MAPHIEUNHAP == that.MAPHIEUNHAP;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(MAPHIEUNHAP);
    }
}
