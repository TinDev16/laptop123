package com.example.demo.Service;

import com.example.demo.Entity.CT_Phieuxuat;

public interface CT_PhieuxuatService {
    void save(CT_Phieuxuat ct);
    CT_Phieuxuat getById(int id);
}
