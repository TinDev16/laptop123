package com.example.demo.excel.dto;

import java.math.BigDecimal;

public class MaterialExcelDto {

    private int rowNumber;
    private String code;           // Mã NVL (manvl_code, VD: NVL-CPU-001)
    private String name;           // Tên linh kiện (tennvl)
    private String categoryName;   // Tên loại linh kiện (tenloai, VD: CPU, RAM)
    private Integer categoryId;    // maloai (resolved)
    private String unit;           // Đơn vị tính (donvitinh)
    private BigDecimal importPrice;// Đơn giá nhập (dongianhap)
    private BigDecimal minStock;   // Tồn tối thiểu (ton_toithieu)
    private Integer leadTimeDays;  // Thời gian giao hàng (ngày)
    private String note;           // Ghi chú (ghichu)
    private String action;         // THEM_MOI hoặc CAP_NHAT

    public MaterialExcelDto() {
        this.unit = "Cái";
        this.importPrice = BigDecimal.ZERO;
        this.minStock = BigDecimal.ZERO;
        this.leadTimeDays = 0;
        this.action = "THEM_MOI";
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
    }

    public BigDecimal getMinStock() {
        return minStock;
    }

    public void setMinStock(BigDecimal minStock) {
        this.minStock = minStock;
    }

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(Integer leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
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
