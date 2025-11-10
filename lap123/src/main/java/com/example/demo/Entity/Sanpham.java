package com.example.demo.Entity;

import jakarta.persistence.*;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MATHUONGHIEU")
    private Thuonghieu thuonghieu;

    @Column(name = "TRANGTHAI")
    private int TRANGTHAI;

    @Column(name = "GHICHU")
    private String GHICHU;

    @Column(name = "SOLUONGTON")
    private Integer SOLUONGTON;

    public Sanpham() {

    }

    public Sanpham(String TENSP, String HINHANHSP, String XUATXU, String CHIP, String HEDIEUHANH, String KICHTHUOCMAN, Thuonghieu thuonghieu, int TRANGTHAI, String GHICHU, Integer SOLUONGTON) {
        super();
        this.TENSP = TENSP;
        this.HINHANHSP = HINHANHSP;
        this.XUATXU = XUATXU;
        this.CHIP = CHIP;
        this.HEDIEUHANH = HEDIEUHANH;
        this.KICHTHUOCMAN = KICHTHUOCMAN;
        this.thuonghieu = thuonghieu;
        this.TRANGTHAI = TRANGTHAI;
        this.GHICHU = GHICHU;
        this.SOLUONGTON = SOLUONGTON;
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

    public Thuonghieu getThuonghieu() {
        return thuonghieu;
    }

    public void setThuonghieu(Thuonghieu thuonghieu) {
        this.thuonghieu = thuonghieu;
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

    public Integer getSOLUONGTON() {
        return SOLUONGTON;
    }

    public void setSOLUONGTON(Integer SOLUONGTON) {
        this.SOLUONGTON = SOLUONGTON;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sanpham)) return false;
        Sanpham that = (Sanpham) o;
        return MASP == that.MASP;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(MASP);
    }
}
