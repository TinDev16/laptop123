package com.example.demo.Entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "vaitro")
public class Vaitro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int vaitro_id;

    @Column(name = "ma_vaitro")
    private String maVaitro;

    @Column(name = "ten")
    private String ten;

    @OneToMany(mappedBy = "vaitro")
    private List<Taikhoan> taikhoans;

    public Vaitro() {}

    public Vaitro(int vaitro_id,String ten){
        this.vaitro_id = vaitro_id;
        this.ten = ten;
    }

    public int getVaitro_id() {
        return vaitro_id;
    }

    public void setVaitro_id(int vaitro_id) {
        this.vaitro_id = vaitro_id;
    }

    public String getTen() {
        return maVaitro;
    }

    public void setTen(String Role) {
        this.ten = Role;
    }

    public String getMaVaitro() { return maVaitro; }
    public void setMaVaitro(String maVaitro) { this.maVaitro = maVaitro; }

    public List<Taikhoan> getTaikhoans() {
        return taikhoans;
    }

    public void setTaikhoans(List<Taikhoan> taikhoans) {
        this.taikhoans = taikhoans;
    }

}
