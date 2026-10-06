package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthSuccessHandler successHandler;

    @Autowired
    private CustomEmployeeDetailService detailService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider getAuthenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(detailService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        /*
                         * =========================================================
                         * PUBLIC - CSS / JS / IMAGES
                         * =========================================================
                         */
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/style.css",
                                "/style1.css",
                                "/script.js",
                                "/sidebar.js",
                                "/number-format.js",
                                "/status-format.js",
                                "/uploads/**"
                        ).permitAll()


                        /*
                         * =========================================================
                         * PUBLIC - LOGIN / PASSWORD
                         * =========================================================
                         */
                        .requestMatchers(
                                "/signin",
                                "/userLogin",
                                "/forgotPassword",
                                "/newPassword",
                                "/forgot-password",
                                "/reset-password"
                        ).permitAll()



                        /*
                         * =========================================================
                         * QUẢN LÝ TÀI KHOẢN
                         * Chỉ QUẢN LÝ KHO
                         * =========================================================
                         */
                        .requestMatchers(
                                "/quanlytaikhoan/**"
                        ).hasRole("QUAN_LY_KHO")


                        /*
                         * =========================================================
                         * DUYỆT / TỪ CHỐI
                         * Chỉ QUẢN LÝ KHO
                         * =========================================================
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/kho/xuat-nvl/*/duyet",
                                "/kho/xuat-nvl/*/tu-choi",
                                "/kho/xuat-thanh-pham/*/duyet",
                                "/kho/xuat-thanh-pham/*/tu-choi",
                                "/san-xuat/ket-qua/*/confirm"
                        ).hasRole("QUAN_LY_KHO")


                        /*
                         * =========================================================
                         * TẠO YÊU CẦU SẢN XUẤT
                         * Chỉ QUẢN LÝ KHO
                         * =========================================================
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/san-xuat/yeu-cau"
                        ).hasRole("QUAN_LY_KHO")


                        /*
                         * =========================================================
                         * NHÂN VIÊN - TẠO PHIẾU / KẾT QUẢ
                         * =========================================================
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/kho/xuat-nvl",
                                "/san-xuat/ket-qua",
                                "/kho/xuat-thanh-pham"
                        ).hasRole("NHAN_VIEN")


                        /*
                         * =========================================================
                         * NHÂN VIÊN - TRANG CREATE
                         * =========================================================
                         */
                        .requestMatchers(
                                "/kho/xuat-nvl/create",
                                "/san-xuat/ket-qua/create",
                                "/kho/xuat-thanh-pham/create"
                        ).hasRole("NHAN_VIEN")


                        /*
                         * =========================================================
                         * BOM VERSION API
                         * QUẢN LÝ KHO + NHÂN VIÊN
                         * =========================================================
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/bom/versions"
                        ).hasAnyRole(
                                "QUAN_LY_KHO",
                                "NHAN_VIEN"
                        )

                        /*
                         * =========================================================
                         * LỊCH SỬ KHO SẢN XUẤT
                         *
                         * Chỉ NHÂN VIÊN được truy cập.
                         * QUẢN LÝ KHO không được truy cập.
                         * =========================================================
                         */
                        .requestMatchers(
                                "/kho-san-xuat/lich-su",
                                "/kho-san-xuat/lich-su/**"
                        ).hasRole("NHAN_VIEN")


                        /*
                         * =========================================================
                         * KHO SẢN XUẤT
                         *
                         * Hai trang này là trang XEM.
                         * QUẢN LÝ KHO + NHÂN VIÊN đều được truy cập.
                         * =========================================================
                         */
                        .requestMatchers(
                                "/kho-san-xuat/nvl",
                                "/kho-san-xuat/thanh-pham",
                                "/kho-san-xuat/thanh-pham/bom"
                        ).hasAnyRole(
                                "QUAN_LY_KHO",
                                "NHAN_VIEN"
                        )


                        /*
                         * =========================================================
                         * XEM CHI TIẾT SẢN PHẨM / BOM
                         *
                         * Nhân viên cần quyền GET để mở:
                         *
                         * /view-sanpham/1?from=kho-san-xuat
                         * /bom/4?from=kho-san-xuat
                         *
                         * =========================================================
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/view-sanpham/**",
                                "/bom/**"
                        ).hasAnyRole(
                                "QUAN_LY_KHO",
                                "NHAN_VIEN"
                        )


                        /*
                         * =========================================================
                         * BOM - CÁC CHỨC NĂNG QUẢN LÝ
                         *
                         * Các URL này vẫn chỉ QUẢN LÝ KHO.
                         *
                         * LƯU Ý:
                         * Không đặt "/bom/{id}" ở đây.
                         * GET /bom/** phía trên đã xử lý trang xem BOM.
                         * =========================================================
                         */
                        .requestMatchers(
                                "/san-xuat/yeu-cau/create",
                                "/bom",
                                "/bom/create",
                                "/bom/*/disable",
                                "/bom/materials-by-bom",
                                "/nguyen-vat-lieu",
                                "/nguyen-vat-lieu/**",
                                "/kho/nhap-nvl",
                                "/kho/nhap-nvl/**",
                                "/kho/lich-su",
                                "/baocao",
                                "/baocao/**",
                                "/nhanvien/**",
                                "/sanpham/**",
                                "/ncc/**",
                                "/khachhang/**",
                                "/thuonghieu/**"
                        ).hasRole("QUAN_LY_KHO")


                        /*
                         * =========================================================
                         * CÁC TRANG NHÂN VIÊN + QUẢN LÝ KHO
                         * =========================================================
                         */
                        .requestMatchers(
                                "/kho/xuat-nvl",
                                "/kho/xuat-nvl/**",
                                "/kho/xuat-thanh-pham",
                                "/kho/xuat-thanh-pham/**",
                                "/san-xuat/yeu-cau",
                                "/san-xuat/yeu-cau/**",
                                "/san-xuat/ket-qua",
                                "/san-xuat/ket-qua/**",
                                "/dashboard",
                                "/tai-khoan/ca-nhan"
                        ).hasAnyRole(
                                "QUAN_LY_KHO",
                                "NHAN_VIEN"
                        )

                        /*
                         * =========================================================
                         * GHI NHẬN - NHẬP KHO SẢN XUẤT
                         * Chỉ NHÂN VIÊN
                         * =========================================================
                         */
                        .requestMatchers(
                                "/san-xuat/ghi-nhan",
                                "/san-xuat/ghi-nhan/**"
                        ).hasRole("NHAN_VIEN")

                        /*
                         * =========================================================
                         * PHIẾU / TỒN KHO / XƯỞNG
                         * Chỉ QUẢN LÝ KHO
                         * =========================================================
                         */
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

                                "/tonkho/**",
                                "/xuong-lap-rap/**",
                                "/de-xuat-nvl/**",
                                "/de-xuat-tp/**"
                        ).hasRole("QUAN_LY_KHO")


                        /*
                         * =========================================================
                         * MẶC ĐỊNH
                         * Những URL chưa khai báo -> chỉ QUẢN LÝ KHO
                         * =========================================================
                         */
                        .requestMatchers("/**")
                        .hasRole("QUAN_LY_KHO")


                        /*
                         * =========================================================
                         * CÒN LẠI
                         * =========================================================
                         */
                        .anyRequest()
                        .authenticated()
                )


                /*
                 * =========================================================
                 * LOGIN
                 * =========================================================
                 */
                .formLogin(form -> form
                        .loginPage("/signin")
                        .loginProcessingUrl("/userLogin")
                        .successHandler(successHandler)
                        .permitAll()
                )


                /*
                 * =========================================================
                 * LOGOUT
                 * =========================================================
                 */
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/signin?logout")
                        .permitAll()
                );

        return http.build();
    }
}