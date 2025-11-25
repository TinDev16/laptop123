package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;



@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    public CustomAuthSuccessHandler successHandler;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService getDetailService() {
        return new CustomEmployeeDetailService();
    }

    @Bean
    public DaoAuthenticationProvider getAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(getDetailService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập file tĩnh và trang login
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/style.css", "/style1.css", "/uploads/**").permitAll()
                        .requestMatchers("/", "/signin", "/userLogin", "/forgotPassword", "/newPassword","/forgot-password","/reset-password").permitAll()

                        .requestMatchers(

                                "/phieunhap/**",
                                "/view-phieunhap/**",
                                "/edit-phieunhap/**",
                                "/update-phieunhap",
                                "/delete-phieunhap/**",


                                "/phieuxuat/**",
                                "/view-phieuxuat/**",
                                "/edit-phieuxuat/**",
                                "/update-phieuxuat",
                                "/delete-phieuxuat/**",


                                "/tonkho/**"
                        ).hasAnyAuthority("NV_nhap_xuat", "QLy", "ADMIN")


                        .requestMatchers("/quanlytaikhoan/**").hasAuthority("ADMIN")

                        .requestMatchers("/**").hasAnyAuthority("QLy","ADMIN")

                        .anyRequest().authenticated()

                )
                .formLogin(form -> form
                        .loginPage("/signin")
                        .loginProcessingUrl("/userLogin")
                        .successHandler(successHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/signin?logout")
                        .permitAll()
                );

        return http.build();
    }
}