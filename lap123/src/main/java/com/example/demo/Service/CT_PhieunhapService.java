package com.example.demo.Service;

import com.example.demo.Entity.CT_Phieunhap;

import java.math.BigDecimal;
import java.util.List;

public interface CT_PhieunhapService {



    BigDecimal findGiaNhapGanNhatByMasp(int MASP);

    List<CT_Phieunhap> getByPhieunhapId(int phieunhapId);
    List<CT_Phieunhap> getCTPhieunhapByPhieunhapId(int MAPHIEUNHAP);

    void save(CT_Phieunhap ct);
}