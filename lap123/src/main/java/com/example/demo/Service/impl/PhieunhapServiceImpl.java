package com.example.demo.Service.impl;

import com.example.demo.Entity.CT_Phieunhap;
import com.example.demo.Entity.Phieunhap;
import com.example.demo.Entity.Sanpham;
import com.example.demo.Repository.CT_PhieunhapRepository;
import com.example.demo.Repository.PhieunhapRepository;
import com.example.demo.Repository.SanphamRepository;
import com.example.demo.Service.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PhieunhapServiceImpl implements PhieunhapService {

    @Autowired
    private CT_PhieunhapRepository ctPhieunhapRepository;

    @Autowired
    private PhieunhapRepository phieunhapRepository;

    @Autowired
    private SanphamRepository sanphamRepository;

    @Autowired
    private SanphamService sanphamService;

    @Autowired
    private NCCService nccService;

    @Autowired
    private NhanvienService nhanvienService;

    @Override
    public List<Phieunhap> getAllPhieunhap() {
        return phieunhapRepository.findAll();
    }

    @Override
    public Phieunhap getPhieunhapById(int id) {
        return phieunhapRepository.findById(id).orElse(null);
    }

    @Transactional
    public void savePhieuNhap(Phieunhap phieunhap, String chiTietListJson, BigDecimal tongTien) throws Exception {
        // 1️⃣ Gán thông tin cho phiếu nhập
        phieunhap.setTHOIGIAN(new Date());
        phieunhap.setTONGTIEN(tongTien);
        phieunhap.setTRANGTHAI(0); // 0 = đang xử lý, 1 = hoàn tất

        // 2️⃣ Lưu phiếu nhập chính
        Phieunhap savedPhieuNhap = phieunhapRepository.save(phieunhap);

        // 3️⃣ Parse danh sách chi tiết
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> chiTietList =
                mapper.readValue(chiTietListJson, new TypeReference<>() {});

        // 4️⃣ Lưu chi tiết phiếu nhập (chưa cộng tồn kho)
        for (Map<String, Object> item : chiTietList) {
            int masp = (int) item.get("MASP");
            int soluong = (int) item.get("SOLUONG");
            double gianhap = Double.parseDouble(item.get("GIANHAP").toString());

            CT_Phieunhap ct = new CT_Phieunhap();
            ct.setPhieunhap(savedPhieuNhap);
            ct.setSanpham(sanphamRepository.findById(masp).orElse(null));
            ct.setSoluong(soluong);
            ct.setGianhap(BigDecimal.valueOf(gianhap));
            ct.setTrangthai(0); // đang chờ nhập hàng
            ctPhieunhapRepository.save(ct);
        }
    }


    public void update(Phieunhap pn) {
        phieunhapRepository.save(pn);// Hibernate sẽ tự update
    }

    public void deletePhieunhap(int id) {
        phieunhapRepository.deleteById(id);
    }

}
