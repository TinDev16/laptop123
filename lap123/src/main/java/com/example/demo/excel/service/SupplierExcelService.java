package com.example.demo.excel.service;

import com.example.demo.excel.core.ExcelCoreUtils;
import com.example.demo.excel.core.ExcelImportResult;
import com.example.demo.excel.core.ExcelSecurityValidator;
import com.example.demo.excel.dto.SupplierExcelDto;
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
public class SupplierExcelService {

    private final JdbcTemplate jdbc;

    public SupplierExcelService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 1. TẠO FILE MẪU EXCEL NHÀ CUNG CẤP
     */
    public byte[] generateTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("DANH_MUC_NCC");

            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle instructionStyle = ExcelCoreUtils.createInstructionStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);

            String[] headers = {
                    "STT",
                    "Tên nhà cung cấp (*)",
                    "Số điện thoại",
                    "Email",
                    "Địa chỉ",
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
            instCell.setCellValue("HƯỚNG DẪN: Cột có dấu (*) là bắt buộc. 'Hành động': Để trống hoặc ghi 'THEM_MOI' để thêm đối tác mới; ghi 'CAP_NHAT' để cập nhật thông tin nhà cung cấp đã có sẵn theo tên.");
            instCell.setCellStyle(instructionStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, headers.length - 1));

            // Dòng mẫu 1
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue(1);
            r2.createCell(1).setCellValue("Công ty Cổ phần Công nghệ Synnex FPT");
            r2.createCell(2).setCellValue("02473006666");
            r2.createCell(3).setCellValue("contact@synnexfpt.com.vn");
            r2.createCell(4).setCellValue("Tòa nhà FPT, Phố Duy Tân, Cầu Giấy, Hà Nội");
            r2.createCell(5).setCellValue("THEM_MOI");
            r2.createCell(6).setCellValue("Nhà phân phối chính hãng linh kiện Asus, Dell, HP");

            // Dòng mẫu 2
            Row r3 = sheet.createRow(3);
            r3.createCell(0).setCellValue(2);
            r3.createCell(1).setCellValue("Công ty TNHH Vi tính Phong Vũ");
            r3.createCell(2).setCellValue("18006867");
            r3.createCell(3).setCellValue("cskh@phongvu.vn");
            r3.createCell(4).setCellValue("264 Nguyễn Thị Minh Khai, Phường 6, Quận 3, TP.HCM");
            r3.createCell(5).setCellValue("CAP_NHAT");
            r3.createCell(6).setCellValue("Cung ứng linh kiện RAM, SSD, màn hình");

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
     * 2. XUẤT DANH SÁCH NHÀ CUNG CẤP RA EXCEL
     * (CHIA NHIỀU BẢNG THEO TRẠNG THÁI: ĐANG HỢP TÁC / NGỪNG HỢP TÁC)
     */
    public byte[] exportSuppliers() throws IOException {
        String sql = "SELECT mancc, tenncc, diachi, sdt, email, trangthai, ghichu FROM ncc ORDER BY trangthai ASC, tenncc ASC";
        List<Map<String, Object>> rows = jdbc.queryForList(sql);

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);

            // Banner style
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

            // Subtotal style
            CellStyle subtotalStyle = workbook.createCellStyle();
            Font subtotalFont = workbook.createFont();
            subtotalFont.setBold(true);
            subtotalFont.setFontHeightInPoints((short) 10);
            subtotalStyle.setFont(subtotalFont);
            subtotalStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            subtotalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            subtotalStyle.setAlignment(HorizontalAlignment.LEFT);
            subtotalStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            subtotalStyle.setBorderTop(BorderStyle.THIN);
            subtotalStyle.setBorderBottom(BorderStyle.MEDIUM);
            subtotalStyle.setBorderLeft(BorderStyle.THIN);
            subtotalStyle.setBorderRight(BorderStyle.THIN);

            // ====================================================
            // SHEET 1: CHIA NHIỀU BẢNG THEO TRẠNG THÁI
            // ====================================================
            Sheet groupSheet = workbook.createSheet("THEO_TRANG_THAI");

            List<Map<String, Object>> activeList = new ArrayList<>();
            List<Map<String, Object>> inactiveList = new ArrayList<>();
            for (Map<String, Object> r : rows) {
                int st = r.get("trangthai") != null ? ((Number) r.get("trangthai")).intValue() : 0;
                if (st == 0) {
                    activeList.add(r);
                } else {
                    inactiveList.add(r);
                }
            }

            String[] tableHeaders = {
                    "STT",
                    "Mã NCC",
                    "Tên đối tác nhà cung cấp",
                    "Số điện thoại",
                    "Email liên hệ",
                    "Địa chỉ trụ sở / Kho hàng",
                    "Ghi chú"
            };

            int curRow = 0;

            // BẢNG 1: ĐANG HỢP TÁC
            Row b1 = groupSheet.createRow(curRow);
            b1.setHeightInPoints(28);
            Cell cB1 = b1.createCell(0);
            cB1.setCellValue("🤝 NHÀ CUNG CẤP ĐANG HỢP TÁC (" + activeList.size() + " đối tác)");
            cB1.setCellStyle(bannerStyle);
            for (int c = 1; c < tableHeaders.length; c++) b1.createCell(c).setCellStyle(bannerStyle);
            groupSheet.addMergedRegion(new CellRangeAddress(curRow, curRow, 0, tableHeaders.length - 1));
            curRow++;

            Row th1 = groupSheet.createRow(curRow);
            th1.setHeightInPoints(25);
            for (int i = 0; i < tableHeaders.length; i++) {
                Cell c = th1.createCell(i);
                c.setCellValue(tableHeaders[i]);
                c.setCellStyle(headerStyle);
            }
            curRow++;

            int stt1 = 1;
            for (Map<String, Object> item : activeList) {
                Row row = groupSheet.createRow(curRow);
                row.setHeightInPoints(20);

                row.createCell(0).setCellValue(stt1++);
                row.getCell(0).setCellStyle(dataStyle);

                row.createCell(1).setCellValue("NCC-" + item.get("mancc"));
                row.getCell(1).setCellStyle(dataStyle);

                row.createCell(2).setCellValue((String) item.get("tenncc"));
                row.getCell(2).setCellStyle(dataStyle);

                row.createCell(3).setCellValue(item.get("sdt") != null ? (String) item.get("sdt") : "-");
                row.getCell(3).setCellStyle(dataStyle);

                row.createCell(4).setCellValue(item.get("email") != null ? (String) item.get("email") : "-");
                row.getCell(4).setCellStyle(dataStyle);

                row.createCell(5).setCellValue(item.get("diachi") != null ? (String) item.get("diachi") : "-");
                row.getCell(5).setCellStyle(dataStyle);

                row.createCell(6).setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) item.get("ghichu")));
                row.getCell(6).setCellStyle(dataStyle);

                curRow++;
            }

            Row sub1 = groupSheet.createRow(curRow);
            sub1.setHeightInPoints(22);
            Cell s1 = sub1.createCell(0);
            s1.setCellValue("Tổng số nhà cung cấp đang hợp tác: " + activeList.size());
            s1.setCellStyle(subtotalStyle);
            for (int c = 1; c < tableHeaders.length; c++) sub1.createCell(c).setCellStyle(subtotalStyle);
            groupSheet.addMergedRegion(new CellRangeAddress(curRow, curRow, 0, tableHeaders.length - 1));
            curRow += 2; // Spacer

            // BẢNG 2: NGỪNG HỢP TÁC
            Row b2 = groupSheet.createRow(curRow);
            b2.setHeightInPoints(28);
            Cell cB2 = b2.createCell(0);
            cB2.setCellValue("📦 NHÀ CUNG CẤP ĐÃ NGỪNG HỢP TÁC / LƯU TRỮ (" + inactiveList.size() + " đối tác)");
            cB2.setCellStyle(bannerStyle);
            for (int c = 1; c < tableHeaders.length; c++) b2.createCell(c).setCellStyle(bannerStyle);
            groupSheet.addMergedRegion(new CellRangeAddress(curRow, curRow, 0, tableHeaders.length - 1));
            curRow++;

            Row th2 = groupSheet.createRow(curRow);
            th2.setHeightInPoints(25);
            for (int i = 0; i < tableHeaders.length; i++) {
                Cell c = th2.createCell(i);
                c.setCellValue(tableHeaders[i]);
                c.setCellStyle(headerStyle);
            }
            curRow++;

            int stt2 = 1;
            for (Map<String, Object> item : inactiveList) {
                Row row = groupSheet.createRow(curRow);
                row.setHeightInPoints(20);

                row.createCell(0).setCellValue(stt2++);
                row.getCell(0).setCellStyle(dataStyle);

                row.createCell(1).setCellValue("NCC-" + item.get("mancc"));
                row.getCell(1).setCellStyle(dataStyle);

                row.createCell(2).setCellValue((String) item.get("tenncc"));
                row.getCell(2).setCellStyle(dataStyle);

                row.createCell(3).setCellValue(item.get("sdt") != null ? (String) item.get("sdt") : "-");
                row.getCell(3).setCellStyle(dataStyle);

                row.createCell(4).setCellValue(item.get("email") != null ? (String) item.get("email") : "-");
                row.getCell(4).setCellStyle(dataStyle);

                row.createCell(5).setCellValue(item.get("diachi") != null ? (String) item.get("diachi") : "-");
                row.getCell(5).setCellStyle(dataStyle);

                row.createCell(6).setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) item.get("ghichu")));
                row.getCell(6).setCellStyle(dataStyle);

                curRow++;
            }

            Row sub2 = groupSheet.createRow(curRow);
            sub2.setHeightInPoints(22);
            Cell s2 = sub2.createCell(0);
            s2.setCellValue("Tổng số nhà cung cấp ngừng hợp tác: " + inactiveList.size());
            s2.setCellStyle(subtotalStyle);
            for (int c = 1; c < tableHeaders.length; c++) sub2.createCell(c).setCellStyle(subtotalStyle);
            groupSheet.addMergedRegion(new CellRangeAddress(curRow, curRow, 0, tableHeaders.length - 1));

            ExcelCoreUtils.autoSizeColumns(groupSheet, tableHeaders.length);

            // ====================================================
            // SHEET 2: TOÀN BỘ NHÀ CUNG CẤP DẠNG BẢNG PHẲNG
            // ====================================================
            Sheet flatSheet = workbook.createSheet("TOAN_BO_NHA_CUNG_CAP");
            String[] flatHeaders = {
                    "STT",
                    "Mã NCC",
                    "Tên nhà cung cấp",
                    "Số điện thoại",
                    "Email",
                    "Địa chỉ",
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
            for (Map<String, Object> item : rows) {
                Row row = flatSheet.createRow(fIdx);

                row.createCell(0).setCellValue(fIdx);
                row.getCell(0).setCellStyle(dataStyle);

                row.createCell(1).setCellValue("NCC-" + item.get("mancc"));
                row.getCell(1).setCellStyle(dataStyle);

                row.createCell(2).setCellValue((String) item.get("tenncc"));
                row.getCell(2).setCellStyle(dataStyle);

                row.createCell(3).setCellValue(item.get("sdt") != null ? (String) item.get("sdt") : "-");
                row.getCell(3).setCellStyle(dataStyle);

                row.createCell(4).setCellValue(item.get("email") != null ? (String) item.get("email") : "-");
                row.getCell(4).setCellStyle(dataStyle);

                row.createCell(5).setCellValue(item.get("diachi") != null ? (String) item.get("diachi") : "-");
                row.getCell(5).setCellStyle(dataStyle);

                int st = item.get("trangthai") != null ? ((Number) item.get("trangthai")).intValue() : 0;
                row.createCell(6).setCellValue(st == 0 ? "Đang hợp tác" : "Ngừng hợp tác");
                row.getCell(6).setCellStyle(dataStyle);

                row.createCell(7).setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) item.get("ghichu")));
                row.getCell(7).setCellStyle(dataStyle);

                fIdx++;
            }
            ExcelCoreUtils.autoSizeColumns(flatSheet, flatHeaders.length);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 3. KIỂM TRA DỮ LIỆU & XEM TRƯỚC NHÀ CUNG CẤP
     */
    public ExcelImportResult<SupplierExcelDto> validateAndPreview(InputStream is, long fileSize) throws IOException {
        ExcelSecurityValidator.validateFileSize(fileSize);

        byte[] fileBytes = is.readAllBytes();
        ExcelSecurityValidator.validateXlsxMagicBytes(new ByteArrayInputStream(fileBytes));

        ExcelImportResult<SupplierExcelDto> result = new ExcelImportResult<>();

        // Tra cứu NCC hiện có: tenncc_lower -> mancc
        List<Map<String, Object>> existingList = jdbc.queryForList("SELECT mancc, LOWER(TRIM(tenncc)) AS norm_name, tenncc FROM ncc");
        Map<String, Integer> existingMap = new HashMap<>();
        for (Map<String, Object> n : existingList) {
            existingMap.put((String) n.get("norm_name"), ((Number) n.get("mancc")).intValue());
        }

        Set<String> processedInFile = new HashSet<>();

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileBytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                result.addError(0, "Sheet", "", "Tệp Excel không có trang tính dữ liệu.");
                return result;
            }

            int lastRow = sheet.getLastRowNum();
            int totalProcessed = 0;

            for (int r = 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                boolean empty = true;
                for (int c = 1; c <= 4; c++) {
                    if (!ExcelCoreUtils.getCellStringValue(row.getCell(c)).isEmpty()) {
                        empty = false;
                        break;
                    }
                }
                if (empty) continue;

                String firstCell = ExcelCoreUtils.getCellStringValue(row.getCell(0));
                String secondCell = ExcelCoreUtils.getCellStringValue(row.getCell(1));
                if (firstCell.toUpperCase().contains("HƯỚNG DẪN") || secondCell.toUpperCase().contains("HƯỚNG DẪN")) {
                    continue;
                }

                totalProcessed++;
                int rowNumber = r + 1;
                boolean rowHasError = false;

                String name = ExcelCoreUtils.getCellStringValue(row.getCell(1));
                String phone = ExcelCoreUtils.getCellStringValue(row.getCell(2));
                String email = ExcelCoreUtils.getCellStringValue(row.getCell(3));
                String address = ExcelCoreUtils.getCellStringValue(row.getCell(4));
                String inputAction = ExcelCoreUtils.getCellStringValue(row.getCell(5)).toUpperCase();
                String note = ExcelCoreUtils.getCellStringValue(row.getCell(6));

                // 1. Kiểm tra Tên nhà cung cấp
                if (name.isEmpty()) {
                    result.addError(rowNumber, "Tên nhà cung cấp", "", "Tên nhà cung cấp không được để trống.");
                    rowHasError = true;
                } else if (name.length() < 2 || name.length() > 255) {
                    result.addError(rowNumber, "Tên nhà cung cấp", name, "Tên nhà cung cấp phải từ 2 đến 255 ký tự.");
                    rowHasError = true;
                }

                // 2. Kiểm tra trùng lặp trong tệp Excel
                String normName = name.toLowerCase().trim();
                if (!processedInFile.add(normName)) {
                    result.addError(rowNumber, "Tên nhà cung cấp", name, "Tên nhà cung cấp bị trùng lặp trong chính tệp Excel này.");
                    rowHasError = true;
                }

                // 3. Kiểm tra số điện thoại (nếu có)
                if (!phone.isEmpty()) {
                    String cleanPhone = phone.replaceAll("[\\s.-]", "");
                    if (!cleanPhone.matches("^[0-9+]{8,15}$")) {
                        result.addError(rowNumber, "Số điện thoại", phone, "Số điện thoại không hợp lệ (cần từ 8 - 15 chữ số).");
                        rowHasError = true;
                    }
                }

                // 4. Kiểm tra email (nếu có)
                if (!email.isEmpty() && !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                    result.addError(rowNumber, "Email", email, "Định dạng email không hợp lệ.");
                    rowHasError = true;
                }

                // 5. Xác định Hành động
                String finalAction = "THEM_MOI";
                Integer existingId = existingMap.get(normName);
                if (existingId != null) {
                    if ("THEM_MOI".equals(inputAction)) {
                        result.addError(rowNumber, "Tên nhà cung cấp", name, "Nhà cung cấp đã tồn tại trong hệ thống. Nếu muốn ghi đè thông tin, vui lòng chọn hành động 'CAP_NHAT'.");
                        rowHasError = true;
                    } else {
                        finalAction = "CAP_NHAT";
                    }
                } else {
                    if ("CAP_NHAT".equals(inputAction)) {
                        result.addError(rowNumber, "Tên nhà cung cấp", name, "Không tìm thấy nhà cung cấp để cập nhật. Vui lòng chọn hành động 'THEM_MOI'.");
                        rowHasError = true;
                    }
                }

                if (!rowHasError) {
                    SupplierExcelDto dto = new SupplierExcelDto();
                    dto.setRowNumber(rowNumber);
                    dto.setId(existingId);
                    dto.setName(name.trim());
                    dto.setPhone(phone.trim());
                    dto.setEmail(email.trim());
                    dto.setAddress(address.trim());
                    dto.setNote(note);
                    dto.setAction(finalAction);

                    result.getValidRows().add(dto);
                }
            }
            result.setTotalRows(totalProcessed);
        }

        return result;
    }

    /**
     * 4. XÁC NHẬN NẠP NHÀ CUNG CẤP VÀO CSDL
     */
    @Transactional
    public int confirmImport(List<SupplierExcelDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (SupplierExcelDto dto : dtos) {
            if ("THEM_MOI".equals(dto.getAction())) {
                jdbc.update(
                        "INSERT INTO ncc (tenncc, diachi, sdt, email, trangthai, ghichu) VALUES (?, ?, ?, ?, 0, ?)",
                        dto.getName(),
                        dto.getAddress(),
                        dto.getPhone(),
                        dto.getEmail(),
                        dto.getNote()
                );
                count++;
            } else if ("CAP_NHAT".equals(dto.getAction())) {
                jdbc.update(
                        "UPDATE ncc SET diachi = ?, sdt = ?, email = ?, ghichu = ? WHERE LOWER(TRIM(tenncc)) = LOWER(TRIM(?))",
                        dto.getAddress(),
                        dto.getPhone(),
                        dto.getEmail(),
                        dto.getNote(),
                        dto.getName()
                );
                count++;
            }
        }
        return count;
    }
}
