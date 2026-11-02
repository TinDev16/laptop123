package com.example.demo.excel.service;

import com.example.demo.excel.core.ExcelCoreUtils;
import com.example.demo.excel.core.ExcelImportResult;
import com.example.demo.excel.core.ExcelSecurityValidator;
import com.example.demo.excel.dto.EmployeeExcelDto;
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
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class EmployeeExcelService {

    private final JdbcTemplate jdbc;
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String DATE_PATTERN = "dd/MM/yyyy";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{9,12}$");

    public EmployeeExcelService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 1. TẠO FILE MẪU EXCEL NHÂN VIÊN
     */
    public byte[] generateTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("DANH_SACH_NHAN_VIEN");

            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle instructionStyle = ExcelCoreUtils.createInstructionStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);

            String[] headers = {
                    "STT",
                    "Họ và tên (*)",
                    "Giới tính (Nam/Nữ/Khác)",
                    "Ngày sinh (dd/MM/yyyy)",
                    "Số điện thoại",
                    "Email (*)",
                    "Ngày tham gia (dd/MM/yyyy)",
                    "Trạng thái (DANG_LAM_VIEC/DA_NGHI)",
                    "Hành động (THEM_MOI/CAP_NHAT)",
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
            instCell.setCellValue("HƯỚNG DẪN: Cột có dấu (*) là bắt buộc. 'Hành động': Để trống hoặc ghi 'THEM_MOI' để thêm mới; ghi 'CAP_NHAT' để cập nhật nhân viên theo Email. 'Trạng thái': Để trống mặc định là 'DANG_LAM_VIEC'. Ngày tháng nhập theo chuẩn dd/MM/yyyy.");
            instCell.setCellStyle(instructionStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, headers.length - 1));

            // Dòng mẫu 1
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue(1);
            r2.createCell(1).setCellValue("Nguyễn Văn An");
            r2.createCell(2).setCellValue("Nam");
            r2.createCell(3).setCellValue("15/05/1995");
            r2.createCell(4).setCellValue("0912345678");
            r2.createCell(5).setCellValue("an.nguyen@lap123.local");
            r2.createCell(6).setCellValue("01/01/2024");
            r2.createCell(7).setCellValue("DANG_LAM_VIEC");
            r2.createCell(8).setCellValue("THEM_MOI");
            r2.createCell(9).setCellValue("Kỹ thuật viên phần cứng");

            // Dòng mẫu 2
            Row r3 = sheet.createRow(3);
            r3.createCell(0).setCellValue(2);
            r3.createCell(1).setCellValue("Trần Thị Mai");
            r3.createCell(2).setCellValue("Nữ");
            r3.createCell(3).setCellValue("20/10/1998");
            r3.createCell(4).setCellValue("0987654321");
            r3.createCell(5).setCellValue("mai.tran@lap123.local");
            r3.createCell(6).setCellValue("15/03/2024");
            r3.createCell(7).setCellValue("DANG_LAM_VIEC");
            r3.createCell(8).setCellValue("CAP_NHAT");
            r3.createCell(9).setCellValue("Thủ kho máy tính");

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
     * 2. XUẤT TOÀN BỘ DANH SÁCH NHÂN VIÊN RA EXCEL
     */
    public byte[] exportEmployees() throws IOException {
        String sql = "SELECT manv, hoten, gioitinh, ngaysinh, sdt, email, ngaythamgia, trangthai, ghichu FROM nhanvien ORDER BY trangthai ASC, manv ASC";
        List<Map<String, Object>> list = jdbc.queryForList(sql);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("DANH_SACH_NHAN_VIEN");

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

            // Banner tiêu đề
            Row r0 = sheet.createRow(0);
            r0.setHeightInPoints(32);
            Cell cTitle = r0.createCell(0);
            cTitle.setCellValue("DANH SÁCH HỒ SƠ NHÂN VIÊN");
            cTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Thời điểm xuất danh sách: " + LocalDateTime.now().format(DATE_TIME_FMT));
            r1.getCell(0).setCellStyle(metaStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 9));

            String[] headers = {
                    "STT", "Mã NV", "Họ và tên", "Giới tính", "Ngày sinh",
                    "Số điện thoại", "Email", "Ngày tham gia", "Trạng thái", "Ghi chú"
            };
            Row rH = sheet.createRow(3);
            rH.setHeightInPoints(26);
            for (int i = 0; i < headers.length; i++) {
                Cell c = rH.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
            int rowIdx = 4;
            int stt = 1;
            int countActive = 0;
            int countInactive = 0;

            for (Map<String, Object> item : list) {
                Row r = sheet.createRow(rowIdx++);
                r.setHeightInPoints(22);

                r.createCell(0).setCellValue(stt++);
                r.getCell(0).setCellStyle(centerDataStyle);

                r.createCell(1).setCellValue("NV-" + item.get("manv"));
                r.getCell(1).setCellStyle(centerDataStyle);

                r.createCell(2).setCellValue(safeStr(item.get("hoten")));
                r.getCell(2).setCellStyle(dataStyle);

                r.createCell(3).setCellValue(safeStr(item.get("gioitinh")));
                r.getCell(3).setCellStyle(centerDataStyle);

                // Ngày sinh
                String birthStr = "";
                if (item.get("ngaysinh") != null) {
                    try {
                        birthStr = sdf.format((Date) item.get("ngaysinh"));
                    } catch (Exception ignored) {}
                }
                r.createCell(4).setCellValue(birthStr);
                r.getCell(4).setCellStyle(centerDataStyle);

                r.createCell(5).setCellValue(safeStr(item.get("sdt")));
                r.getCell(5).setCellStyle(centerDataStyle);

                r.createCell(6).setCellValue(safeStr(item.get("email")));
                r.getCell(6).setCellStyle(dataStyle);

                // Ngày tham gia
                String joinStr = "";
                if (item.get("ngaythamgia") != null) {
                    try {
                        joinStr = sdf.format((Date) item.get("ngaythamgia"));
                    } catch (Exception ignored) {}
                }
                r.createCell(7).setCellValue(joinStr);
                r.getCell(7).setCellStyle(centerDataStyle);

                int st = item.get("trangthai") != null ? ((Number) item.get("trangthai")).intValue() : 0;
                String stText = (st == 0) ? "Đang làm việc" : "Đã nghỉ việc";
                if (st == 0) countActive++; else countInactive++;

                Cell cSt = r.createCell(8);
                cSt.setCellValue(stText);
                cSt.setCellStyle(centerDataStyle);

                r.createCell(9).setCellValue(safeStr(item.get("ghichu")));
                r.getCell(9).setCellStyle(dataStyle);
            }

            // Dòng tổng kết
            Row rTotal = sheet.createRow(rowIdx);
            rTotal.setHeightInPoints(24);
            Cell cTot = rTotal.createCell(0);
            cTot.setCellValue(String.format("TỔNG CỘNG: %d nhân viên (Đang làm việc: %d | Đã nghỉ việc: %d)", list.size(), countActive, countInactive));
            cTot.setCellStyle(subtotalStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 9));

            ExcelCoreUtils.autoSizeColumns(sheet, headers.length);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 3. KIỂM TRA & XEM TRƯỚC (PREVIEW) TỆP EXCEL NHÂN VIÊN
     */
    public ExcelImportResult<EmployeeExcelDto> validateAndPreview(InputStream inputStream, long fileSize) throws IOException {
        ExcelSecurityValidator.validateFileSize(fileSize);

        byte[] fileBytes = inputStream.readAllBytes();
        ExcelSecurityValidator.validateXlsxMagicBytes(new ByteArrayInputStream(fileBytes));
        ExcelImportResult<EmployeeExcelDto> result = new ExcelImportResult<>();

        // Tra cứu danh sách nhân viên hiện có trong DB theo email và sdt
        Map<String, Integer> existingByEmail = new HashMap<>();
        Map<String, Integer> existingByPhone = new HashMap<>();
        List<Map<String, Object>> dbList = jdbc.queryForList("SELECT manv, LOWER(TRIM(email)) AS norm_email, TRIM(sdt) AS norm_phone FROM nhanvien");
        for (Map<String, Object> row : dbList) {
            int id = ((Number) row.get("manv")).intValue();
            String email = safeStr(row.get("norm_email")).toLowerCase();
            String phone = safeStr(row.get("norm_phone"));
            if (!email.isEmpty()) existingByEmail.put(email, id);
            if (!phone.isEmpty()) existingByPhone.put(phone, id);
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        sdf.setLenient(false);

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

            Set<String> seenEmailsInFile = new HashSet<>();
            Set<String> seenPhonesInFile = new HashSet<>();
            int totalProcessed = 0;

            for (int r = 2; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                boolean empty = true;
                for (int c = 0; c < 10; c++) {
                    if (!ExcelCoreUtils.getCellStringValue(row.getCell(c)).isEmpty()) {
                        empty = false;
                        break;
                    }
                }
                if (empty) continue;

                totalProcessed++;
                int rowNum = r + 1;
                boolean hasRowError = false;

                String fullName = ExcelCoreUtils.getCellStringValue(row.getCell(1)).trim();
                String gender = ExcelCoreUtils.getCellStringValue(row.getCell(2)).trim();
                String birthDate = ExcelCoreUtils.getCellStringValue(row.getCell(3)).trim();
                String phone = ExcelCoreUtils.getCellStringValue(row.getCell(4)).trim();
                String email = ExcelCoreUtils.getCellStringValue(row.getCell(5)).trim();
                String joinDate = ExcelCoreUtils.getCellStringValue(row.getCell(6)).trim();
                String statusStr = ExcelCoreUtils.getCellStringValue(row.getCell(7)).trim();
                String actionStr = ExcelCoreUtils.getCellStringValue(row.getCell(8)).trim().toUpperCase();
                String note = ExcelCoreUtils.getCellStringValue(row.getCell(9)).trim();

                // 1. Kiểm tra Họ tên
                if (fullName.isEmpty()) {
                    result.addError(rowNum, "Họ và tên (*)", "", "Họ tên không được để trống.");
                    hasRowError = true;
                }

                // 2. Kiểm tra Email
                if (email.isEmpty()) {
                    result.addError(rowNum, "Email (*)", "", "Email không được để trống.");
                    hasRowError = true;
                } else if (!EMAIL_PATTERN.matcher(email).matches()) {
                    result.addError(rowNum, "Email (*)", email, "Email không hợp lệ (ví dụ: example@lap123.local).");
                    hasRowError = true;
                } else {
                    String normEmail = email.toLowerCase();
                    if (seenEmailsInFile.contains(normEmail)) {
                        result.addError(rowNum, "Email (*)", email, "Email bị trùng lặp trong tệp Excel.");
                        hasRowError = true;
                    } else {
                        seenEmailsInFile.add(normEmail);
                    }
                }

                // 3. Kiểm tra Số điện thoại
                if (!phone.isEmpty()) {
                    if (!PHONE_PATTERN.matcher(phone).matches()) {
                        result.addError(rowNum, "Số điện thoại", phone, "Số điện thoại phải từ 9-12 chữ số.");
                        hasRowError = true;
                    } else if (seenPhonesInFile.contains(phone)) {
                        result.addError(rowNum, "Số điện thoại", phone, "Số điện thoại bị trùng lặp trong tệp Excel.");
                        hasRowError = true;
                    } else {
                        seenPhonesInFile.add(phone);
                    }
                }

                // 4. Kiểm tra Ngày sinh (dd/MM/yyyy)
                if (!birthDate.isEmpty()) {
                    try {
                        sdf.parse(birthDate);
                    } catch (ParseException e) {
                        result.addError(rowNum, "Ngày sinh", birthDate, "Ngày sinh không đúng định dạng dd/MM/yyyy.");
                        hasRowError = true;
                    }
                }

                // 5. Kiểm tra Ngày vào làm (dd/MM/yyyy)
                if (!joinDate.isEmpty()) {
                    try {
                        sdf.parse(joinDate);
                    } catch (ParseException e) {
                        result.addError(rowNum, "Ngày tham gia", joinDate, "Ngày tham gia không đúng định dạng dd/MM/yyyy.");
                        hasRowError = true;
                    }
                }

                // 6. Giới tính
                if (gender.isEmpty()) {
                    gender = "Nam";
                }

                // 7. Xác định Hành động
                Integer existingId = !email.isEmpty() ? existingByEmail.get(email.toLowerCase()) : null;
                if (existingId == null && !phone.isEmpty()) {
                    existingId = existingByPhone.get(phone);
                }

                String finalAction = actionStr;
                if (finalAction.isEmpty()) {
                    finalAction = (existingId != null) ? "CAP_NHAT" : "THEM_MOI";
                } else if (!finalAction.equals("THEM_MOI") && !finalAction.equals("CAP_NHAT")) {
                    result.addError(rowNum, "Hành động", actionStr, "Hành động chỉ chấp nhận 'THEM_MOI' hoặc 'CAP_NHAT'.");
                    hasRowError = true;
                }

                // 8. Đối chiếu DB
                if ("THEM_MOI".equals(finalAction) && existingId != null) {
                    result.addError(rowNum, "Email (*)", email, "Nhân viên đã tồn tại trong hệ thống (Mã: NV-" + existingId + "). Dùng 'CAP_NHAT' để sửa.");
                    hasRowError = true;
                } else if ("CAP_NHAT".equals(finalAction) && existingId == null) {
                    result.addError(rowNum, "Email (*)", email, "Không tìm thấy nhân viên này trong hệ thống để cập nhật.");
                    hasRowError = true;
                }

                // 9. Trạng thái
                String finalStatus = "DANG_LAM_VIEC";
                if (!statusStr.isEmpty()) {
                    if ("DA_NGHI".equalsIgnoreCase(statusStr) || "NGHI_VIEC".equalsIgnoreCase(statusStr) || "1".equals(statusStr)) {
                        finalStatus = "DA_NGHI";
                    } else if ("DANG_LAM_VIEC".equalsIgnoreCase(statusStr) || "0".equals(statusStr)) {
                        finalStatus = "DANG_LAM_VIEC";
                    } else {
                        result.addError(rowNum, "Trạng thái", statusStr, "Trạng thái chỉ chấp nhận 'DANG_LAM_VIEC' hoặc 'DA_NGHI'.");
                        hasRowError = true;
                    }
                }

                if (!hasRowError) {
                    EmployeeExcelDto dto = new EmployeeExcelDto();
                    dto.setRowNumber(rowNum);
                    dto.setId(existingId);
                    dto.setFullName(fullName);
                    dto.setGender(gender);
                    dto.setBirthDate(birthDate);
                    dto.setPhone(phone);
                    dto.setEmail(email);
                    dto.setJoinDate(joinDate);
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
    public int confirmImport(List<EmployeeExcelDto> rows) {
        if (rows == null || rows.isEmpty()) return 0;
        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

        for (EmployeeExcelDto dto : rows) {
            int stCode = "DA_NGHI".equalsIgnoreCase(dto.getStatus()) ? 1 : 0;

            Timestamp birthTs = null;
            if (dto.getBirthDate() != null && !dto.getBirthDate().trim().isEmpty()) {
                try {
                    birthTs = new Timestamp(sdf.parse(dto.getBirthDate().trim()).getTime());
                } catch (Exception ignored) {}
            }

            Timestamp joinTs = null;
            if (dto.getJoinDate() != null && !dto.getJoinDate().trim().isEmpty()) {
                try {
                    joinTs = new Timestamp(sdf.parse(dto.getJoinDate().trim()).getTime());
                } catch (Exception ignored) {}
            }

            if ("THEM_MOI".equalsIgnoreCase(dto.getAction())) {
                jdbc.update(
                        "INSERT INTO nhanvien (hoten, gioitinh, ngaysinh, sdt, email, ngaythamgia, trangthai, ghichu) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        dto.getFullName(),
                        dto.getGender(),
                        birthTs,
                        dto.getPhone(),
                        dto.getEmail(),
                        joinTs,
                        stCode,
                        dto.getNote()
                );
                count++;
            } else if ("CAP_NHAT".equalsIgnoreCase(dto.getAction())) {
                if (dto.getId() != null) {
                    jdbc.update(
                            "UPDATE nhanvien SET hoten = ?, gioitinh = ?, ngaysinh = ?, sdt = ?, email = ?, ngaythamgia = ?, trangthai = ?, ghichu = ? WHERE manv = ?",
                            dto.getFullName(),
                            dto.getGender(),
                            birthTs,
                            dto.getPhone(),
                            dto.getEmail(),
                            joinTs,
                            stCode,
                            dto.getNote(),
                            dto.getId()
                    );
                    count++;
                } else {
                    jdbc.update(
                            "UPDATE nhanvien SET hoten = ?, gioitinh = ?, ngaysinh = ?, sdt = ?, ngaythamgia = ?, trangthai = ?, ghichu = ? WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))",
                            dto.getFullName(),
                            dto.getGender(),
                            birthTs,
                            dto.getPhone(),
                            joinTs,
                            stCode,
                            dto.getNote(),
                            dto.getEmail()
                    );
                    count++;
                }
            }
        }
        return count;
    }

    private String safeStr(Object obj) {
        if (obj == null) return "";
        return ExcelSecurityValidator.sanitizeForFormulaInjection(obj.toString().trim());
    }
}
