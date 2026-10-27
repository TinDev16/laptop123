package com.example.demo.excel.dto;

public class MaterialTypeExcelDto {

    private int rowNumber;
    private Integer id;
    private String name;
    private String status;
    private String action; // THEM_MOI / CAP_NHAT

    public MaterialTypeExcelDto() {
    }

    public MaterialTypeExcelDto(int rowNumber, Integer id, String name, String status, String action) {
        this.rowNumber = rowNumber;
        this.id = id;
        this.name = name;
        this.status = status;
        this.action = action;
    }

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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
