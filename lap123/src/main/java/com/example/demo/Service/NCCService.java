package com.example.demo.Service;

import com.example.demo.Entity.NCC;
import com.example.demo.Repository.NCCRepository;

import java.util.List;

public interface NCCService {

    List<NCC> getAllNCC();

    NCC createNCC(NCC ncc);

    NCC saveNCC(NCC ncc);

    NCC getNCCById(int id);

    NCC updateNCC(NCC ncc);

    void deleteNCC(int id, String GHICHU);
}
