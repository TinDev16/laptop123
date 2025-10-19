package com.example.demo.Service;

import com.example.demo.Entity.Phieuxuat;
import java.math.BigDecimal;
import java.util.List;

public interface PhieuxuatService {

    List<Phieuxuat> getAllPhieuxuat();

    void savePhieuXuat(Phieuxuat phieuxuat, String chiTietListJson, BigDecimal tongTien) throws Exception;

    void update(Phieuxuat phieuxuat);

    void deletePhieuxuat(int id);

    Phieuxuat getPhieuxuatById(int id);

}
