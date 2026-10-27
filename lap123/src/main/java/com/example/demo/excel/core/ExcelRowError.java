package com.example.demo.excel.core;

public class ExcelRowError {

    private int rowNumber;
    private String columnName;
    private String cellValue;
    private String errorMessage;

    public ExcelRowError() {
    }

    public ExcelRowError(int rowNumber, String columnName, String cellValue, String errorMessage) {
        this.rowNumber = rowNumber;
        this.columnName = columnName;
        this.cellValue = cellValue;
        this.errorMessage = errorMessage;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getCellValue() {
        return cellValue;
    }

    public void setCellValue(String cellValue) {
        this.cellValue = cellValue;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
