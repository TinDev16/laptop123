package com.example.demo.excel.service;

import com.example.demo.excel.core.ExcelCoreUtils;
import com.example.demo.excel.core.ExcelImportResult;
import com.example.demo.excel.core.ExcelSecurityValidator;
import com.example.demo.excel.dto.MaterialExcelDto;
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
public class MaterialExcelService {

    private final JdbcTemplate jdbc;

    public MaterialExcelService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 1. TẠO FILE MẪU EXCEL CHUẨN ĐỂ NGƯỜI DÙNG TẢI VỀ
     */
    public byte[] generateTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Sheet 1: Mẫu nhập liệu
            Sheet sheet = workbook.createSheet("DANH_MUC_NVL");

            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle instructionStyle = ExcelCoreUtils.createInstructionStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);
            CellStyle numberStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0");

            String[] headers = {
                    "STT",
                    "Mã linh kiện (*)",
                    "Tên nguyên vật liệu (*)",
                    "Tên loại linh kiện (*)",
                    "Đơn vị tính",
                    "Đơn giá nhập (VNĐ)",
                    "Tồn tối thiểu",
                    "Thời gian đặt hàng (ngày)",
                    "Hành động (THEM_MOI / CAP_NHAT)",
                    "Ghi chú"
            };

            // Dòng 0: Header
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(28);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Dòng 1: Hướng dẫn
            String[] instructions = {
                    "-",
                    "Duy nhất, tối đa 30 ký tự (VD: NVL-RAM-005)",
                    "Tên đầy đủ của linh kiện",
                    "Xem danh sách ở Sheet 'DANH_MUC_LOAI_NVL'",
                    "Mặc định: Cái (Bộ, Chiếc...)",
                    "Số >= 0 (Ví dụ: 850000)",
                    "Số >= 0 (Ví dụ: 5)",
                    "Số ngày từ khi đặt đến khi giao (VD: 3)",
                    "THEM_MOI (nếu thêm mới) hoặc CAP_NHAT (nếu sửa)",
                    "Thông tin bổ sung (nếu có)"
            };
            Row instRow = sheet.createRow(1);
            instRow.setHeightInPoints(22);
            for (int i = 0; i < instructions.length; i++) {
                Cell cell = instRow.createCell(i);
                cell.setCellValue(instructions[i]);
                cell.setCellStyle(instructionStyle);
            }

            // Dòng 2, 3: Dữ liệu mẫu minh họa
            Object[][] sampleData = {
                    {1, "NVL-RAM-005", "RAM DDR5 16GB 4800MHz", "RAM", "Cái", 850000, 10, 3, "THEM_MOI", "Linh kiện mẫu"},
                    {2, "NVL-CPU-005", "CPU Intel Core i7 13700H", "CPU", "Cái", 4500000, 5, 5, "THEM_MOI", "Linh kiện mẫu"}
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

            // Sheet 2: Danh sách loại linh kiện hợp lệ đang có trong hệ thống
            Sheet typeSheet = workbook.createSheet("DANH_MUC_LOAI_NVL");
            Row typeHeaderRow = typeSheet.createRow(0);
            typeHeaderRow.setHeightInPoints(25);
            Cell th0 = typeHeaderRow.createCell(0);
            th0.setCellValue("Mã loại");
            th0.setCellStyle(headerStyle);
            Cell th1 = typeHeaderRow.createCell(1);
            th1.setCellValue("Tên loại nguyên vật liệu");
            th1.setCellStyle(headerStyle);

            List<Map<String, Object>> types = jdbc.queryForList(
                    "SELECT maloai, tenloai FROM loai_nguyen_vat_lieu WHERE trangthai = 1 ORDER BY maloai ASC"
            );
            int typeRowIndex = 1;
            for (Map<String, Object> type : types) {
                Row row = typeSheet.createRow(typeRowIndex++);
                Cell c0 = row.createCell(0);
                c0.setCellValue(((Number) type.get("maloai")).intValue());
                c0.setCellStyle(dataStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue((String) type.get("tenloai"));
                c1.setCellStyle(dataStyle);
            }
            ExcelCoreUtils.autoSizeColumns(typeSheet, 2);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 2. XUẤT DANH MỤC NGUYÊN VẬT LIỆU RA EXCEL
     * Hỗ trợ chia thành nhiều bảng riêng biệt theo từng Loại linh kiện (Sheet 1)
     * và bảng phẳng toàn bộ danh sách (Sheet 2).
     */
    public byte[] exportMaterials(List<Map<String, Object>> materials) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);
            CellStyle numberStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0");
            CellStyle currencyStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0 ₫");

            // Category Banner Style (Header khối từng loại linh kiện)
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

            // Subtotal Style (Dòng tổng cộng của từng loại)
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
            // SHEET 1: CHIA THÀNH NHIỀU BẢNG THEO TỪNG LOẠI LINH KIỆN
            // ====================================================
            Sheet groupSheet = workbook.createSheet("THEO_LOAI_LINH_KIEN");

            // Gom nhóm theo loại linh kiện (tương tự giao diện web)
            Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
            for (Map<String, Object> mat : materials) {
                String typeName = mat.get("tenloai") != null ? mat.get("tenloai").toString() : "Chưa phân loại";
                groups.computeIfAbsent(typeName, k -> new ArrayList<>()).add(mat);
            }

            String[] groupHeaders = {
                    "STT",
                    "Mã linh kiện",
                    "Tên nguyên vật liệu",
                    "Đơn vị tính",
                    "Đơn giá nhập",
                    "Tồn kho trung tâm",
                    "Tồn tối thiểu",
                    "Thời gian giao (ngày)",
                    "Trạng thái",
                    "Ghi chú"
            };

            int curRow = 0;

            for (Map.Entry<String, List<Map<String, Object>>> entry : groups.entrySet()) {
                String categoryName = entry.getKey();
                List<Map<String, Object>> items = entry.getValue();

                // 1. Dòng Banner Loại linh kiện (Bắt đầu 1 bảng mới)
                Row bannerRow = groupSheet.createRow(curRow);
                bannerRow.setHeightInPoints(28);
                Cell bannerCell = bannerRow.createCell(0);
                bannerCell.setCellValue("📁 NHÓM LINH KIỆN: " + categoryName.toUpperCase() + " (" + items.size() + " linh kiện)");
                bannerCell.setCellStyle(bannerStyle);
                for (int c = 1; c < groupHeaders.length; c++) {
                    Cell emptyCell = bannerRow.createCell(c);
                    emptyCell.setCellStyle(bannerStyle);
                }
                groupSheet.addMergedRegion(new CellRangeAddress(curRow, curRow, 0, groupHeaders.length - 1));
                curRow++;

                // 2. Dòng Tiêu đề bảng con (Table Header)
                Row subHeaderRow = groupSheet.createRow(curRow);
                subHeaderRow.setHeightInPoints(24);
                for (int c = 0; c < groupHeaders.length; c++) {
                    Cell cell = subHeaderRow.createCell(c);
                    cell.setCellValue(groupHeaders[c]);
                    cell.setCellStyle(headerStyle);
                }
                curRow++;

                // 3. Các dòng dữ liệu linh kiện của nhóm đó
                int stt = 1;
                double groupTotalStock = 0;
                for (Map<String, Object> mat : items) {
                    Row dataRow = groupSheet.createRow(curRow);
                    dataRow.setHeightInPoints(20);

                    // STT
                    Cell c0 = dataRow.createCell(0);
                    c0.setCellValue(stt++);
                    c0.setCellStyle(dataStyle);

                    // Mã NVL
                    Cell c1 = dataRow.createCell(1);
                    c1.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) mat.get("manvl_code")));
                    c1.setCellStyle(dataStyle);

                    // Tên NVL
                    Cell c2 = dataRow.createCell(2);
                    c2.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) mat.get("tennvl")));
                    c2.setCellStyle(dataStyle);

                    // Đơn vị tính
                    Cell c3 = dataRow.createCell(3);
                    c3.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) mat.get("donvitinh")));
                    c3.setCellStyle(dataStyle);

                    // Đơn giá nhập
                    Cell c4 = dataRow.createCell(4);
                    BigDecimal price = mat.get("dongianhap") != null ? (BigDecimal) mat.get("dongianhap") : BigDecimal.ZERO;
                    c4.setCellValue(price.doubleValue());
                    c4.setCellStyle(currencyStyle);

                    // Tồn kho
                    Cell c5 = dataRow.createCell(5);
                    BigDecimal stock = mat.get("soluongton") != null ? (BigDecimal) mat.get("soluongton") : BigDecimal.ZERO;
                    c5.setCellValue(stock.doubleValue());
                    c5.setCellStyle(numberStyle);
                    groupTotalStock += stock.doubleValue();

                    // Tồn tối thiểu
                    Cell c6 = dataRow.createCell(6);
                    BigDecimal minStock = mat.get("ton_toithieu") != null ? (BigDecimal) mat.get("ton_toithieu") : BigDecimal.ZERO;
                    c6.setCellValue(minStock.doubleValue());
                    c6.setCellStyle(numberStyle);

                    // Lead time
                    Cell c7 = dataRow.createCell(7);
                    int lead = mat.get("lead_time_ngay") != null ? ((Number) mat.get("lead_time_ngay")).intValue() : 0;
                    c7.setCellValue(lead);
                    c7.setCellStyle(numberStyle);

                    // Trạng thái
                    Cell c8 = dataRow.createCell(8);
                    int status = mat.get("trangthai") != null ? ((Number) mat.get("trangthai")).intValue() : 1;
                    c8.setCellValue(status == 1 ? "Hoạt động" : "Ngừng sử dụng");
                    c8.setCellStyle(dataStyle);

                    // Ghi chú
                    Cell c9 = dataRow.createCell(9);
                    c9.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) mat.get("ghichu")));
                    c9.setCellStyle(dataStyle);

                    curRow++;
                }

                // 4. Dòng Tổng cộng của nhóm (Subtotal)
                Row subtotalRow = groupSheet.createRow(curRow);
                subtotalRow.setHeightInPoints(22);
                Cell st0 = subtotalRow.createCell(0);
                st0.setCellValue("Tổng cộng tồn kho nhóm " + categoryName + ":");
                st0.setCellStyle(subtotalText);
                for (int c = 1; c < 5; c++) {
                    Cell sc = subtotalRow.createCell(c);
                    sc.setCellStyle(subtotalStyle);
                }
                groupSheet.addMergedRegion(new CellRangeAddress(curRow, curRow, 0, 4));

                Cell stStock = subtotalRow.createCell(5);
                stStock.setCellValue(groupTotalStock);
                stStock.setCellStyle(subtotalStyle);

                for (int c = 6; c < groupHeaders.length; c++) {
                    Cell sc = subtotalRow.createCell(c);
                    sc.setCellStyle(subtotalStyle);
                }
                curRow++;

                // 5. Dòng trống ngăn cách giữa các bảng
                curRow++;
            }
            ExcelCoreUtils.autoSizeColumns(groupSheet, groupHeaders.length);

            // ====================================================
            // SHEET 2: TOÀN BỘ DANH MỤC DẠNG BẢNG PHẲNG (DỄ LỌC / PIVOT)
            // ====================================================
            Sheet flatSheet = workbook.createSheet("TOAN_BO_DANH_SACH");
            String[] flatHeaders = {
                    "STT",
                    "Mã linh kiện",
                    "Tên nguyên vật liệu",
                    "Loại linh kiện",
                    "Đơn vị tính",
                    "Đơn giá nhập",
                    "Tồn kho trung tâm",
                    "Tồn tối thiểu",
                    "Thời gian giao hàng (ngày)",
                    "Trạng thái",
                    "Ghi chú"
            };

            Row flatHeaderRow = flatSheet.createRow(0);
            flatHeaderRow.setHeightInPoints(28);
            for (int i = 0; i < flatHeaders.length; i++) {
                Cell cell = flatHeaderRow.createCell(i);
                cell.setCellValue(flatHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            int flatRowIndex = 1;
            for (Map<String, Object> mat : materials) {
                Row row = flatSheet.createRow(flatRowIndex);
                row.setHeightInPoints(20);

                Cell c0 = row.createCell(0);
                c0.setCellValue(flatRowIndex);
                c0.setCellStyle(dataStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) mat.get("manvl_code")));
                c1.setCellStyle(dataStyle);

                Cell c2 = row.createCell(2);
                c2.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) mat.get("tennvl")));
                c2.setCellStyle(dataStyle);

                Cell c3 = row.createCell(3);
                c3.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) mat.get("tenloai")));
                c3.setCellStyle(dataStyle);

                Cell c4 = row.createCell(4);
                c4.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) mat.get("donvitinh")));
                c4.setCellStyle(dataStyle);

                Cell c5 = row.createCell(5);
                BigDecimal price = mat.get("dongianhap") != null ? (BigDecimal) mat.get("dongianhap") : BigDecimal.ZERO;
                c5.setCellValue(price.doubleValue());
                c5.setCellStyle(currencyStyle);

                Cell c6 = row.createCell(6);
                BigDecimal stock = mat.get("soluongton") != null ? (BigDecimal) mat.get("soluongton") : BigDecimal.ZERO;
                c6.setCellValue(stock.doubleValue());
                c6.setCellStyle(numberStyle);

                Cell c7 = row.createCell(7);
                BigDecimal minStock = mat.get("ton_toithieu") != null ? (BigDecimal) mat.get("ton_toithieu") : BigDecimal.ZERO;
                c7.setCellValue(minStock.doubleValue());
                c7.setCellStyle(numberStyle);

                Cell c8 = row.createCell(8);
                int lead = mat.get("lead_time_ngay") != null ? ((Number) mat.get("lead_time_ngay")).intValue() : 0;
                c8.setCellValue(lead);
                c8.setCellStyle(numberStyle);

                Cell c9 = row.createCell(9);
                int status = mat.get("trangthai") != null ? ((Number) mat.get("trangthai")).intValue() : 1;
                c9.setCellValue(status == 1 ? "Hoạt động" : "Ngừng sử dụng");
                c9.setCellStyle(dataStyle);

                Cell c10 = row.createCell(10);
                c10.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) mat.get("ghichu")));
                c10.setCellStyle(dataStyle);

                flatRowIndex++;
            }
            ExcelCoreUtils.autoSizeColumns(flatSheet, flatHeaders.length);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 3. KIỂM TRA DỮ LIỆU & XEM TRƯỚC (PREVIEW & VALIDATION)
     */
    public ExcelImportResult<MaterialExcelDto> validateAndPreview(InputStream is, long fileSize) throws IOException {
        ExcelSecurityValidator.validateFileSize(fileSize);

        byte[] fileBytes = is.readAllBytes();
        ExcelSecurityValidator.validateXlsxMagicBytes(new ByteArrayInputStream(fileBytes));

        ExcelImportResult<MaterialExcelDto> result = new ExcelImportResult<>();

        // Tải danh mục loại NVL từ DB để map
        List<Map<String, Object>> categoryList = jdbc.queryForList(
                "SELECT maloai, LOWER(TRIM(tenloai)) AS normalized_name, tenloai FROM loai_nguyen_vat_lieu WHERE trangthai = 1"
        );
        Map<String, Integer> categoryMap = new HashMap<>();
        for (Map<String, Object> cat : categoryList) {
            categoryMap.put((String) cat.get("normalized_name"), ((Number) cat.get("maloai")).intValue());
        }

        // Tải danh sách mã NVL hiện có trong DB để check duplicate/update
        List<String> existingCodes = jdbc.queryForList(
                "SELECT LOWER(TRIM(manvl_code)) FROM nguyenvatlieu",
                String.class
        );
        Set<String> existingCodeSet = new HashSet<>(existingCodes);

        Set<String> fileCodes = new HashSet<>();

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
            // Dữ liệu bắt đầu từ dòng index 2 (tức Dòng số 3 trên Excel)
            for (int r = 2; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }

                String code = ExcelCoreUtils.getCellStringValue(row.getCell(1));
                String name = ExcelCoreUtils.getCellStringValue(row.getCell(2));
                String categoryName = ExcelCoreUtils.getCellStringValue(row.getCell(3));

                // Nếu dòng hoàn toàn trống thì bỏ qua
                if (code.isEmpty() && name.isEmpty() && categoryName.isEmpty()) {
                    continue;
                }

                totalRows++;
                int displayRowNumber = r + 1; // 1-indexed cho người dùng
                boolean rowHasError = false;

                // 1. Kiểm tra Mã linh kiện
                if (code.isEmpty()) {
                    result.addError(displayRowNumber, "Mã linh kiện", "", "Mã linh kiện không được để trống.");
                    rowHasError = true;
                } else if (code.length() > 30) {
                    result.addError(displayRowNumber, "Mã linh kiện", code, "Mã linh kiện vượt quá 30 ký tự cho phép.");
                    rowHasError = true;
                } else {
                    String lowerCode = code.toLowerCase();
                    if (fileCodes.contains(lowerCode)) {
                        result.addError(displayRowNumber, "Mã linh kiện", code, "Mã linh kiện bị trùng lặp với dòng khác trong cùng tệp Excel.");
                        rowHasError = true;
                    } else {
                        fileCodes.add(lowerCode);
                    }
                }

                // 2. Kiểm tra Tên linh kiện
                if (name.isEmpty()) {
                    result.addError(displayRowNumber, "Tên nguyên vật liệu", "", "Tên nguyên vật liệu không được để trống.");
                    rowHasError = true;
                } else if (name.length() > 255) {
                    result.addError(displayRowNumber, "Tên nguyên vật liệu", name, "Tên nguyên vật liệu vượt quá 255 ký tự.");
                    rowHasError = true;
                }

                // 3. Kiểm tra Loại linh kiện
                Integer categoryId = null;
                if (categoryName.isEmpty()) {
                    result.addError(displayRowNumber, "Tên loại linh kiện", "", "Loại linh kiện không được để trống.");
                    rowHasError = true;
                } else {
                    categoryId = categoryMap.get(categoryName.toLowerCase());
                    if (categoryId == null) {
                        result.addError(displayRowNumber, "Tên loại linh kiện", categoryName,
                                "Không tìm thấy loại linh kiện này trong hệ thống. Vui lòng xem danh mục hợp lệ ở Sheet 'DANH_MUC_LOAI_NVL'.");
                        rowHasError = true;
                    }
                }

                // 4. Đơn vị tính
                String unit = ExcelCoreUtils.getCellStringValue(row.getCell(4));
                if (unit.isEmpty()) {
                    unit = "Cái";
                }

                // 5. Đơn giá nhập
                BigDecimal price = ExcelCoreUtils.getCellBigDecimalValue(row.getCell(5));
                if (price == null) {
                    result.addError(displayRowNumber, "Đơn giá nhập", ExcelCoreUtils.getCellStringValue(row.getCell(5)), "Đơn giá nhập không đúng định dạng số.");
                    rowHasError = true;
                } else if (price.compareTo(BigDecimal.ZERO) < 0) {
                    result.addError(displayRowNumber, "Đơn giá nhập", price.toString(), "Đơn giá nhập không được âm.");
                    rowHasError = true;
                }

                // 6. Tồn tối thiểu
                BigDecimal minStock = ExcelCoreUtils.getCellBigDecimalValue(row.getCell(6));
                if (minStock == null) {
                    minStock = BigDecimal.ZERO;
                } else if (minStock.compareTo(BigDecimal.ZERO) < 0) {
                    result.addError(displayRowNumber, "Tồn tối thiểu", minStock.toString(), "Tồn tối thiểu không được âm.");
                    rowHasError = true;
                }

                // 7. Lead time
                BigDecimal leadNum = ExcelCoreUtils.getCellBigDecimalValue(row.getCell(7));
                int leadTime = 0;
                if (leadNum != null) {
                    leadTime = leadNum.intValue();
                    if (leadTime < 0) {
                        result.addError(displayRowNumber, "Thời gian đặt hàng", String.valueOf(leadTime), "Thời gian đặt hàng không được âm.");
                        rowHasError = true;
                    }
                }

                // 8. Hành động (THEM_MOI / CAP_NHAT)
                String action = ExcelCoreUtils.getCellStringValue(row.getCell(8)).toUpperCase();
                if (action.isEmpty() || (!action.equals("THEM_MOI") && !action.equals("CAP_NHAT"))) {
                    action = "THEM_MOI";
                }

                // Đối soát với DB
                if (!code.isEmpty()) {
                    boolean existsInDb = existingCodeSet.contains(code.toLowerCase());
                    if ("THEM_MOI".equals(action) && existsInDb) {
                        result.addError(displayRowNumber, "Hành động", action,
                                "Mã linh kiện [" + code + "] đã tồn tại trong hệ thống. Vui lòng chọn hành động CAP_NHAT nếu muốn cập nhật thông tin.");
                        rowHasError = true;
                    } else if ("CAP_NHAT".equals(action) && !existsInDb) {
                        result.addError(displayRowNumber, "Hành động", action,
                                "Mã linh kiện [" + code + "] chưa tồn tại trong hệ thống để cập nhật. Vui lòng chọn THEM_MOI.");
                        rowHasError = true;
                    }
                }

                String note = ExcelCoreUtils.getCellStringValue(row.getCell(9));

                if (!rowHasError) {
                    MaterialExcelDto dto = new MaterialExcelDto();
                    dto.setRowNumber(displayRowNumber);
                    dto.setCode(code);
                    dto.setName(name);
                    dto.setCategoryName(categoryName);
                    dto.setCategoryId(categoryId);
                    dto.setUnit(unit);
                    dto.setImportPrice(price != null ? price : BigDecimal.ZERO);
                    dto.setMinStock(minStock);
                    dto.setLeadTimeDays(leadTime);
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
     * 4. LƯU DỮ LIỆU ĐÃ HỢP LỆ THEO TRANSACTION (ALL-OR-NOTHING)
     * KHÔNG tác động lên cột tồn kho soluongton để đảm bảo an toàn số dư kho.
     */
    @Transactional(rollbackFor = Exception.class)
    public int confirmImport(List<MaterialExcelDto> validRows) {
        if (validRows == null || validRows.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (MaterialExcelDto dto : validRows) {
            if ("THEM_MOI".equalsIgnoreCase(dto.getAction())) {
                jdbc.update(
                        """
                        INSERT INTO nguyenvatlieu
                        (
                            manvl_code,
                            tennvl,
                            maloai,
                            donvitinh,
                            dongianhap,
                            soluongton,
                            ton_toithieu,
                            lead_time_ngay,
                            trangthai,
                            ghichu
                        )
                        VALUES (?, ?, ?, ?, ?, 0.00, ?, ?, 1, ?)
                        """,
                        dto.getCode(),
                        dto.getName(),
                        dto.getCategoryId(),
                        dto.getUnit(),
                        dto.getImportPrice(),
                        dto.getMinStock(),
                        dto.getLeadTimeDays(),
                        dto.getNote()
                );
                count++;
            } else if ("CAP_NHAT".equalsIgnoreCase(dto.getAction())) {
                jdbc.update(
                        """
                        UPDATE nguyenvatlieu
                        SET
                            tennvl = ?,
                            maloai = ?,
                            donvitinh = ?,
                            dongianhap = ?,
                            ton_toithieu = ?,
                            lead_time_ngay = ?,
                            ghichu = ?
                        WHERE LOWER(manvl_code) = LOWER(?)
                        """,
                        dto.getName(),
                        dto.getCategoryId(),
                        dto.getUnit(),
                        dto.getImportPrice(),
                        dto.getMinStock(),
                        dto.getLeadTimeDays(),
                        dto.getNote(),
                        dto.getCode()
                );
                count++;
            }
        }
        return count;
    }
}
