package com.example.demo.Service;

import com.example.demo.Entity.Phieunhap;
import java.math.BigDecimal;
import java.util.List;

public interface PhieunhapService {

    List<Phieunhap> getAllPhieunhap();

    Phieunhap getPhieunhapById(int MAPHIEUNHAP);

    void savePhieuNhap(Phieunhap phieunhap, String chiTietListJson, BigDecimal tongTien) throws Exception;

    void update(Phieunhap pn);

    void deletePhieunhap(int id);

}
