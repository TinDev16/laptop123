package com.example.demo.Service;

import com.example.demo.Entity.Thuonghieu;
import java.util.List;

public interface ThuonghieuService {

    List<Thuonghieu> getAllThuonghieu();

    Thuonghieu create(Thuonghieu thuonghieu);

    Thuonghieu save(Thuonghieu thuonghieu);

    Thuonghieu getThuonghieuById(int id);

    Thuonghieu updateThuonghieu(Thuonghieu thuonghieu);

    void deleteThuonghieu(int id, String GHICHU);
}
