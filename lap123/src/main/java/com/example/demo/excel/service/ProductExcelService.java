package com.example.demo.excel.service;

import com.example.demo.excel.core.ExcelCoreUtils;
import com.example.demo.excel.core.ExcelImportResult;
import com.example.demo.excel.core.ExcelSecurityValidator;
import com.example.demo.excel.dto.ProductExcelDto;
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
import java.math.BigDecimal;
import java.util.*;

@Service
public class ProductExcelService {

    private final JdbcTemplate jdbc;

    public ProductExcelService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 1. TẠO FILE MẪU EXCEL SẢN PHẨM LAPTOP
     */
    public byte[] generateTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Sheet 1: Mẫu nhập liệu
            Sheet sheet = workbook.createSheet("DANH_MUC_LAPTOP");

            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle instructionStyle = ExcelCoreUtils.createInstructionStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);
            CellStyle numberStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0");

            String[] headers = {
                    "STT",
                    "Tên dòng máy Laptop (*)",
                    "Thương hiệu (*)",
                    "Đơn vị tính",
                    "Xuất xứ",
                    "Vi xử lý (CPU)",
                    "Hệ điều hành",
                    "Kích thước màn hình",
                    "Giá bán niêm yết (VNĐ)",
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

            String[] instructions = {
                    "-",
                    "Tên đầy đủ của máy (VD: Laptop ASUS ZenBook 14 OLED)",
                    "Xem danh sách ở Sheet 'DANH_MUC_THUONG_HIEU'",
                    "Mặc định: Cái",
                    "Ví dụ: China, Vietnam, USA",
                    "Ví dụ: Intel Core i7 13700H, AMD Ryzen 7",
                    "Ví dụ: Windows 11 Home",
                    "Ví dụ: 14 inch, 15.6 inch, 16 inch",
                    "Số >= 0 (Ví dụ: 25990000)",
                    "THEM_MOI hoặc CAP_NHAT",
                    "Ghi chú cấu hình hoặc đặc điểm máy"
            };
            Row instRow = sheet.createRow(1);
            instRow.setHeightInPoints(22);
            for (int i = 0; i < instructions.length; i++) {
                Cell cell = instRow.createCell(i);
                cell.setCellValue(instructions[i]);
                cell.setCellStyle(instructionStyle);
            }

            Object[][] sampleData = {
                    {1, "Laptop ASUS ZenBook 14 OLED UX3405", "Asus", "Cái", "China", "Intel Core Ultra 7 155H", "Windows 11", "14 inch 3K OLED", 28500000, "THEM_MOI", "Máy mỏng nhẹ cao cấp"},
                    {2, "Laptop Lenovo Legion Pro 5 16IRX9", "Lenovo", "Cái", "China", "Intel Core i7 14700HX", "Windows 11", "16 inch WQXGA 240Hz", 39990000, "THEM_MOI", "Dòng gaming hiệu năng cao"}
            };

            for (int r = 0; r < sampleData.length; r++) {
                Row row = sheet.createRow(r + 2);
                row.setHeightInPoints(20);
                for (int c = 0; c < sampleData[r].length; c++) {
                    Cell cell = row.createCell(c);
                    Object val = sampleData[r][c];
                    if (val instanceof Number) {
                        cell.setCellValue(((Number) val).doubleValue());
                        cell.setCellStyle(numberStyle);
                    } else {
                        cell.setCellValue(val != null ? val.toString() : "");
                        cell.setCellStyle(dataStyle);
                    }
                }
            }

            ExcelCoreUtils.autoSizeColumns(sheet, headers.length);

            // Sheet 2: Danh sách Thương hiệu hợp lệ
            Sheet brandSheet = workbook.createSheet("DANH_MUC_THUONG_HIEU");
            Row bhRow = brandSheet.createRow(0);
            bhRow.setHeightInPoints(25);
            Cell bh0 = bhRow.createCell(0);
            bh0.setCellValue("Mã thương hiệu");
            bh0.setCellStyle(headerStyle);
            Cell bh1 = bhRow.createCell(1);
            bh1.setCellValue("Tên thương hiệu");
            bh1.setCellStyle(headerStyle);

            List<Map<String, Object>> brands = jdbc.queryForList(
                    "SELECT mathuonghieu, tenthuonghieu FROM thuonghieu ORDER BY mathuonghieu ASC"
            );
            int bRowIndex = 1;
            for (Map<String, Object> brand : brands) {
                Row row = brandSheet.createRow(bRowIndex++);
                Cell c0 = row.createCell(0);
                c0.setCellValue(((Number) brand.get("mathuonghieu")).intValue());
                c0.setCellStyle(dataStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue((String) brand.get("tenthuonghieu"));
                c1.setCellStyle(dataStyle);
            }
            ExcelCoreUtils.autoSizeColumns(brandSheet, 2);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 2. XUẤT DANH SÁCH LAPTOP RA EXCEL (CHIA NHIỀU BẢNG THEO THƯƠNG HIỆU)
     */
    public byte[] exportProducts(String keyword) throws IOException {
        return exportProducts(getAllProductsForExport(keyword));
    }

    public byte[] exportProducts(List<Map<String, Object>> products) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);
            CellStyle numberStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0");
            CellStyle currencyStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0 ₫");

            CellStyle bannerStyle = workbook.createCellStyle();
            Font bannerFont = workbook.createFont();
            bannerFont.setBold(true);
            bannerFont.setFontHeightInPoints((short) 11);
            bannerFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            bannerStyle.setFont(bannerFont);
            bannerStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            bannerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            bannerStyle.setAlignment(HorizontalAlignment.LEFT);
            bannerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            bannerStyle.setBorderTop(BorderStyle.MEDIUM);
            bannerStyle.setBorderBottom(BorderStyle.THIN);
            bannerStyle.setBorderLeft(BorderStyle.THIN);
            bannerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle subtotalStyle = workbook.createCellStyle();
            Font subtotalFont = workbook.createFont();
            subtotalFont.setBold(true);
            subtotalFont.setFontHeightInPoints((short) 10);
            subtotalStyle.setFont(subtotalFont);
            subtotalStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            subtotalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            subtotalStyle.setAlignment(HorizontalAlignment.RIGHT);
            subtotalStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            subtotalStyle.setBorderTop(BorderStyle.THIN);
            subtotalStyle.setBorderBottom(BorderStyle.MEDIUM);
            subtotalStyle.setBorderLeft(BorderStyle.THIN);
            subtotalStyle.setBorderRight(BorderStyle.THIN);

            CellStyle subtotalText = workbook.createCellStyle();
            subtotalText.cloneStyleFrom(subtotalStyle);
            subtotalText.setAlignment(HorizontalAlignment.LEFT);

            // ====================================================
            // SHEET 1: CHIA BẢNG THEO THƯƠNG HIỆU
            // ====================================================
            Sheet groupSheet = workbook.createSheet("THEO_THUONG_HIEU");

            Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
            for (Map<String, Object> p : products) {
                String brand = p.get("tenthuonghieu") != null ? p.get("tenthuonghieu").toString() : "Khác";
                groups.computeIfAbsent(brand, k -> new ArrayList<>()).add(p);
            }

            String[] tableHeaders = {
                    "STT",
                    "Mã máy",
                    "Tên dòng máy Laptop",
                    "Xuất xứ",
                    "Vi xử lý (CPU)",
                    "Hệ điều hành",
                    "Màn hình",
                    "Giá bán niêm yết",
                    "Tồn kho thành phẩm",
                    "Trạng thái",
                    "Ghi chú"
            };

            int curRow = 0;
            for (Map.Entry<String, List<Map<String, Object>>> entry : groups.entrySet()) {
                String brandName = entry.getKey();
                List<Map<String, Object>> items = entry.getValue();

                // 1. Banner
                Row bannerRow = groupSheet.createRow(curRow);
                bannerRow.setHeightInPoints(28);
                Cell bannerCell = bannerRow.createCell(0);
                bannerCell.setCellValue("💻 THƯƠNG HIỆU: " + brandName.toUpperCase() + " (" + items.size() + " dòng máy)");
                bannerCell.setCellStyle(bannerStyle);
                for (int c = 1; c < tableHeaders.length; c++) {
                    Cell ec = bannerRow.createCell(c);
                    ec.setCellStyle(bannerStyle);
                }
                groupSheet.addMergedRegion(new CellRangeAddress(curRow, curRow, 0, tableHeaders.length - 1));
                curRow++;

                // 2. Table Header
                Row subHeaderRow = groupSheet.createRow(curRow);
                subHeaderRow.setHeightInPoints(24);
                for (int c = 0; c < tableHeaders.length; c++) {
                    Cell cell = subHeaderRow.createCell(c);
                    cell.setCellValue(tableHeaders[c]);
                    cell.setCellStyle(headerStyle);
                }
                curRow++;

                // 3. Data rows
                int stt = 1;
                long totalStock = 0;
                for (Map<String, Object> p : items) {
                    Row dataRow = groupSheet.createRow(curRow);
                    dataRow.setHeightInPoints(20);

                    // STT
                    Cell c0 = dataRow.createCell(0);
                    c0.setCellValue(stt++);
                    c0.setCellStyle(dataStyle);

                    // Mã máy
                    Cell c1 = dataRow.createCell(1);
                    c1.setCellValue("SP-" + p.get("masp"));
                    c1.setCellStyle(dataStyle);

                    // Tên máy
                    Cell c2 = dataRow.createCell(2);
                    c2.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) p.get("tensp")));
                    c2.setCellStyle(dataStyle);

                    // Xuất xứ
                    Cell c3 = dataRow.createCell(3);
                    c3.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) p.get("xuatxu")));
                    c3.setCellStyle(dataStyle);

                    // CPU
                    Cell c4 = dataRow.createCell(4);
                    c4.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) p.get("chip")));
                    c4.setCellStyle(dataStyle);

                    // OS
                    Cell c5 = dataRow.createCell(5);
                    c5.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) p.get("hedieuhanh")));
                    c5.setCellStyle(dataStyle);

                    // Màn hình
                    Cell c6 = dataRow.createCell(6);
                    c6.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) p.get("kichthuocman")));
                    c6.setCellStyle(dataStyle);

                    // Giá bán
                    Cell c7 = dataRow.createCell(7);
                    BigDecimal price = p.get("giaban") != null ? (BigDecimal) p.get("giaban") : BigDecimal.ZERO;
                    c7.setCellValue(price.doubleValue());
                    c7.setCellStyle(currencyStyle);

                    // Tồn kho
                    Cell c8 = dataRow.createCell(8);
                    int stock = p.get("soluongton") != null ? ((Number) p.get("soluongton")).intValue() : 0;
                    c8.setCellValue(stock);
                    c8.setCellStyle(numberStyle);
                    totalStock += stock;

                    // Trạng thái
                    Cell c9 = dataRow.createCell(9);
                    int status = p.get("trangthai") != null ? ((Number) p.get("trangthai")).intValue() : 0;
                    c9.setCellValue(status == 0 ? "Đang bán" : "Ngừng bán");
                    c9.setCellStyle(dataStyle);

                    // Ghi chú
                    Cell c10 = dataRow.createCell(10);
                    c10.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) p.get("ghichu")));
                    c10.setCellStyle(dataStyle);

                    curRow++;
                }

                // 4. Subtotal row
                Row subtotalRow = groupSheet.createRow(curRow);
                subtotalRow.setHeightInPoints(22);
                Cell st0 = subtotalRow.createCell(0);
                st0.setCellValue("Tổng tồn kho hãng " + brandName + ":");
                st0.setCellStyle(subtotalText);
                for (int c = 1; c < 8; c++) {
                    Cell sc = subtotalRow.createCell(c);
                    sc.setCellStyle(subtotalStyle);
                }
                groupSheet.addMergedRegion(new CellRangeAddress(curRow, curRow, 0, 7));

                Cell stStock = subtotalRow.createCell(8);
                stStock.setCellValue(totalStock);
                stStock.setCellStyle(subtotalStyle);

                for (int c = 9; c < tableHeaders.length; c++) {
                    Cell sc = subtotalRow.createCell(c);
                    sc.setCellStyle(subtotalStyle);
                }
                curRow++;

                // 5. Spacer
                curRow++;
            }
            ExcelCoreUtils.autoSizeColumns(groupSheet, tableHeaders.length);

            // ====================================================
            // SHEET 2: TOÀN BỘ SẢN PHẨM DẠNG BẢNG PHẲNG
            // ====================================================
            Sheet flatSheet = workbook.createSheet("TOAN_BO_SAN_PHAM");
            String[] flatHeaders = {
                    "STT",
                    "Mã máy",
                    "Tên dòng máy Laptop",
                    "Thương hiệu",
                    "Đơn vị tính",
                    "Xuất xứ",
                    "Vi xử lý (CPU)",
                    "Hệ điều hành",
                    "Màn hình",
                    "Giá bán niêm yết",
                    "Tồn kho thành phẩm",
                    "Trạng thái",
                    "Ghi chú"
            };

            Row fhRow = flatSheet.createRow(0);
            fhRow.setHeightInPoints(28);
            for (int i = 0; i < flatHeaders.length; i++) {
                Cell cell = fhRow.createCell(i);
                cell.setCellValue(flatHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            int fIdx = 1;
            for (Map<String, Object> p : products) {
                Row row = flatSheet.createRow(fIdx);
                row.setHeightInPoints(20);

                Cell c0 = row.createCell(0);
                c0.setCellValue(fIdx);
                c0.setCellStyle(dataStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue("SP-" + p.get("masp"));
                c1.setCellStyle(dataStyle);

                Cell c2 = row.createCell(2);
                c2.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) p.get("tensp")));
                c2.setCellStyle(dataStyle);

                Cell c3 = row.createCell(3);
                c3.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) p.get("tenthuonghieu")));
                c3.setCellStyle(dataStyle);

                Cell c4 = row.createCell(4);
                c4.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) p.get("donvitinh")));
                c4.setCellStyle(dataStyle);

                Cell c5 = row.createCell(5);
                c5.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) p.get("xuatxu")));
                c5.setCellStyle(dataStyle);

                Cell c6 = row.createCell(6);
                c6.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) p.get("chip")));
                c6.setCellStyle(dataStyle);

                Cell c7 = row.createCell(7);
                c7.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) p.get("hedieuhanh")));
                c7.setCellStyle(dataStyle);

                Cell c8 = row.createCell(8);
                c8.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) p.get("kichthuocman")));
                c8.setCellStyle(dataStyle);

                Cell c9 = row.createCell(9);
                BigDecimal price = p.get("giaban") != null ? (BigDecimal) p.get("giaban") : BigDecimal.ZERO;
                c9.setCellValue(price.doubleValue());
                c9.setCellStyle(currencyStyle);

                Cell c10 = row.createCell(10);
                int stock = p.get("soluongton") != null ? ((Number) p.get("soluongton")).intValue() : 0;
                c10.setCellValue(stock);
                c10.setCellStyle(numberStyle);

                Cell c11 = row.createCell(11);
                int status = p.get("trangthai") != null ? ((Number) p.get("trangthai")).intValue() : 0;
                c11.setCellValue(status == 0 ? "Đang bán" : "Ngừng bán");
                c11.setCellStyle(dataStyle);

                Cell c12 = row.createCell(12);
                c12.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) p.get("ghichu")));
                c12.setCellStyle(dataStyle);

                fIdx++;
            }
            ExcelCoreUtils.autoSizeColumns(flatSheet, flatHeaders.length);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 3. KIỂM TRA DỮ LIỆU & XEM TRƯỚC SẢN PHẨM LAPTOP
     */
    public ExcelImportResult<ProductExcelDto> validateAndPreview(InputStream is, long fileSize) throws IOException {
        ExcelSecurityValidator.validateFileSize(fileSize);

        byte[] fileBytes = is.readAllBytes();
        ExcelSecurityValidator.validateXlsxMagicBytes(new ByteArrayInputStream(fileBytes));

        ExcelImportResult<ProductExcelDto> result = new ExcelImportResult<>();

        // Tải danh bạ thương hiệu
        List<Map<String, Object>> brandList = jdbc.queryForList(
                "SELECT mathuonghieu, LOWER(TRIM(tenthuonghieu)) AS normalized_name, tenthuonghieu FROM thuonghieu"
        );
        Map<String, Integer> brandMap = new HashMap<>();
        for (Map<String, Object> b : brandList) {
            brandMap.put((String) b.get("normalized_name"), ((Number) b.get("mathuonghieu")).intValue());
        }

        // Tải danh sách tên sản phẩm hiện có trong DB
        List<String> existingNames = jdbc.queryForList(
                "SELECT LOWER(TRIM(tensp)) FROM sanpham",
                String.class
        );
        Set<String> existingNameSet = new HashSet<>(existingNames);
        Set<String> fileNames = new HashSet<>();

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileBytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                result.addError(0, "Tệp", "", "Không tìm thấy Sheet dữ liệu trong tệp Excel.");
                return result;
            }

            int lastRow = sheet.getLastRowNum();
            if (lastRow < 2) {
                result.addError(0, "Tệp", "", "Tệp Excel không chứa dòng dữ liệu nào (vui lòng điền từ dòng 3 trở đi).");
                return result;
            }

            int totalRows = 0;
            for (int r = 2; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }

                String name = ExcelCoreUtils.getCellStringValue(row.getCell(1));
                String brandName = ExcelCoreUtils.getCellStringValue(row.getCell(2));

                if (name.isEmpty() && brandName.isEmpty()) {
                    continue;
                }

                totalRows++;
                int displayRowNumber = r + 1;
                boolean rowHasError = false;

                // 1. Kiểm tra Tên máy Laptop
                if (name.isEmpty()) {
                    result.addError(displayRowNumber, "Tên dòng máy Laptop", "", "Tên máy Laptop không được để trống.");
                    rowHasError = true;
                } else if (name.length() > 255) {
                    result.addError(displayRowNumber, "Tên dòng máy Laptop", name, "Tên máy Laptop vượt quá 255 ký tự.");
                    rowHasError = true;
                } else {
                    String lowerName = name.toLowerCase();
                    if (fileNames.contains(lowerName)) {
                        result.addError(displayRowNumber, "Tên dòng máy Laptop", name, "Tên dòng máy bị trùng lặp với dòng khác trong cùng tệp Excel.");
                        rowHasError = true;
                    } else {
                        fileNames.add(lowerName);
                    }
                }

                // 2. Kiểm tra Thương hiệu
                Integer brandId = null;
                if (brandName.isEmpty()) {
                    result.addError(displayRowNumber, "Thương hiệu", "", "Thương hiệu không được để trống.");
                    rowHasError = true;
                } else {
                    brandId = brandMap.get(brandName.toLowerCase());
                    if (brandId == null) {
                        result.addError(displayRowNumber, "Thương hiệu", brandName,
                                "Không tìm thấy thương hiệu này. Vui lòng xem danh mục hợp lệ ở Sheet 'DANH_MUC_THUONG_HIEU'.");
                        rowHasError = true;
                    }
                }

                // 3. Đơn vị tính
                String unit = ExcelCoreUtils.getCellStringValue(row.getCell(3));
                if (unit.isEmpty()) {
                    unit = "Cái";
                }

                // 4. Các thông số cấu hình
                String origin = ExcelCoreUtils.getCellStringValue(row.getCell(4));
                String cpu = ExcelCoreUtils.getCellStringValue(row.getCell(5));
                String os = ExcelCoreUtils.getCellStringValue(row.getCell(6));
                if (os.isEmpty()) {
                    os = "Windows";
                }
                String screenSize = ExcelCoreUtils.getCellStringValue(row.getCell(7));

                // 5. Giá bán
                BigDecimal price = ExcelCoreUtils.getCellBigDecimalValue(row.getCell(8));
                if (price == null) {
                    result.addError(displayRowNumber, "Giá bán niêm yết", ExcelCoreUtils.getCellStringValue(row.getCell(8)), "Giá bán không đúng định dạng số.");
                    rowHasError = true;
                } else if (price.compareTo(BigDecimal.ZERO) < 0) {
                    result.addError(displayRowNumber, "Giá bán niêm yết", price.toString(), "Giá bán không được âm.");
                    rowHasError = true;
                }

                // 6. Hành động
                String action = ExcelCoreUtils.getCellStringValue(row.getCell(9)).toUpperCase();
                if (action.isEmpty() || (!action.equals("THEM_MOI") && !action.equals("CAP_NHAT"))) {
                    action = "THEM_MOI";
                }

                // Đối soát DB
                if (!name.isEmpty()) {
                    boolean exists = existingNameSet.contains(name.toLowerCase());
                    if ("THEM_MOI".equals(action) && exists) {
                        result.addError(displayRowNumber, "Hành động", action,
                                "Dòng máy [" + name + "] đã tồn tại trong hệ thống. Vui lòng chọn CAP_NHAT nếu muốn cập nhật thông số.");
                        rowHasError = true;
                    } else if ("CAP_NHAT".equals(action) && !exists) {
                        result.addError(displayRowNumber, "Hành động", action,
                                "Dòng máy [" + name + "] chưa tồn tại trong hệ thống để cập nhật. Vui lòng chọn THEM_MOI.");
                        rowHasError = true;
                    }
                }

                String note = ExcelCoreUtils.getCellStringValue(row.getCell(10));

                if (!rowHasError) {
                    ProductExcelDto dto = new ProductExcelDto();
                    dto.setRowNumber(displayRowNumber);
                    dto.setName(name);
                    dto.setBrandName(brandName);
                    dto.setBrandId(brandId);
                    dto.setUnit(unit);
                    dto.setOrigin(origin);
                    dto.setCpu(cpu);
                    dto.setOs(os);
                    dto.setScreenSize(screenSize);
                    dto.setPrice(price != null ? price : BigDecimal.ZERO);
                    dto.setAction(action);
                    dto.setNote(note);
                    result.getValidRows().add(dto);
                }
            }

            result.setTotalRows(totalRows);
            result.setValidCount(result.getValidRows().size());
            result.setErrorCount(result.getErrors().size());
        }

        return result;
    }

    /**
     * 4. LƯU DỮ LIỆU SẢN PHẨM ĐÃ HỢP LỆ THEO TRANSACTION
     * KHÔNG tác động lên cột tồn kho soluongton để đảm bảo an toàn số dư kho.
     */
    @Transactional(rollbackFor = Exception.class)
    public int confirmImport(List<ProductExcelDto> validRows) {
        if (validRows == null || validRows.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (ProductExcelDto dto : validRows) {
            if ("THEM_MOI".equalsIgnoreCase(dto.getAction())) {
                jdbc.update(
                        """
                        INSERT INTO sanpham
                        (
                            tensp,
                            mathuonghieu,
                            donvitinh,
                            xuatxu,
                            chip,
                            hedieuhanh,
                            kichthuocman,
                            giaban,
                            soluongton,
                            trangthai,
                            ghichu
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, 0, ?)
                        """,
                        dto.getName(),
                        dto.getBrandId(),
                        dto.getUnit(),
                        dto.getOrigin(),
                        dto.getCpu(),
                        dto.getOs(),
                        dto.getScreenSize(),
                        dto.getPrice(),
                        dto.getNote()
                );
                count++;
            } else if ("CAP_NHAT".equalsIgnoreCase(dto.getAction())) {
                jdbc.update(
                        """
                        UPDATE sanpham
                        SET
                            mathuonghieu = ?,
                            donvitinh = ?,
                            xuatxu = ?,
                            chip = ?,
                            hedieuhanh = ?,
                            kichthuocman = ?,
                            giaban = ?,
                            ghichu = ?
                        WHERE LOWER(tensp) = LOWER(?)
                        """,
                        dto.getBrandId(),
                        dto.getUnit(),
                        dto.getOrigin(),
                        dto.getCpu(),
                        dto.getOs(),
                        dto.getScreenSize(),
                        dto.getPrice(),
                        dto.getNote(),
                        dto.getName()
                );
                count++;
            }
        }
        return count;
    }

    /**
     * Lấy toàn bộ danh sách sản phẩm phục vụ xuất Excel
     */
    public List<Map<String, Object>> getAllProductsForExport(String keyword) {
        String sql = """
            SELECT
                s.*,
                t.tenthuonghieu
            FROM sanpham s
            LEFT JOIN thuonghieu t ON t.mathuonghieu = s.mathuonghieu
            WHERE s.tensp LIKE ? OR t.tenthuonghieu LIKE ?
            ORDER BY t.tenthuonghieu ASC, s.tensp ASC
        """;
        String kw = "%" + (keyword != null ? keyword : "") + "%";
        return jdbc.queryForList(sql, kw, kw);
    }
}
