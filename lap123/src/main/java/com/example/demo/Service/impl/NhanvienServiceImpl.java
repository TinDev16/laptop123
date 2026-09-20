package com.example.demo.Service.impl;

import com.example.demo.Entity.Nhanvien;
import com.example.demo.Repository.NhanvienRepository;
import com.example.demo.Service.NhanvienService;
import com.example.demo.Service.ReferencedRecordDeleteGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NhanvienServiceImpl implements NhanvienService {

    private final NhanvienRepository nhanvienRepository;
    private final ReferencedRecordDeleteGuard deleteGuard;

    public NhanvienServiceImpl(NhanvienRepository nhanvienRepository, ReferencedRecordDeleteGuard deleteGuard) {
        super();
        this.nhanvienRepository = nhanvienRepository;
        this.deleteGuard = deleteGuard;
    }

    @Override
    public List<Nhanvien> getAllNhanvien() {
        return nhanvienRepository.findAll();
    }

    @Override
    public Nhanvien createNhanvien(Nhanvien Nhanvien) {
        return nhanvienRepository.save(Nhanvien);
    }

    @Override
    public Nhanvien saveNhanvien(Nhanvien nhanvien) {
        return nhanvienRepository.save(nhanvien);
    }



    @Override
    public Nhanvien getNhanvienById(int MANV) {
        Optional<Nhanvien> nhanvien = nhanvienRepository.findById(MANV);
        return nhanvien.orElse(null);
    }

    @Override
    public Nhanvien updateNhanvien(Nhanvien nhanvien) {
        return nhanvienRepository.save(nhanvien);
    }

    @Override
    public void deleteNhanvien(int id, String GHICHU) {

        Nhanvien nhanvien = nhanvienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với id=" + id));

        nhanvien.setTRANGTHAI(1);
        nhanvien.setGHICHU(GHICHU);
        nhanvienRepository.save(nhanvien);
    }

    @Override
    @Transactional
    public void permanentlyDeleteNhanvien(int id) {
        Nhanvien nhanvien = nhanvienRepository.findForDeletion(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên."));
        if (nhanvien.getTRANGTHAI() != 1) {
            throw new IllegalStateException("Chỉ có thể xóa nhân viên đã nghỉ việc.");
        }
        deleteGuard.checkUnreferenced("nhanvien", id);
        nhanvienRepository.delete(nhanvien);
        nhanvienRepository.flush();
    }
}
