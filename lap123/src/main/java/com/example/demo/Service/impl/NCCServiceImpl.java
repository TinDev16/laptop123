package com.example.demo.Service.impl;

import com.example.demo.Entity.NCC;
import com.example.demo.Repository.NCCRepository;
import com.example.demo.Service.NCCService;
import com.example.demo.Service.ReferencedRecordDeleteGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NCCServiceImpl implements NCCService {

    NCCRepository nccRepository;
    private final ReferencedRecordDeleteGuard deleteGuard;

    public NCCServiceImpl(NCCRepository nccRepository, ReferencedRecordDeleteGuard deleteGuard) {
        super();
        this.nccRepository = nccRepository;
        this.deleteGuard = deleteGuard;
    }

    @Override
    public List<NCC> getAllNCC() {
        return nccRepository.findAll();
    }

    @Override
    public NCC createNCC(NCC ncc) {
        return nccRepository.save(ncc);
    }

    @Override
    public NCC saveNCC(NCC ncc) {
        return nccRepository.save(ncc);
    }

    @Override
    public NCC getNCCById(int MANCC) {
        Optional<NCC> thuonghieu = nccRepository.findById(MANCC);
        if (thuonghieu.isPresent()) {
            return thuonghieu.get();
        } else {
            return null;
        }
    }

    @Override
    public NCC updateNCC(NCC ncc) {
        return nccRepository.save(ncc);
    }

    @Override
    public void deleteNCC(int id, String GHICHU) {
        NCC ncc = nccRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Không có nhà cung cấp" + id));
        ncc.setTRANGTHAI(1);
        ncc.setGHICHU(GHICHU);
        nccRepository.save(ncc);
    }

    @Override
    @Transactional
    public void permanentlyDeleteNCC(int id) {
        NCC ncc = nccRepository.findForDeletion(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp."));
        if (ncc.getTRANGTHAI() != 1) {
            throw new IllegalStateException("Chỉ có thể xóa nhà cung cấp đã ngừng hợp tác.");
        }
        deleteGuard.checkUnreferenced("ncc", id);
        nccRepository.delete(ncc);
        nccRepository.flush();
    }
}
