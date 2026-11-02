package com.example.demo.excel.dto;

public class BrandExcelDto {
    private int rowNumber;
    private Integer id;
    private String name;
    private String status; // HOAT_DONG hoặc TAM_DUNG
    private String note;
    private String action; // THEM_MOI hoặc CAP_NHAT

    public BrandExcelDto() {}

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

