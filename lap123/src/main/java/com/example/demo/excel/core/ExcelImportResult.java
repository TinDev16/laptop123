package com.example.demo.excel.core;

import java.util.ArrayList;
import java.util.List;

public class ExcelImportResult<T> {

    private int totalRows;
    private int validCount;
    private int errorCount;
    private List<T> validRows = new ArrayList<>();
    private List<ExcelRowError> errors = new ArrayList<>();

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getValidCount() {
        return validCount;
    }

    public void setValidCount(int validCount) {
        this.validCount = validCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public List<T> getValidRows() {
        return validRows;
    }

    public void setValidRows(List<T> validRows) {
        this.validRows = validRows;
    }

    public List<ExcelRowError> getErrors() {
        return errors;
    }

    public void setErrors(List<ExcelRowError> errors) {
        this.errors = errors;
    }

    public void addError(int rowNumber, String columnName, String cellValue, String message) {
        this.errors.add(new ExcelRowError(rowNumber, columnName, cellValue, message));
        this.errorCount = this.errors.size();
    }
}
