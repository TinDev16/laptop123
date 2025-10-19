package com.example.demo.Service;

import com.example.demo.Entity.Taikhoan;

public interface RegisterService {

    public Taikhoan saveAccount(Taikhoan taikhoan);

    public void removeSessionMessage();

    public boolean emailExists(String email);

}
