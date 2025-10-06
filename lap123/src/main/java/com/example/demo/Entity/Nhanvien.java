package com.example.demo.Entity;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

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
        public String getGHICHU() {
        return GHICHU;
    }

    public void setGHICHU(String GHICHU) {
        this.GHICHU = GHICHU;
    }

}
