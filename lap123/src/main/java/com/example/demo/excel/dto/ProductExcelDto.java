package com.example.demo.excel.dto;

import java.math.BigDecimal;

public class ProductExcelDto {

    private int rowNumber;
    private Integer productId;    // masp (nếu có khi cập nhật)
    private String name;          // tensp (Tên máy, VD: Laptop ASUS ROG Strix G16)
    private String brandName;     // tenthuonghieu (VD: Asus, Lenovo, Dell)
    private Integer brandId;      // mathuonghieu (resolved)
    private String unit;          // donvitinh (Cái)
    private String origin;        // xuatxu
    private String cpu;           // chip (Intel Core i7 13700H...)
    private String os;            // hedieuhanh (Windows 11...)
    private String screenSize;    // kichthuocman (16 inch...)
    private BigDecimal price;     // giaban
    private String note;          // ghichu
    private String action;        // THEM_MOI hoặc CAP_NHAT

    public ProductExcelDto() {
        this.unit = "Cái";
        this.price = BigDecimal.ZERO;
        this.os = "Windows";
        this.action = "THEM_MOI";
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(String screenSize) {
        this.screenSize = screenSize;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
