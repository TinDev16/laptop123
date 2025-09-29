package com.example.demo.Entity;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Table(name = "khachhang")
public class Khachhang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int MAKH;

    @Column(name="HOTEN")
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

    public Khachhang() {

    }

    public Khachhang(String HOTEN, String GIOITINH, Date NGAYSINH, String SDT, Date NGAYTHAMGIA, int TRANGTHAI) {
        this.HOTEN = HOTEN;
        this.GIOITINH = GIOITINH;
        this.NGAYSINH = NGAYSINH;
        this.SDT = SDT;
        this.NGAYTHAMGIA = NGAYTHAMGIA;
        this.TRANGTHAI = TRANGTHAI;
    }

    public int getMAKH() {
        return MAKH;
    }

    public void setMAKH(int MAKH) {
        this.MAKH = MAKH;
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
}
