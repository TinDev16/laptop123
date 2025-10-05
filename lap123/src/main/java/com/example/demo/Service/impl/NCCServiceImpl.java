package com.example.demo.Service.impl;

import com.example.demo.Entity.NCC;
import com.example.demo.Entity.Thuonghieu;
import com.example.demo.Repository.NCCRepository;
import com.example.demo.Service.NCCService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NCCServiceImpl implements NCCService {

    NCCRepository nccRepository;

    public NCCServiceImpl(NCCRepository nccRepository) {
        super();
        this.nccRepository = nccRepository;
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
    public void deleteNCC(int id) {
        nccRepository.deleteById(id);
    }
}
