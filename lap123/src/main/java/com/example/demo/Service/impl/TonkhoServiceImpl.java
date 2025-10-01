package com.example.demo.Service.impl;

import com.example.demo.Entity.Tonkho;
import com.example.demo.Repository.TonkhoRepository;
import com.example.demo.Service.TonkhoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TonkhoServiceImpl implements TonkhoService {

    TonkhoRepository tonkhoRepository;

    public TonkhoServiceImpl(TonkhoRepository tonkhoRepository) {
        super();
        this.tonkhoRepository = tonkhoRepository;
    }

    @Override
    public List<Tonkho> getAllTonkho() {
        return tonkhoRepository.findAll();
    }

}
