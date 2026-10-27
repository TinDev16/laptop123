package com.example.demo.excel.service;

import com.example.demo.excel.core.ExcelCoreUtils;
import com.example.demo.excel.core.ExcelImportResult;
import com.example.demo.excel.core.ExcelSecurityValidator;
import com.example.demo.excel.dto.CustomerExcelDto;
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
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class CustomerExcelService {

    private final JdbcTemplate jdbc;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public CustomerExcelService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 1. TẠO FILE MẪU EXCEL KHÁCH HÀNG / ĐẠI LÝ
     */
    public byte[] generateTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("DANH_MUC_KHACH_HANG");

            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle instructionStyle = ExcelCoreUtils.createInstructionStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);

            String[] headers = {
                    "STT",
                    "Họ và tên khách hàng (*)",
                    "Số điện thoại (*)",
                    "Email",
                    "Địa chỉ",
                    "Giới tính (Nam/Nữ)",
                    "Ngày sinh (dd/MM/yyyy)",
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
            instCell.setCellValue("HƯỚNG DẪN: Các cột (*) là bắt buộc. Hệ thống dùng 'Số điện thoại' làm mã định danh phân biệt khách hàng. 'Hành động': Để trống hoặc 'THEM_MOI' để thêm mới; 'CAP_NHAT' để cập nhật thông tin khách hàng cũ.");
            instCell.setCellStyle(instructionStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, headers.length - 1));

            // Dòng mẫu 1
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue(1);
            r2.createCell(1).setCellValue("Nguyễn Văn An");
            r2.createCell(2).setCellValue("0912345678");
            r2.createCell(3).setCellValue("an.nguyen@gmail.com");
            r2.createCell(4).setCellValue("123 Lê Lợi, Phường Bến Nghé, Quận 1, TP.HCM");
            r2.createCell(5).setCellValue("Nam");
            r2.createCell(6).setCellValue("15/08/1995");
            r2.createCell(7).setCellValue("THEM_MOI");
            r2.createCell(8).setCellValue("Khách hàng VIP mua lẻ laptop Gaming");

            // Dòng mẫu 2
            Row r3 = sheet.createRow(3);
            r3.createCell(0).setCellValue(2);
            r3.createCell(1).setCellValue("Công ty CP Công Nghệ Đại Việt");
            r3.createCell(2).setCellValue("02838999888");
            r3.createCell(3).setCellValue("daiviet.corp@gmail.com");
            r3.createCell(4).setCellValue("Số 45 Trần Phú, Hà Đông, Hà Nội");
            r3.createCell(5).setCellValue("Khác");
            r3.createCell(6).setCellValue("");
            r3.createCell(7).setCellValue("THEM_MOI");
            r3.createCell(8).setCellValue("Đại lý mua sỉ laptop số lượng lớn");

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
     * 2. XUẤT DANH SÁCH KHÁCH HÀNG RA EXCEL
     * (CHIA NHIỀU BẢNG: ĐANG HOẠT ĐỘNG / NGỪNG SỬ DỤNG)
     */
    public byte[] exportCustomers() throws IOException {
        String sql = """
            SELECT makh, hoten, sdt, email, diachi, gioitinh, ngaysinh, ngaythamgia, trangthai, ghichu
            FROM khachhang
            ORDER BY trangthai ASC, makh ASC
        """;
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
                if (st == 0) activeList.add(r);
                else inactiveList.add(r);
            }

            String[] tableHeaders = {
                    "STT",
                    "Mã KH",
                    "Họ và tên khách hàng / Đại lý",
                    "Số điện thoại",
                    "Email",
                    "Địa chỉ",
                    "Giới tính",
                    "Ngày sinh",
                    "Ngày tham gia",
                    "Ghi chú"
            };

            int curRow = 0;

            // BẢNG 1: KHÁCH HÀNG ĐANG HOẠT ĐỘNG
            Row b1 = groupSheet.createRow(curRow);
            b1.setHeightInPoints(28);
            Cell cB1 = b1.createCell(0);
            cB1.setCellValue("👤 KHÁCH HÀNG ĐANG HOẠT ĐỘNG / GIAO DỊCH (" + activeList.size() + " khách hàng)");
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

                row.createCell(1).setCellValue("KH-" + item.get("makh"));
                row.getCell(1).setCellStyle(dataStyle);

                row.createCell(2).setCellValue((String) item.get("hoten"));
                row.getCell(2).setCellStyle(dataStyle);

                row.createCell(3).setCellValue(item.get("sdt") != null ? (String) item.get("sdt") : "-");
                row.getCell(3).setCellStyle(dataStyle);

                row.createCell(4).setCellValue(item.get("email") != null ? (String) item.get("email") : "-");
                row.getCell(4).setCellStyle(dataStyle);

                row.createCell(5).setCellValue(item.get("diachi") != null ? (String) item.get("diachi") : "-");
                row.getCell(5).setCellStyle(dataStyle);

                row.createCell(6).setCellValue(item.get("gioitinh") != null ? (String) item.get("gioitinh") : "-");
                row.getCell(6).setCellStyle(dataStyle);

                Object bday = item.get("ngaysinh");
                row.createCell(7).setCellValue(bday != null ? bday.toString().substring(0, 10) : "-");
                row.getCell(7).setCellStyle(dataStyle);

                Object jday = item.get("ngaythamgia");
                row.createCell(8).setCellValue(jday != null ? jday.toString().substring(0, 10) : "-");
                row.getCell(8).setCellStyle(dataStyle);

                row.createCell(9).setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) item.get("ghichu")));
                row.getCell(9).setCellStyle(dataStyle);

                curRow++;
            }

            Row sub1 = groupSheet.createRow(curRow);
            sub1.setHeightInPoints(22);
            Cell s1 = sub1.createCell(0);
            s1.setCellValue("Tổng số khách hàng đang hoạt động: " + activeList.size());
            s1.setCellStyle(subtotalStyle);
            for (int c = 1; c < tableHeaders.length; c++) sub1.createCell(c).setCellStyle(subtotalStyle);
            groupSheet.addMergedRegion(new CellRangeAddress(curRow, curRow, 0, tableHeaders.length - 1));
            curRow += 2; // Spacer

            // BẢNG 2: NGỪNG SỬ DỤNG
            Row b2 = groupSheet.createRow(curRow);
            b2.setHeightInPoints(28);
            Cell cB2 = b2.createCell(0);
            cB2.setCellValue("📁 HỒ SƠ KHÁCH HÀNG ĐÃ LƯU TRỮ / NGỪNG SỬ DỤNG (" + inactiveList.size() + " hồ sơ)");
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

                row.createCell(1).setCellValue("KH-" + item.get("makh"));
                row.getCell(1).setCellStyle(dataStyle);

                row.createCell(2).setCellValue((String) item.get("hoten"));
                row.getCell(2).setCellStyle(dataStyle);

                row.createCell(3).setCellValue(item.get("sdt") != null ? (String) item.get("sdt") : "-");
                row.getCell(3).setCellStyle(dataStyle);

                row.createCell(4).setCellValue(item.get("email") != null ? (String) item.get("email") : "-");
                row.getCell(4).setCellStyle(dataStyle);

                row.createCell(5).setCellValue(item.get("diachi") != null ? (String) item.get("diachi") : "-");
                row.getCell(5).setCellStyle(dataStyle);

                row.createCell(6).setCellValue(item.get("gioitinh") != null ? (String) item.get("gioitinh") : "-");
                row.getCell(6).setCellStyle(dataStyle);

                Object bday = item.get("ngaysinh");
                row.createCell(7).setCellValue(bday != null ? bday.toString().substring(0, 10) : "-");
                row.getCell(7).setCellStyle(dataStyle);

                Object jday = item.get("ngaythamgia");
                row.createCell(8).setCellValue(jday != null ? jday.toString().substring(0, 10) : "-");
                row.getCell(8).setCellStyle(dataStyle);

                row.createCell(9).setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) item.get("ghichu")));
                row.getCell(9).setCellStyle(dataStyle);

                curRow++;
            }

            Row sub2 = groupSheet.createRow(curRow);
            sub2.setHeightInPoints(22);
            Cell s2 = sub2.createCell(0);
            s2.setCellValue("Tổng số khách hàng lưu trữ: " + inactiveList.size());
            s2.setCellStyle(subtotalStyle);
            for (int c = 1; c < tableHeaders.length; c++) sub2.createCell(c).setCellStyle(subtotalStyle);
            groupSheet.addMergedRegion(new CellRangeAddress(curRow, curRow, 0, tableHeaders.length - 1));

            ExcelCoreUtils.autoSizeColumns(groupSheet, tableHeaders.length);

            // ====================================================
            // SHEET 2: BẢNG PHẲNG TẤT CẢ KHÁCH HÀNG
            // ====================================================
            Sheet flatSheet = workbook.createSheet("TOAN_BO_KHACH_HANG");
            Row fhRow = flatSheet.createRow(0);
            fhRow.setHeightInPoints(28);
            for (int i = 0; i < tableHeaders.length; i++) {
                Cell cell = fhRow.createCell(i);
                cell.setCellValue(tableHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            int fIdx = 1;
            for (Map<String, Object> item : rows) {
                Row row = flatSheet.createRow(fIdx);

                row.createCell(0).setCellValue(fIdx);
                row.getCell(0).setCellStyle(dataStyle);

                row.createCell(1).setCellValue("KH-" + item.get("makh"));
                row.getCell(1).setCellStyle(dataStyle);

                row.createCell(2).setCellValue((String) item.get("hoten"));
                row.getCell(2).setCellStyle(dataStyle);

                row.createCell(3).setCellValue(item.get("sdt") != null ? (String) item.get("sdt") : "-");
                row.getCell(3).setCellStyle(dataStyle);

                row.createCell(4).setCellValue(item.get("email") != null ? (String) item.get("email") : "-");
                row.getCell(4).setCellStyle(dataStyle);

                row.createCell(5).setCellValue(item.get("diachi") != null ? (String) item.get("diachi") : "-");
                row.getCell(5).setCellStyle(dataStyle);

                row.createCell(6).setCellValue(item.get("gioitinh") != null ? (String) item.get("gioitinh") : "-");
                row.getCell(6).setCellStyle(dataStyle);

                Object bday = item.get("ngaysinh");
                row.createCell(7).setCellValue(bday != null ? bday.toString().substring(0, 10) : "-");
                row.getCell(7).setCellStyle(dataStyle);

                Object jday = item.get("ngaythamgia");
                row.createCell(8).setCellValue(jday != null ? jday.toString().substring(0, 10) : "-");
                row.getCell(8).setCellStyle(dataStyle);

                row.createCell(9).setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) item.get("ghichu")));
                row.getCell(9).setCellStyle(dataStyle);

                fIdx++;
            }
            ExcelCoreUtils.autoSizeColumns(flatSheet, tableHeaders.length);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 3. KIỂM TRA DỮ LIỆU & XEM TRƯỚC KHÁCH HÀNG
     */
    public ExcelImportResult<CustomerExcelDto> validateAndPreview(InputStream is, long fileSize) throws IOException {
        ExcelSecurityValidator.validateFileSize(fileSize);

        byte[] fileBytes = is.readAllBytes();
        ExcelSecurityValidator.validateXlsxMagicBytes(new ByteArrayInputStream(fileBytes));

        ExcelImportResult<CustomerExcelDto> result = new ExcelImportResult<>();

        // Tra cứu khách hàng theo SĐT: sdt_clean -> makh
        List<Map<String, Object>> existingList = jdbc.queryForList("SELECT makh, sdt FROM khachhang WHERE sdt IS NOT NULL AND sdt != ''");
        Map<String, Integer> phoneMap = new HashMap<>();
        for (Map<String, Object> k : existingList) {
            String clean = ((String) k.get("sdt")).replaceAll("[\\s.-]", "");
            phoneMap.put(clean, ((Number) k.get("makh")).intValue());
        }

        Set<String> processedPhones = new HashSet<>();

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
                String gender = ExcelCoreUtils.getCellStringValue(row.getCell(5));
                String birthdayStr = ExcelCoreUtils.getCellStringValue(row.getCell(6));
                String inputAction = ExcelCoreUtils.getCellStringValue(row.getCell(7)).toUpperCase();
                String note = ExcelCoreUtils.getCellStringValue(row.getCell(8));

                // 1. Tên khách hàng
                if (name.isEmpty()) {
                    result.addError(rowNumber, "Họ và tên", "", "Họ và tên khách hàng không được để trống.");
                    rowHasError = true;
                }

                // 2. Số điện thoại
                String cleanPhone = phone.replaceAll("[\\s.-]", "");
                if (cleanPhone.isEmpty()) {
                    result.addError(rowNumber, "Số điện thoại", "", "Số điện thoại là trường bắt buộc để quản lý khách hàng.");
                    rowHasError = true;
                } else if (!cleanPhone.matches("^[0-9+]{8,15}$")) {
                    result.addError(rowNumber, "Số điện thoại", phone, "Số điện thoại không đúng định dạng (8 - 15 chữ số).");
                    rowHasError = true;
                } else if (!processedPhones.add(cleanPhone)) {
                    result.addError(rowNumber, "Số điện thoại", phone, "Số điện thoại bị trùng lặp trong chính tệp Excel này.");
                    rowHasError = true;
                }

                // 3. Email
                if (!email.isEmpty() && !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                    result.addError(rowNumber, "Email", email, "Định dạng email không hợp lệ.");
                    rowHasError = true;
                }

                // 4. Ngày sinh
                LocalDate birthday = null;
                if (!birthdayStr.isEmpty()) {
                    try {
                        birthday = LocalDate.parse(birthdayStr.trim(), DATE_FORMATTER);
                    } catch (DateTimeParseException e1) {
                        try {
                            birthday = LocalDate.parse(birthdayStr.trim());
                        } catch (DateTimeParseException e2) {
                            result.addError(rowNumber, "Ngày sinh", birthdayStr, "Ngày sinh không đúng định dạng dd/MM/yyyy (VD: 15/08/1995).");
                            rowHasError = true;
                        }
                    }
                }

                // 5. Xác định hành động
                String finalAction = "THEM_MOI";
                Integer existingId = phoneMap.get(cleanPhone);
                if (existingId != null) {
                    if ("THEM_MOI".equals(inputAction)) {
                        result.addError(rowNumber, "Số điện thoại", phone, "Khách hàng với số điện thoại này đã tồn tại trong hệ thống. Chọn hành động 'CAP_NHAT' nếu muốn cập nhật thông tin.");
                        rowHasError = true;
                    } else {
                        finalAction = "CAP_NHAT";
                    }
                } else {
                    if ("CAP_NHAT".equals(inputAction)) {
                        result.addError(rowNumber, "Số điện thoại", phone, "Không tìm thấy khách hàng nào có số điện thoại này để cập nhật. Vui lòng chọn hành động 'THEM_MOI'.");
                        rowHasError = true;
                    }
                }

                if (!rowHasError) {
                    CustomerExcelDto dto = new CustomerExcelDto();
                    dto.setRowNumber(rowNumber);
                    dto.setId(existingId);
                    dto.setName(name.trim());
                    dto.setPhone(cleanPhone);
                    dto.setEmail(email.trim());
                    dto.setAddress(address.trim());
                    dto.setGender(!gender.isEmpty() ? gender : "Khác");
                    dto.setBirthday(birthday);
                    dto.setBirthdayStr(birthday != null ? birthday.format(DATE_FORMATTER) : "");
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
     * 4. XÁC NHẬN NẠP KHÁCH HÀNG VÀO CSDL
     */
    @Transactional
    public int confirmImport(List<CustomerExcelDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (CustomerExcelDto dto : dtos) {
            Date bday = dto.getBirthday() != null ? Date.valueOf(dto.getBirthday()) : null;

            if ("THEM_MOI".equals(dto.getAction())) {
                jdbc.update(
                        """
                        INSERT INTO khachhang
                        (hoten, sdt, email, diachi, gioitinh, ngaysinh, ngaythamgia, trangthai, ghichu)
                        VALUES (?, ?, ?, ?, ?, ?, NOW(), 0, ?)
                        """,
                        dto.getName(),
                        dto.getPhone(),
                        dto.getEmail(),
                        dto.getAddress(),
                        dto.getGender(),
                        bday,
                        dto.getNote()
                );
                count++;
            } else if ("CAP_NHAT".equals(dto.getAction())) {
                jdbc.update(
                        """
                        UPDATE khachhang
                        SET hoten = ?, email = ?, diachi = ?, gioitinh = ?, ngaysinh = ?, ghichu = ?
                        WHERE sdt = ?
                        """,
                        dto.getName(),
                        dto.getEmail(),
                        dto.getAddress(),
                        dto.getGender(),
                        bday,
                        dto.getNote(),
                        dto.getPhone()
                );
                count++;
            }
        }
        return count;
    }
}
