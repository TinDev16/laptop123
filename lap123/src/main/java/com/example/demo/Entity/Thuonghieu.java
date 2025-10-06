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

    @Column(name = "GHICHU")
    private String GHICHU;

    public Thuonghieu() {

    }

    public Thuonghieu(String TENTHUONGHIEU, int TRANGTHAI, String GHICHU) {
        this.TENTHUONGHIEU = TENTHUONGHIEU;
        this.TRANGTHAI = TRANGTHAI;
        this.GHICHU = GHICHU;
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

    public String getGHICHU() {
        return GHICHU;
    }

    public void setGHICHU(String GHICHU) {
        this.GHICHU = GHICHU;
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
