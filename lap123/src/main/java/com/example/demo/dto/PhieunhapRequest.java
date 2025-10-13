package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

public class PhieunhapRequest {
    private int MANCC;
    private int MANV;
    private double TONGTIEN;
    private String GHICHU;
    private List<ChiTietNhapDTO> chiTietList;

    // getter/setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChiTietNhapDTO {
        private int MASP;
        private int SOLUONG;
        private BigDecimal GIANHAP;

        // getter/setter

        public int getMASP() {
            return MASP;
        }

        public void setMASP(int MASP) {
            this.MASP = MASP;
        }

        public int getSOLUONG() {
            return SOLUONG;
        }

        public void setSOLUONG(int SOLUONG) {
            this.SOLUONG = SOLUONG;
        }

        public BigDecimal getGIANHAP() {
            return GIANHAP;
        }

        public void setGIANHAP(BigDecimal GIANHAP) {
            this.GIANHAP = GIANHAP;
        }
    }
}
