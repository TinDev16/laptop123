package com.example.demo.Service;
import com.example.demo.Entity.CT_Phieunhap;

public interface CT_PhieunhapService {
    void save(CT_Phieunhap ct);
    CT_Phieunhap getById(int id);
}