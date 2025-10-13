package com.example.demo.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ct_phieuxuat")
public class CT_Phieuxuat {

    @ManyToOne
    @JoinColumn(name = "MAPHIEUXUAT")
    private Phieuxuat phieuxuat;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    @JoinColumn(name = "MASP")
    private Sanpham MASP;

    @Column(name = "SOLUONG")
    private int SOLUONG;

    @Column(name = "GIAXUAT")
    private Float GIAXUAT;

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

    public Sanpham getMASP() {
        return MASP;
    }

    public void setMASP(Sanpham MASP) {
        this.MASP = MASP;
    }

    public int getSOLUONG() {
        return SOLUONG;
    }

    public void setSOLUONG(int SOLUONG) {
        this.SOLUONG = SOLUONG;
    }

    public Float getGIAXUAT() {
        return GIAXUAT;
    }

    public void setGIAXUAT(Float GIAXUAT) {
        this.GIAXUAT = GIAXUAT;
    }

    public int getTRANGTHAI() {
        return TRANGTHAI;
    }

    public void setTRANGTHAI(int TRANGTHAI) {
        this.TRANGTHAI = TRANGTHAI;
    }
}
