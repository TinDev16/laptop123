package com.example.demo.Entity;

import jakarta.persistence.*;

import javax.lang.model.element.Name;

@Entity
@Table(name = "thuonghieu")
public class Thuonghieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int MATHUONGHIEU;

    @Column(name = "TENTHUONGHIEU")
    private String TENTHUONGHIEU;

    @Column(name = "TRANGTHAI")
    private int TRANGTHAI;


    public Thuonghieu() {

    }

    public Thuonghieu(String TENTHUONGHIEU, int TRANGTHAI) {
        this.TENTHUONGHIEU = TENTHUONGHIEU;
        this.TRANGTHAI = TRANGTHAI;
    }

    public int getMATHUONGHIEU() {
        return MATHUONGHIEU;
    }

    public void setMATHUONGHIEU(int MATHUONGHIEU) {
        this.MATHUONGHIEU = MATHUONGHIEU;
    }

    public String getTENTHUONGHIEU() {
        return TENTHUONGHIEU;
    }

    public void setTENTHUONGHIEU(String TENTHUONGHIEU) {
        this.TENTHUONGHIEU = TENTHUONGHIEU;
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
        if (!(o instanceof Thuonghieu)) return false;
        Thuonghieu that = (Thuonghieu) o;
        return MATHUONGHIEU == that.MATHUONGHIEU;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(MATHUONGHIEU);
    }
}
