package com.example.demo.Service;

import com.example.demo.Entity.Phieunhap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.List;

public interface PhieunhapService {

    List<Phieunhap> getAllPhieunhap();

    Phieunhap getPhieunhapById(int MAPHIEUNHAP);

    void savePhieuNhap(Phieunhap phieunhap, String chiTietListJson, float tongTien) throws Exception;

}
