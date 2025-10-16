package com.example.demo.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ct_phieuxuat")
public class CT_Phieuxuat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MAPHIEUXUAT")
    private Phieuxuat phieuxuat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MASP")
    private Sanpham sanpham;

    @Column(name = "SOLUONG")
    private int soluong;

    @Column(name = "GIAXUAT")
    private BigDecimal giaxuat;

    @Column(name = "TRANGTHAI")
    private int TRANGTHAI;

    public Phieuxuat getPhieuxuat() {
        return phieuxuat;
    }

    public void setPhieuxuat(Phieuxuat phieuxuat) {
        this.phieuxuat = phieuxuat;
    }

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

    public int getSoluong() {
        return soluong;
    }

    public void setSoluong(int soluong) {
        this.soluong = soluong;
    }

    public BigDecimal getGiaxuat() {
        return giaxuat;
    }

    public void setGiaxuat(BigDecimal giaxuat) {
        this.giaxuat = giaxuat;
    }

    public int getTRANGTHAI() {
        return TRANGTHAI;
    }

    public void setTRANGTHAI(int TRANGTHAI) {
        this.TRANGTHAI = TRANGTHAI;
    }


}
