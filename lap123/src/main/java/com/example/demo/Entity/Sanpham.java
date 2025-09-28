package com.example.demo.Entity;

import jakarta.persistence.*;

import javax.lang.model.element.Name;

@Entity
@Table(name = "sanpham")
public class Sanpham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int MASP;

    @Column(name = "TENSP")
    private String TENSP;

    @Column(name = "HINHANHSP")
    private String HINHANHSP;

    @Column(name = "XUATXU")
    private String XUATXU;

    @Column(name = "CHIP")
    private String CHIP;

    @Column(name = "HEDIEUHANH")
    private String HEDIEUHANH;

    @Column(name = "KICHTHUOCMAN")
    private String KICHTHUOCMAN;

    @Column(name = "THUONGHIEU")
    private String THUONGHIEU;

    @Column(name = "TRANGTHAI")
    private int TRANGTHAI;


    public Sanpham() {

    }
    public Sanpham(String TENSP, String HINHANHSP, String XUATXU, String CHIP, String HEDIEUHANH, String KICHTHUOCMAN, String THUONGHIEU, int TRANGTHAI) {
        super();
        this.TENSP = TENSP;
        this.HINHANHSP = HINHANHSP;
        this.XUATXU = XUATXU;
        this.CHIP = CHIP;
        this.HEDIEUHANH = HEDIEUHANH;
        this.KICHTHUOCMAN = KICHTHUOCMAN;
        this.THUONGHIEU = THUONGHIEU;
        this.TRANGTHAI = TRANGTHAI;
    }

    public int getMASP() {
        return MASP;
    }

    public void setMASP(int MASP) {
        this.MASP = MASP;
    }

    public String getTENSP() {
        return TENSP;
    }

    public void setTENSP(String TENSP) {
        this.TENSP = TENSP;
    }

    public String getHINHANHSP() {
        return HINHANHSP;
    }

    public void setHINHANHSP(String HINHANHSP) {
        this.HINHANHSP = HINHANHSP;
    }

    public String getXUATXU() {
        return XUATXU;
    }

    public void setXUATXU(String XUATXU) {
        this.XUATXU = XUATXU;
    }

    public String getCHIP() {
        return CHIP;
    }

    public void setCHIP(String CHIP) {
        this.CHIP = CHIP;
    }

    public String getHEDIEUHANH() {
        return HEDIEUHANH;
    }

    public void setHEDIEUHANH(String HEDIEUHANH) {
        this.HEDIEUHANH = HEDIEUHANH;
    }

    public String getKICHTHUOCMAN() {
        return KICHTHUOCMAN;
    }

    public void setKICHTHUOCMAN(String KICHTHUOCMAN) {
        this.KICHTHUOCMAN = KICHTHUOCMAN;
    }

    public String getTHUONGHIEU() {
        return THUONGHIEU;
    }

    public void setTHUONGHIEU(String THUONGHIEU) {
        this.THUONGHIEU = THUONGHIEU;
    }

    public int getTRANGTHAI() {
        return TRANGTHAI;
    }

    public void setTRANGTHAI(int TRANGTHAI) {
        this.TRANGTHAI = TRANGTHAI;
    }





}
