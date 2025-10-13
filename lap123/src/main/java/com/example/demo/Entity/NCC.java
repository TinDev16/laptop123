package com.example.demo.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ncc")
public class NCC {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int MANCC;

    @Column(name = "TENNCC")
    private String TENNCC;

    @Column(name = "DIACHI")
    private String DIACHI;

    @Column(name = "SDT")
    private String SDT;

    @Column(name = "TRANGTHAI")
    private int TRANGTHAI;

    @Column(name = "GHICHU")
    private String GHICHU;

    public NCC() {

    }

    public NCC(String TENNCC, String DIACHI, String SDT, int TRANGTHAI, String GHICHU) {
        this.TENNCC = TENNCC;
        this.DIACHI = DIACHI;
        this.SDT = SDT;
        this.TRANGTHAI = TRANGTHAI;
        this.GHICHU = GHICHU;
    }

    public int getMANCC() {
        return MANCC;
    }

    public void setMANCC(int MANCC) {
        this.MANCC = MANCC;
    }

    public String getTENNCC() {
        return TENNCC;
    }

    public void setTENNCC(String TENNCC) {
        this.TENNCC = TENNCC;
    }

    public String getDIACHI() {
        return DIACHI;
    }

    public void setDIACHI(String DIACHI) {
        this.DIACHI = DIACHI;
    }

    public String getSDT() {
        return SDT;
    }

    public void setSDT(String SDT) {
        this.SDT = SDT;
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
        if (!(o instanceof NCC)) return false;
        NCC that = (NCC) o;
        return MANCC == that.MANCC;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(MANCC);
    }
}
