package com.example.demo.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "tonkho")
public class Tonkho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MASP")
    private Sanpham sanpham;

    @Column(name = "SOLUONGTON")
    private String SOLUONGTON;

    @Column(name = "GIANHAP")
    private BigDecimal GIANHAP;

    @Column(name = "GIABAN")
    private BigDecimal GIABAN;

    @Column(name = "NGAYCAPNHAT")
    private Date NGAYCAPNHAT;

    @Column(name = "TRANGTHAI")
    private int TRANGTHAI;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Sanpham getSanpham() {
        return sanpham;
    }

    public void setSanpham(Sanpham sanpham) {
        this.sanpham = sanpham;
    }

    public String getSOLUONGTON() {
        return SOLUONGTON;
    }

    public void setSOLUONGTON(String SOLUONGTON) {
        this.SOLUONGTON = SOLUONGTON;
    }

    public BigDecimal getGIANHAP() {
        return GIANHAP;
    }

    public void setGIANHAP(BigDecimal GIANHAP) {
        this.GIANHAP = GIANHAP;
    }

    public BigDecimal getGIABAN() {
        return GIABAN;
    }

    public void setGIABAN(BigDecimal GIABAN) {
        this.GIABAN = GIABAN;
    }

    public Date getNGAYCAPNHAT() {
        return NGAYCAPNHAT;
    }

    public void setNGAYCAPNHAT(Date NGAYCAPNHAT) {
        this.NGAYCAPNHAT = NGAYCAPNHAT;
    }

    public int getTRANGTHAI() {
        return TRANGTHAI;
    }

    public void setTRANGTHAI(int TRANGTHAI) {
        this.TRANGTHAI = TRANGTHAI;
    }
}
