package com.example.demo.excel.service;

import com.example.demo.excel.core.ExcelCoreUtils;
import com.example.demo.excel.core.ExcelImportResult;
import com.example.demo.excel.core.ExcelSecurityValidator;
import com.example.demo.excel.dto.BrandExcelDto;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BrandExcelService {

    private final JdbcTemplate jdbc;
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public BrandExcelService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 1. TẠO FILE MẪU EXCEL THƯƠNG HIỆU
     */
    public byte[] generateTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("DANH_MUC_THUONG_HIEU");

            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle instructionStyle = ExcelCoreUtils.createInstructionStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);

            String[] headers = {
                    "STT",
                    "Tên thương hiệu (*)",
                    "Trạng thái (HOAT_DONG / TAM_DUNG)",
                    "Hành động (THEM_MOI / CAP_NHAT)",
                    "Ghi chú"
            };

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(28);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Dòng hướng dẫn
            Row instRow = sheet.createRow(1);
            Cell instCell = instRow.createCell(0);
            instCell.setCellValue("HƯỚNG DẪN: Cột có dấu (*) là bắt buộc. 'Hành động': Để trống hoặc ghi 'THEM_MOI' để thêm thương hiệu mới; ghi 'CAP_NHAT' để cập nhật thương hiệu đã có theo tên. 'Trạng thái': Để trống mặc định là 'HOAT_DONG'.");
            instCell.setCellStyle(instructionStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, headers.length - 1));

            // Dòng mẫu 1
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue(1);
            r2.createCell(1).setCellValue("ASUS");
            r2.createCell(2).setCellValue("HOAT_DONG");
            r2.createCell(3).setCellValue("THEM_MOI");
            r2.createCell(4).setCellValue("Thương hiệu laptop Gaming và Zenbook");

            // Dòng mẫu 2
            Row r3 = sheet.createRow(3);
            r3.createCell(0).setCellValue(2);
            r3.createCell(1).setCellValue("LENOVO");
            r3.createCell(2).setCellValue("HOAT_DONG");
            r3.createCell(3).setCellValue("CAP_NHAT");
            r3.createCell(4).setCellValue("Thương hiệu laptop ThinkPad và Legion");

            for (int r = 2; r <= 3; r++) {
                Row row = sheet.getRow(r);
                for (int c = 0; c < headers.length; c++) {
                    Cell cell = row.getCell(c);
                    if (cell.getCellStyle() == workbook.getCellStyleAt(0)) {
                        cell.setCellStyle(dataStyle);
                    }
                }
            }
            ExcelCoreUtils.autoSizeColumns(sheet, headers.length);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 2. XUẤT TOÀN BỘ DANH SÁCH THƯƠNG HIỆU RA EXCEL
     */
    public byte[] exportBrands() throws IOException {
        String sql = "SELECT mathuonghieu, tenthuonghieu, trangthai, ghichu FROM thuonghieu ORDER BY trangthai ASC, tenthuonghieu ASC";
        List<Map<String, Object>> list = jdbc.queryForList(sql);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("DANH_MUC_THUONG_HIEU");

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setColor(IndexedColors.WHITE.getIndex());
            titleFont.setFontHeightInPoints((short) 13);
            titleStyle.setFont(titleFont);
            titleStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle metaStyle = workbook.createCellStyle();
            Font metaFont = workbook.createFont();
            metaFont.setItalic(true);
            metaFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            metaFont.setFontHeightInPoints((short) 10);
            metaStyle.setFont(metaFont);

            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);

            CellStyle centerDataStyle = workbook.createCellStyle();
            centerDataStyle.cloneStyleFrom(dataStyle);
            centerDataStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle subtotalStyle = workbook.createCellStyle();
            Font subtotalFont = workbook.createFont();
            subtotalFont.setBold(true);
            subtotalStyle.setFont(subtotalFont);
            subtotalStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            subtotalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            subtotalStyle.setBorderTop(BorderStyle.THIN);
            subtotalStyle.setBorderBottom(BorderStyle.DOUBLE);

            // Title Banner
            Row r0 = sheet.createRow(0);
            r0.setHeightInPoints(32);
            Cell cTitle = r0.createCell(0);
            cTitle.setCellValue("DANH SÁCH THƯƠNG HIỆU SẢN PHẨM");
            cTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Thời điểm xuất danh sách: " + LocalDateTime.now().format(DATE_TIME_FMT));
            r1.getCell(0).setCellStyle(metaStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

            String[] headers = {"STT", "Mã thương hiệu", "Tên thương hiệu", "Trạng thái", "Ghi chú"};
            Row rH = sheet.createRow(3);
            rH.setHeightInPoints(26);
            for (int i = 0; i < headers.length; i++) {
                Cell c = rH.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx = 4;
            int stt = 1;
            int countActive = 0;
            int countInactive = 0;

            for (Map<String, Object> item : list) {
                Row r = sheet.createRow(rowIdx++);
                r.setHeightInPoints(22);

                r.createCell(0).setCellValue(stt++);
                r.getCell(0).setCellStyle(centerDataStyle);

                r.createCell(1).setCellValue("TH-" + item.get("mathuonghieu"));
                r.getCell(1).setCellStyle(centerDataStyle);

                r.createCell(2).setCellValue(safeStr(item.get("tenthuonghieu")));
                r.getCell(2).setCellStyle(dataStyle);

                int st = item.get("trangthai") != null ? ((Number) item.get("trangthai")).intValue() : 0;
                String stText = (st == 0) ? "Đang hoạt động" : "Tạm dừng";
                if (st == 0) countActive++; else countInactive++;

                Cell cSt = r.createCell(3);
                cSt.setCellValue(stText);
                cSt.setCellStyle(centerDataStyle);

                r.createCell(4).setCellValue(safeStr(item.get("ghichu")));
                r.getCell(4).setCellStyle(dataStyle);
            }

            // Summary row
            Row rTotal = sheet.createRow(rowIdx);
            rTotal.setHeightInPoints(24);
            Cell cTot = rTotal.createCell(0);
            cTot.setCellValue(String.format("TỔNG CỘNG: %d thương hiệu (Đang hoạt động: %d | Tạm dừng: %d)", list.size(), countActive, countInactive));
            cTot.setCellStyle(subtotalStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 4));

            ExcelCoreUtils.autoSizeColumns(sheet, headers.length);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 3. KIỂM TRA & XEM TRƯỚC (PREVIEW) TỆP EXCEL THƯƠNG HIỆU
     */
    public ExcelImportResult<BrandExcelDto> validateAndPreview(InputStream inputStream, long fileSize) throws IOException {
        ExcelSecurityValidator.validateFileSize(fileSize);

        byte[] fileBytes = inputStream.readAllBytes();
        ExcelSecurityValidator.validateXlsxMagicBytes(new ByteArrayInputStream(fileBytes));
        ExcelImportResult<BrandExcelDto> result = new ExcelImportResult<>();

        // Tra cứu tên thương hiệu hiện có trong DB
        Map<String, Integer> existingBrands = new HashMap<>();
        List<Map<String, Object>> dbList = jdbc.queryForList("SELECT mathuonghieu, LOWER(TRIM(tenthuonghieu)) AS norm_name FROM thuonghieu");
        for (Map<String, Object> row : dbList) {
            String norm = safeStr(row.get("norm_name"));
            int id = ((Number) row.get("mathuonghieu")).intValue();
            existingBrands.put(norm, id);
        }

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileBytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                result.addError(0, "Sheet", "", "Tệp Excel không có trang tính dữ liệu.");
                return result;
            }

            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum < 2) {
                result.addError(0, "Dữ liệu", "", "Tệp Excel không có dòng dữ liệu hợp lệ nào để xử lý.");
                return result;
            }

            Set<String> seenNamesInFile = new HashSet<>();
            int totalProcessed = 0;

            for (int r = 2; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                boolean empty = true;
                for (int c = 0; c < 5; c++) {
                    if (!ExcelCoreUtils.getCellStringValue(row.getCell(c)).isEmpty()) {
                        empty = false;
                        break;
                    }
                }
                if (empty) continue;

                totalProcessed++;
                int rowNum = r + 1;
                boolean hasRowError = false;

                String name = ExcelCoreUtils.getCellStringValue(row.getCell(1)).trim();
                String statusStr = ExcelCoreUtils.getCellStringValue(row.getCell(2)).trim();
                String actionStr = ExcelCoreUtils.getCellStringValue(row.getCell(3)).trim().toUpperCase();
                String note = ExcelCoreUtils.getCellStringValue(row.getCell(4)).trim();

                // 1. Kiểm tra Tên thương hiệu
                if (name.isEmpty()) {
                    result.addError(rowNum, "Tên thương hiệu (*)", "", "Tên thương hiệu không được để trống.");
                    hasRowError = true;
                } else {
                    String normName = name.toLowerCase();
                    if (seenNamesInFile.contains(normName)) {
                        result.addError(rowNum, "Tên thương hiệu (*)", name, "Tên thương hiệu bị lặp lại trong tệp Excel.");
                        hasRowError = true;
                    } else {
                        seenNamesInFile.add(normName);
                    }
                }

                // 2. Xác định Hành động
                String finalAction = actionStr;
                if (finalAction.isEmpty()) {
                    finalAction = existingBrands.containsKey(name.toLowerCase()) ? "CAP_NHAT" : "THEM_MOI";
                } else if (!finalAction.equals("THEM_MOI") && !finalAction.equals("CAP_NHAT")) {
                    result.addError(rowNum, "Hành động", actionStr, "Hành động chỉ chấp nhận 'THEM_MOI' hoặc 'CAP_NHAT'.");
                    hasRowError = true;
                }

                // 3. Đối chiếu DB
                Integer brandId = existingBrands.get(name.toLowerCase());
                if ("THEM_MOI".equals(finalAction) && brandId != null) {
                    result.addError(rowNum, "Tên thương hiệu (*)", name, "Thương hiệu đã tồn tại trong hệ thống (Mã: TH-" + brandId + "). Dùng 'CAP_NHAT' để sửa.");
                    hasRowError = true;
                } else if ("CAP_NHAT".equals(finalAction) && brandId == null) {
                    result.addError(rowNum, "Tên thương hiệu (*)", name, "Không tìm thấy thương hiệu này trong hệ thống để cập nhật.");
                    hasRowError = true;
                }

                // 4. Trạng thái
                String finalStatus = "HOAT_DONG";
                if (!statusStr.isEmpty()) {
                    if ("TAM_DUNG".equalsIgnoreCase(statusStr) || "NGUNG_SU_DUNG".equalsIgnoreCase(statusStr) || "1".equals(statusStr)) {
                        finalStatus = "TAM_DUNG";
                    } else if ("HOAT_DONG".equalsIgnoreCase(statusStr) || "0".equals(statusStr)) {
                        finalStatus = "HOAT_DONG";
                    } else {
                        result.addError(rowNum, "Trạng thái", statusStr, "Trạng thái chỉ chấp nhận 'HOAT_DONG' hoặc 'TAM_DUNG'.");
                        hasRowError = true;
                    }
                }

                if (!hasRowError) {
                    BrandExcelDto dto = new BrandExcelDto();
                    dto.setRowNumber(rowNum);
                    dto.setId(brandId);
                    dto.setName(name);
                    dto.setStatus(finalStatus);
                    dto.setAction(finalAction);
                    dto.setNote(note);
                    result.getValidRows().add(dto);
                }
            }
            result.setTotalRows(totalProcessed);
            result.setValidCount(result.getValidRows().size());
        }
        return result;
    }

    /**
     * 4. XÁC NHẬN NẠP HÀNG LOẠT VÀO CSDL
     */
    @Transactional
    public int confirmImport(List<BrandExcelDto> rows) {
        if (rows == null || rows.isEmpty()) return 0;
        int count = 0;

        for (BrandExcelDto dto : rows) {
            int stCode = "TAM_DUNG".equalsIgnoreCase(dto.getStatus()) ? 1 : 0;
            if ("THEM_MOI".equalsIgnoreCase(dto.getAction())) {
                jdbc.update(
                        "INSERT INTO thuonghieu (tenthuonghieu, trangthai, ghichu) VALUES (?, ?, ?)",
                        dto.getName(),
                        stCode,
                        dto.getNote()
                );
                count++;
            } else if ("CAP_NHAT".equalsIgnoreCase(dto.getAction())) {
                jdbc.update(
                        "UPDATE thuonghieu SET trangthai = ?, ghichu = ? WHERE LOWER(TRIM(tenthuonghieu)) = LOWER(TRIM(?))",
                        stCode,
                        dto.getNote(),
                        dto.getName()
                );
                count++;
            }
        }
        return count;
    }

    private String safeStr(Object obj) {
        if (obj == null) return "";
        return ExcelSecurityValidator.sanitizeForFormulaInjection(obj.toString().trim());
    }
}
