package com.example.demo.Service.impl;

import com.example.demo.Entity.Thuonghieu;
import com.example.demo.Repository.ThuonghieuRepository;
import com.example.demo.Service.ThuonghieuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ThuonghieuServiceImpl implements ThuonghieuService {

    @Autowired
    ThuonghieuRepository thuonghieuRepository;

    public ThuonghieuServiceImpl(ThuonghieuRepository thuonghieuRepository) {
        super();
        this.thuonghieuRepository = thuonghieuRepository;
    }

    @Override
    public List<Thuonghieu> getAllThuonghieu() {
        return thuonghieuRepository.findAll();
    }

    @Override
    public Thuonghieu create(Thuonghieu thuonghieu) {
        return thuonghieuRepository.save(thuonghieu);
    }

    @Override
    public Thuonghieu save(Thuonghieu thuonghieu) {
        return thuonghieuRepository.save(thuonghieu);
    }

    @Override
    public Thuonghieu getThuonghieuById(int MATHUONGHIEU) {
        Optional<Thuonghieu> thuonghieu = thuonghieuRepository.findById(MATHUONGHIEU);
        if (thuonghieu.isPresent()) {
            return thuonghieu.get();
        } else {
            return null;
        }
    }

    @Override
    public Thuonghieu updateThuonghieu(Thuonghieu thuonghieu) {
        return thuonghieuRepository.save(thuonghieu);
    }

    @Override
    public void deleteThuonghieu(int id, String GHICHU) {

        Thuonghieu thuonghieu = thuonghieuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với id=" + id));

        thuonghieu.setTRANGTHAI(1); // chuyển sang ngừng
        thuonghieu.setGHICHU(GHICHU);
        thuonghieuRepository.save(thuonghieu);
    }

    @Override
    public void restoreThuonghieu(int id) {
        Thuonghieu thuonghieu = thuonghieuRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy thương hiệu với id=" + id));

        thuonghieu.setTRANGTHAI(0);
        thuonghieu.setGHICHU(null);

        thuonghieuRepository.save(thuonghieu);
    }


}
