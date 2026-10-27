package com.example.demo.excel.service;

import com.example.demo.excel.core.ExcelCoreUtils;
import com.example.demo.excel.core.ExcelImportResult;
import com.example.demo.excel.core.ExcelSecurityValidator;
import com.example.demo.excel.dto.MaterialTypeExcelDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class MaterialTypeExcelService {

    private final JdbcTemplate jdbc;

    public MaterialTypeExcelService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 1. TẢI FILE MẪU EXCEL LOẠI NGUYÊN VẬT LIỆU
     */
    public byte[] generateTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("LOAI_NVL");

            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle instructionStyle = ExcelCoreUtils.createInstructionStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);

            String[] headers = {
                    "STT",
                    "Tên loại nguyên vật liệu (*)",
                    "Trạng thái (HOAT_DONG / NGUNG_SU_DUNG)",
                    "Hành động (THEM_MOI / CAP_NHAT)"
            };

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(28);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            Row instructRow = sheet.createRow(1);
            instructRow.setHeightInPoints(24);
            instructRow.createCell(0).setCellValue("HD:");
            instructRow.createCell(1).setCellValue("Nhập tên nhóm loại linh kiện (VD: RAM, CPU, Pin, Mainboard...)");
            instructRow.createCell(2).setCellValue("HOAT_DONG hoặc NGUNG_SU_DUNG (Mặc định: HOAT_DONG)");
            instructRow.createCell(3).setCellValue("THEM_MOI (nếu chưa có) hoặc CAP_NHAT (nếu muốn đổi trạng thái)");
            for (int i = 0; i < headers.length; i++) {
                if (instructRow.getCell(i) != null) {
                    instructRow.getCell(i).setCellStyle(instructionStyle);
                }
            }

            // Dòng dữ liệu mẫu
            String[][] samples = {
                    {"1", "Bộ vi xử lý (CPU)", "HOAT_DONG", "THEM_MOI"},
                    {"2", "Bộ nhớ trong (RAM)", "HOAT_DONG", "THEM_MOI"},
                    {"3", "Bo mạch chủ (Mainboard)", "HOAT_DONG", "THEM_MOI"},
                    {"4", "Màn hình hiển thị", "HOAT_DONG", "THEM_MOI"},
                    {"5", "Pin Laptop", "HOAT_DONG", "THEM_MOI"}
            };

            for (int r = 0; r < samples.length; r++) {
                Row row = sheet.createRow(r + 2);
                row.setHeightInPoints(22);
                for (int c = 0; c < samples[r].length; c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(samples[r][c]);
                    cell.setCellStyle(dataStyle);
                }
            }

            ExcelCoreUtils.autoSizeColumns(sheet, headers.length);

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                workbook.write(bos);
                return bos.toByteArray();
            }
        }
    }

    /**
     * 2. XUẤT DANH SÁCH LOẠI NGUYÊN VẬT LIỆU HIỆN CÓ
     */
    public byte[] exportCurrentTypes() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("DANH_SACH_LOAI_NVL");

            CellStyle titleStyle = workbook.createCellStyle();
            Font fontTitle = workbook.createFont();
            fontTitle.setBold(true);
            fontTitle.setColor(IndexedColors.WHITE.getIndex());
            fontTitle.setFontHeightInPoints((short) 13);
            titleStyle.setFont(fontTitle);
            titleStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle metaStyle = workbook.createCellStyle();
            Font fontMeta = workbook.createFont();
            fontMeta.setItalic(true);
            fontMeta.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            fontMeta.setFontHeightInPoints((short) 10);
            metaStyle.setFont(fontMeta);

            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);
            CellStyle numberStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0");

            // Dòng tiêu đề
            Row r0 = sheet.createRow(0);
            r0.setHeightInPoints(32);
            Cell cTitle = r0.createCell(0);
            cTitle.setCellValue("DANH SÁCH PHÂN LOẠI NGUYÊN VẬT LIỆU");
            cTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Thời điểm xuất danh sách: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            r1.getCell(0).setCellStyle(metaStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

            String[] headers = {"STT", "Mã loại", "Tên loại nguyên vật liệu", "Số linh kiện liên kết", "Trạng thái"};
            Row r3 = sheet.createRow(3);
            r3.setHeightInPoints(26);
            for (int i = 0; i < headers.length; i++) {
                Cell c = r3.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // Truy vấn CSDL
            List<Map<String, Object>> list = jdbc.queryForList("""
                SELECT l.maloai, l.tenloai, l.trangthai, COUNT(n.manvl) AS so_linh_kien
                FROM loai_nguyen_vat_lieu l
                LEFT JOIN nguyenvatlieu n ON n.maloai = l.maloai
                GROUP BY l.maloai, l.tenloai, l.trangthai
                ORDER BY l.tenloai ASC
            """);

            int rowIdx = 4;
            int stt = 1;
            for (Map<String, Object> item : list) {
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(22);

                row.createCell(0).setCellValue(stt++);
                row.getCell(0).setCellStyle(dataStyle);

                row.createCell(1).setCellValue(String.valueOf(item.get("maloai")));
                row.getCell(1).setCellStyle(dataStyle);

                row.createCell(2).setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) item.get("tenloai")));
                row.getCell(2).setCellStyle(dataStyle);

                Number count = (Number) item.get("so_linh_kien");
                Cell cCount = row.createCell(3);
                cCount.setCellValue(count != null ? count.intValue() : 0);
                cCount.setCellStyle(numberStyle);

                Number status = (Number) item.get("trangthai");
                row.createCell(4).setCellValue(status != null && status.intValue() == 1 ? "Hoạt động" : "Ngừng sử dụng");
                row.getCell(4).setCellStyle(dataStyle);
            }

            ExcelCoreUtils.autoSizeColumns(sheet, headers.length);

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                workbook.write(bos);
                return bos.toByteArray();
            }
        }
    }

    /**
     * 3. KIỂM TRA & XEM TRƯỚC FILE EXCEL TẢI LÊN
     */
    public ExcelImportResult<MaterialTypeExcelDto> validateAndPreview(InputStream inputStream, long fileSize) throws IOException {
        ExcelSecurityValidator.validateFileSize(fileSize);

        byte[] fileBytes = inputStream.readAllBytes();
        ExcelSecurityValidator.validateXlsxMagicBytes(new ByteArrayInputStream(fileBytes));

        ExcelImportResult<MaterialTypeExcelDto> result = new ExcelImportResult<>();

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileBytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            if (lastRowNum < 2) {
                result.addError(0, "Dữ liệu", "", "Tệp Excel không chứa dữ liệu hợp lệ (Dữ liệu bắt đầu từ dòng 3).");
                return result;
            }

            // Lấy danh sách tên loại đã có trong DB
            List<String> existingNamesInDb = jdbc.queryForList("SELECT LOWER(tenloai) FROM loai_nguyen_vat_lieu", String.class);
            Set<String> dbNameSet = new HashSet<>(existingNamesInDb);
            Set<String> fileNames = new HashSet<>();

            int totalRowsFound = 0;

            for (int r = 2; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String name = ExcelCoreUtils.getCellStringValue(row.getCell(1));
                String statusStr = ExcelCoreUtils.getCellStringValue(row.getCell(2));
                String actionStr = ExcelCoreUtils.getCellStringValue(row.getCell(3));

                // Bỏ qua dòng trống hoàn toàn
                if (name.isEmpty() && statusStr.isEmpty() && actionStr.isEmpty()) {
                    continue;
                }

                totalRowsFound++;
                int displayRow = r + 1;

                // 1. Kiểm tra Tên loại
                if (name.isEmpty()) {
                    result.addError(displayRow, "Tên loại NVL", "", "Tên loại nguyên vật liệu không được để trống.");
                    continue;
                }
                if (name.length() > 255) {
                    result.addError(displayRow, "Tên loại NVL", name, "Tên loại vượt quá độ dài tối đa (255 ký tự).");
                    continue;
                }

                String lowerName = name.toLowerCase();
                if (fileNames.contains(lowerName)) {
                    result.addError(displayRow, "Tên loại NVL", name, "Tên loại này bị trùng lặp với một dòng khác trong cùng tệp Excel.");
                    continue;
                }
                fileNames.add(lowerName);

                // 2. Kiểm tra Hành động
                String action = actionStr.toUpperCase().trim();
                if (action.isEmpty()) {
                    action = "THEM_MOI";
                }
                if (!action.equals("THEM_MOI") && !action.equals("CAP_NHAT")) {
                    result.addError(displayRow, "Hành động", actionStr, "Hành động không hợp lệ. Chỉ chấp nhận THEM_MOI hoặc CAP_NHAT.");
                    continue;
                }

                // 3. Kiểm tra Trạng thái
                String status = statusStr.toUpperCase().trim();
                if (status.isEmpty() || status.equals("1") || status.equals("HOẠT ĐỘNG")) {
                    status = "HOAT_DONG";
                } else if (status.equals("0") || status.equals("NGỪNG SỬ DỤNG")) {
                    status = "NGUNG_SU_DUNG";
                }
                if (!status.equals("HOAT_DONG") && !status.equals("NGUNG_SU_DUNG")) {
                    result.addError(displayRow, "Trạng thái", statusStr, "Trạng thái không hợp lệ. Chỉ chấp nhận HOAT_DONG hoặc NGUNG_SU_DUNG.");
                    continue;
                }

                // 4. Ràng buộc với DB
                if (action.equals("THEM_MOI") && dbNameSet.contains(lowerName)) {
                    result.addError(displayRow, "Tên loại NVL", name, "Loại NVL này đã tồn tại trong CSDL. Vui lòng chọn hành động CAP_NHAT.");
                    continue;
                }
                if (action.equals("CAP_NHAT") && !dbNameSet.contains(lowerName)) {
                    result.addError(displayRow, "Tên loại NVL", name, "Loại NVL này chưa tồn tại trong CSDL để cập nhật. Vui lòng chọn THEM_MOI.");
                    continue;
                }

                MaterialTypeExcelDto dto = new MaterialTypeExcelDto();
                dto.setRowNumber(displayRow);
                dto.setName(name);
                dto.setStatus(status);
                dto.setAction(action);

                result.getValidRows().add(dto);
            }

            result.setTotalRows(totalRowsFound);
            result.setValidCount(result.getValidRows().size());
            result.setErrorCount(result.getErrors().size());
            return result;
        }
    }

    /**
     * 4. XÁC NHẬN NẠP DỮ LIỆU VÀO CSDL (ALL-OR-NOTHING TRANSACTION)
     */
    @Transactional
    public int confirmImport(List<MaterialTypeExcelDto> validRows) {
        if (validRows == null || validRows.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (MaterialTypeExcelDto dto : validRows) {
            int statusVal = "HOAT_DONG".equals(dto.getStatus()) ? 1 : 0;

            if ("THEM_MOI".equals(dto.getAction())) {
                jdbc.update("INSERT INTO loai_nguyen_vat_lieu (tenloai, trangthai) VALUES (?, ?)",
                        dto.getName().trim(), statusVal);
                count++;
            } else if ("CAP_NHAT".equals(dto.getAction())) {
                jdbc.update("UPDATE loai_nguyen_vat_lieu SET trangthai = ? WHERE LOWER(tenloai) = LOWER(?)",
                        statusVal, dto.getName().trim());
                count++;
            }
        }
        return count;
    }
}
