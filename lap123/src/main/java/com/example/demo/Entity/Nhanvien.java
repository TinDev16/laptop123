package com.example.demo.Entity;

import jakarta.persistence.*;

import javax.lang.model.element.Name;

@Entity
@Table(name = "nhanvien")
public class Nhanvien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long MANV;

    @Column(name = "HOTEN")
    private String HOTEN;

    @Column(name = "GIOITINH")
    private String GIOITINH;

    @Column(name = "NGAYSINH")
    private String NGAYSINH;

    @Column(name = "SDT")
    private String SDT;

    @Column(name = "NGAYTHAMGIA")
    private String NGAYTHAMGIA;

    @Column(name = "TRANGTHAI")
    private int TRANGTHAI;

    public Nhanvien() {

    }

    public Nhanvien(String HOTEN, String GIOITINH, String NGAYSINH, String SDT, String NGAYTHAMGIA, int TRANGTHAI) {
        this.HOTEN = HOTEN;
        this.GIOITINH = GIOITINH;
        this.NGAYSINH = NGAYSINH;
        this.SDT = SDT;
        this.NGAYTHAMGIA = NGAYTHAMGIA;
        this.TRANGTHAI = TRANGTHAI;
    }

    public Long getMANV() {
        return MANV;
    }

    public void setMANV(Long MANV) {
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

    public String getNGAYSINH() {
        return NGAYSINH;
    }

    public void setNGAYSINH(String NGAYSINH) {
        this.NGAYSINH = NGAYSINH;
    }

    public String getSDT() {
        return SDT;
    }

    public void setSDT(String SDT) {
        this.SDT = SDT;
    }

    public String getNGAYTHAMGIA() {
        return NGAYTHAMGIA;
    }

    public void setNGAYTHAMGIA(String NGAYTHAMGIA) {
        this.NGAYTHAMGIA = NGAYTHAMGIA;
    }

    public int getTRANGTHAI() {
        return TRANGTHAI;
    }

    public void setTRANGTHAI(int TRANGTHAI) {
        this.TRANGTHAI = TRANGTHAI;
    }
}
