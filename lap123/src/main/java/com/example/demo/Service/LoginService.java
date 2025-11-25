package com.example.demo.Service;

import jakarta.servlet.http.HttpSession;

public interface LoginService {
    void handleNewPassword(String email, HttpSession session);

}

