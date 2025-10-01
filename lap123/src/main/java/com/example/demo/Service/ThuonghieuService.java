package com.example.demo.Service;

import com.example.demo.Entity.Thuonghieu;
import com.example.demo.Repository.ThuonghieuRepository;

import java.util.List;

public interface ThuonghieuService {

    List<Thuonghieu> getAllThuonghieu();

    Thuonghieu create(Thuonghieu thuonghieu);

    Thuonghieu save(Thuonghieu thuonghieu);

//    Thuonghieu getThuonghieuById(int id);

    void deleteThuonghieu(int id);
}
