package com.example.demo.excel.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ExcelSecurityValidator {

    private static final byte[] XLSX_MAGIC_BYTES = new byte[]{0x50, 0x4B, 0x03, 0x04};
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private ExcelSecurityValidator() {
    }

    /**
     * Kiểm tra file có đúng định dạng OOXML (.xlsx) qua Magic Bytes hay không.
     */
    public static void validateXlsxMagicBytes(InputStream inputStream) throws IOException {
        if (!inputStream.markSupported()) {
            throw new IllegalArgumentException("InputStream phải hỗ trợ mark/reset để kiểm tra định dạng.");
        }

        inputStream.mark(4);
        byte[] header = new byte[4];
        int read = inputStream.read(header);
        inputStream.reset();

        if (read < 4) {
            throw new IllegalArgumentException("Tệp tải lên không hợp lệ hoặc bị rỗng.");
        }

        for (int i = 0; i < 4; i++) {
            if (header[i] != XLSX_MAGIC_BYTES[i]) {
                throw new IllegalArgumentException("Định dạng tệp không hợp lệ! Vui lòng chỉ tải lên tệp Excel định dạng chuẩn (.xlsx).");
            }
        }
    }

    /**
     * Kiểm tra dung lượng tệp tải lên
     */
    public static void validateFileSize(long size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Tệp tải lên không có nội dung.");
        }
        if (size > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Dung lượng tệp vượt quá giới hạn cho phép (Tối đa 10MB).");
        }
    }

    /**
     * Chống CSV/Formula Injection khi xuất ra Excel.
     * Nếu chuỗi bắt đầu bằng '=', '+', '-', '@' thì tự động thêm dấu nháy đơn '
     */
    public static String sanitizeForFormulaInjection(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        char firstChar = value.charAt(0);
        if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@') {
            return "'" + value;
        }
        return value;
    }
}
