package com.example.demo.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ct_phieunhap")
public class CT_Phieunhap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MAPHIEUNHAP")
    private Phieunhap phieunhap;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MASP")
    private Sanpham sanpham;

    @Column(name = "SOLUONG")
    private int soluong;

    @Column(name = "GIANHAP")
    private BigDecimal gianhap;

    @Column(name = "TRANGTHAI")
    private int trangthai;

    // ---- Getter & Setter ----

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Phieunhap getPhieunhap() {
        return phieunhap;
    }

    public void setPhieunhap(Phieunhap phieunhap) {
        this.phieunhap = phieunhap;
    }

    public Sanpham getSanpham() {
        return sanpham;
    }

    public void setSanpham(Sanpham sanpham) {
        this.sanpham = sanpham;
    }

    public int getSoluong() {
        return soluong;
    }

    public void setSoluong(int soluong) {
        this.soluong = soluong;
    }

    public BigDecimal getGianhap() {
        return gianhap;
    }

    public void setGianhap(BigDecimal gianhap) {
        this.gianhap = gianhap;
    }

    public int getTrangthai() {
        return trangthai;
    }

    public void setTrangthai(int trangthai) {
        this.trangthai = trangthai;
    }
}
