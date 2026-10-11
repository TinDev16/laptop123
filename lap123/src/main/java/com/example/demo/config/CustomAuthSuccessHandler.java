package com.example.demo.config;

import java.io.IOException;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        Collection<? extends GrantedAuthority> authorities =
                authentication.getAuthorities();

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();

            if ("ROLE_ADMIN".equals(role)) {
                response.sendRedirect("/admin");
                return;
            }

            if ("ROLE_QUAN_LY_KHO".equals(role)) {
                response.sendRedirect("/dashboard");
                return;
            }

            if ("ROLE_NHAN_VIEN".equals(role)) {
                response.sendRedirect("/dashboard");
                return;
            }
        }

        response.sendRedirect("/signin?error");
    }
}