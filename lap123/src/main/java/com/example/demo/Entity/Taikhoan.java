package com.example.demo.Entity;

import jakarta.persistence.*;

@Entity
@Table(name =  "taikhoan")

public class Taikhoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int taikhoan_id;

    @Column(name = "Email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "reset_token")
    private String resetToken;


    @OneToOne
    @JoinColumn(name = "MANV")
    private Nhanvien nhanvien;

    @ManyToOne
    @JoinColumn(name = "vaitro_id")
    private Vaitro vaitro;

    public Taikhoan() {
    }

    public Taikhoan(int taikhoan_id,String email,String password){
        this.taikhoan_id = taikhoan_id;
        this.email = email;
        this.password = password;
    }

    public int getTaikhoan_id() {
        return taikhoan_id;
    }

    public void setTaikhoan_id(int taikhoan_id) {
        this.taikhoan_id = taikhoan_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }


    public Nhanvien getNhanvien() { return nhanvien; }

    public void setNhanvien(Nhanvien nhanvien) { this.nhanvien = nhanvien; }

    public Vaitro getVaitro() {
        return vaitro;
    }

    public void setVaitro(Vaitro vaitro) {
        this.vaitro = vaitro;
    }
}
