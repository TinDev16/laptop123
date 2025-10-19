package com.example.demo.Service.impl;

import com.example.demo.Entity.*;
import com.example.demo.Repository.CT_PhieuxuatRepository;
import com.example.demo.Repository.PhieuxuatRepository;
import com.example.demo.Repository.SanphamRepository;
import com.example.demo.Service.PhieuxuatService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class PhieuxuatServiceImpl implements PhieuxuatService {

    @Autowired
    SanphamRepository sanphamRepository;

    @Autowired
    CT_PhieuxuatRepository ctPhieuxuatRepository;

    @Autowired
    PhieuxuatRepository phieuxuatRepository;

    @Override
    public List<Phieuxuat> getAllPhieuxuat() {
        return phieuxuatRepository.findAll();
    }

    @Override
    public void update(Phieuxuat phieuxuat) {
        phieuxuatRepository.save(phieuxuat);
    }

    @Override
    public Phieuxuat getPhieuxuatById(int id) {
        return phieuxuatRepository.findById(id).orElse(null);
    }

    @Transactional
    public void savePhieuXuat(Phieuxuat phieuxuat, String chiTietListJson, BigDecimal tongTien) throws Exception {
        // 1️⃣ Gán thông tin cho phiếu nhập
        phieuxuat.setTHOIGIAN(new Date());
        phieuxuat.setTONGTIEN(tongTien);
        phieuxuat.setTRANGTHAI(0);

        // 2️⃣ Lưu phiếu nhập chính
        Phieuxuat savedPhieuXuat = phieuxuatRepository.save(phieuxuat);

        // 3️⃣ Parse danh sách chi tiết
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> chiTietList =
                mapper.readValue(chiTietListJson, new TypeReference<>() {});

        // 4️⃣ Lưu chi tiết phiếu nhập (chưa cộng tồn kho)
        for (Map<String, Object> item : chiTietList) {
            int masp = (int) item.get("MASP");
            int soluong = (int) item.get("SOLUONG");
            double gianhap = Double.parseDouble(item.get("GIAXUAT").toString());

            CT_Phieuxuat ct = new CT_Phieuxuat();
            ct.setPhieuxuat(savedPhieuXuat);
            ct.setSanpham(sanphamRepository.findById(masp).orElse(null));
            ct.setSoluong(soluong);
            ct.setGiaxuat(BigDecimal.valueOf(gianhap));
            ct.setTRANGTHAI(0); // đang chờ nhập hàng
            ctPhieuxuatRepository.save(ct);
        }
    }

    @Override
    public void deletePhieuxuat(int id) {
        phieuxuatRepository.deleteById(id);
    }
}
