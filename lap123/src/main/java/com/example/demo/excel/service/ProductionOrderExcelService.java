package com.example.demo.excel.service;

import com.example.demo.excel.core.ExcelCoreUtils;
import com.example.demo.excel.core.ExcelImportResult;
import com.example.demo.excel.core.ExcelSecurityValidator;
import com.example.demo.excel.dto.ProductionOrderExcelDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class ProductionOrderExcelService {

    private final JdbcTemplate jdbc;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ProductionOrderExcelService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 1. TẠO FILE MẪU EXCEL YÊU CẦU SẢN XUẤT (KẾ HOẠCH SX)
     */
    public byte[] generateTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Sheet 1: Kế hoạch YCSX
            Sheet sheet = workbook.createSheet("KE_HOACH_YCSX");

            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle instructionStyle = ExcelCoreUtils.createInstructionStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);
            CellStyle numberStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0");

            String[] headers = {
                    "STT",
                    "Tên đợt SX / Đơn hàng kinh doanh (*)",
                    "Tên dòng máy Laptop (*)",
                    "Phiên bản BOM (*)",
                    "Số lượng cần ráp (*)",
                    "Hạn hoàn thành (dd/MM/yyyy) (*)",
                    "Nhân viên phụ trách xưởng (Tùy chọn)",
                    "Ghi chú đơn hàng"
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
            instCell.setCellValue("HƯỚNG DẪN: Các cột dấu (*) là bắt buộc. 'Tên đợt SX' dùng để gom nhóm các đơn hàng kinh doanh. 'Tên máy' và 'Phiên bản BOM' tra cứu từ Sheet bên cạnh. Hạn hoàn thành nhập theo định dạng ngày/tháng/năm (VD: 31/12/2026).");
            instCell.setCellStyle(instructionStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, headers.length - 1));

            // Dòng mẫu 1
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue(1);
            r2.createCell(1).setCellValue("Đợt SX Q4 - ĐH Doanh Nghiệp FPT");
            r2.createCell(2).setCellValue("Laptop ASUS ROG Strix G16");
            r2.createCell(3).setCellValue("2026");
            Cell r2c4 = r2.createCell(4);
            r2c4.setCellValue(20);
            r2c4.setCellStyle(numberStyle);
            r2.createCell(5).setCellValue(LocalDate.now().plusDays(15).format(DATE_FORMATTER));
            r2.createCell(6).setCellValue("Trần Văn Nhân Viên");
            r2.createCell(7).setCellValue("Đơn hàng hợp đồng số HD-2026-FPT-01");

            // Dòng mẫu 2
            Row r3 = sheet.createRow(3);
            r3.createCell(0).setCellValue(2);
            r3.createCell(1).setCellValue("Đợt SX Q4 - ĐH Doanh Nghiệp FPT");
            r3.createCell(2).setCellValue("Laptop ASUS ROG Strix G16");
            r3.createCell(3).setCellValue("2026");
            Cell r3c4 = r3.createCell(4);
            r3c4.setCellValue(10);
            r3c4.setCellStyle(numberStyle);
            r3.createCell(5).setCellValue(LocalDate.now().plusDays(20).format(DATE_FORMATTER));
            r3.createCell(6).setCellValue("Lê Tra");
            r3.createCell(7).setCellValue("Đơn hàng hợp đồng số HD-2026-FPT-02");

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

            // ====================================================
            // SHEET 2: DANH MỤC LAPTOP & CÔNG THỨC BOM HIỆU LỰC
            // ====================================================
            Sheet bomSheet = workbook.createSheet("DANH_MUC_LAPTOP_BOM");
            Row bhRow = bomSheet.createRow(0);
            bhRow.setHeightInPoints(24);
            String[] bomHeaders = {"Mã BOM", "Tên dòng máy Laptop", "Phiên bản BOM", "Trạng thái", "Ghi chú BOM"};
            for (int i = 0; i < bomHeaders.length; i++) {
                Cell cell = bhRow.createCell(i);
                cell.setCellValue(bomHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Map<String, Object>> bomList = jdbc.queryForList(
                    "SELECT b.mabom, s.tensp, b.phienban, b.trangthai, b.ghichu " +
                    "FROM bom b JOIN sanpham s ON s.masp = b.masp " +
                    "WHERE b.trangthai = 'HIEU_LUC' ORDER BY s.tensp ASC, b.phienban ASC"
            );
            int bRowIdx = 1;
            for (Map<String, Object> b : bomList) {
                Row row = bomSheet.createRow(bRowIdx++);
                Cell c0 = row.createCell(0);
                c0.setCellValue(((Number) b.get("mabom")).intValue());
                c0.setCellStyle(dataStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue((String) b.get("tensp"));
                c1.setCellStyle(dataStyle);

                Cell c2 = row.createCell(2);
                c2.setCellValue((String) b.get("phienban"));
                c2.setCellStyle(dataStyle);

                Cell c3 = row.createCell(3);
                c3.setCellValue("Hiệu lực");
                c3.setCellStyle(dataStyle);

                Cell c4 = row.createCell(4);
                c4.setCellValue(b.get("ghichu") != null ? (String) b.get("ghichu") : "");
                c4.setCellStyle(dataStyle);
            }
            ExcelCoreUtils.autoSizeColumns(bomSheet, bomHeaders.length);

            // ====================================================
            // SHEET 3: DANH SÁCH NHÂN VIÊN XƯỞNG
            // ====================================================
            Sheet empSheet = workbook.createSheet("DANH_SACH_NHAN_VIEN_XUONG");
            Row ehRow = empSheet.createRow(0);
            ehRow.setHeightInPoints(24);
            String[] empHeaders = {"Mã NV", "Họ tên nhân viên", "Email"};
            for (int i = 0; i < empHeaders.length; i++) {
                Cell cell = ehRow.createCell(i);
                cell.setCellValue(empHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Map<String, Object>> empList = jdbc.queryForList(
                    "SELECT n.manv, n.hoten, t.email " +
                    "FROM nhanvien n JOIN taikhoan t ON t.manv = n.manv " +
                    "JOIN vaitro v ON v.vaitro_id = t.vaitro_id " +
                    "WHERE n.trangthai = 0 AND v.ma_vaitro = 'NHAN_VIEN' " +
                    "ORDER BY n.hoten ASC"
            );
            int eRowIdx = 1;
            for (Map<String, Object> e : empList) {
                Row row = empSheet.createRow(eRowIdx++);
                Cell c0 = row.createCell(0);
                c0.setCellValue(((Number) e.get("manv")).intValue());
                c0.setCellStyle(dataStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue((String) e.get("hoten"));
                c1.setCellStyle(dataStyle);

                Cell c2 = row.createCell(2);
                c2.setCellValue((String) e.get("email"));
                c2.setCellStyle(dataStyle);
            }
            ExcelCoreUtils.autoSizeColumns(empSheet, empHeaders.length);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 2. XUẤT TOÀN BỘ KẾ HOẠCH & TIẾN ĐỘ YCSX RA EXCEL
     * (CHIA NHIỀU BẢNG TRÊN 1 FILE THEO TỪNG ĐỢT SẢN XUẤT)
     */
    public byte[] exportOrders() throws IOException {
        String sql = """
            SELECT
                y.maycsx,
                COALESCE(d.madot, 0) AS madot,
                COALESCE(d.madot_code, CONCAT('YCSX-', y.maycsx)) AS madot_code,
                COALESCE(d.tendot, 'Yêu cầu sản xuất lẻ') AS tendot,
                s.masp,
                s.tensp,
                b.phienban,
                y.soluongyeucau,
                y.soluonghoanthanh,
                y.ngayyeucau,
                y.hanhoanthanh,
                y.trangthai,
                y.ghichu,
                COALESCE(nv.hoten, 'Chưa chỉ định') AS nguoiphutrach_ten
            FROM yeucausanxuat y
            JOIN sanpham s ON s.masp = y.masp
            JOIN bom b ON b.mabom = y.mabom
            LEFT JOIN dot_san_xuat d ON d.madot = y.madot
            LEFT JOIN nhanvien nv ON nv.manv = y.nguoiphutrach
            ORDER BY COALESCE(d.madot, y.maycsx) DESC, y.maycsx ASC
        """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql);

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);
            CellStyle numberStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0");
            CellStyle percentStyle = ExcelCoreUtils.createNumberStyle(workbook, "0.0%");

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
            // SHEET 1: CHIA NHIỀU BẢNG THEO ĐỢT SẢN XUẤT
            // ====================================================
            Sheet groupSheet = workbook.createSheet("THEO_DOT_SAN_XUAT");

            Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
            for (Map<String, Object> r : rows) {
                String tendot = (String) r.get("tendot");
                String madotCode = (String) r.get("madot_code");
                String key = tendot + " (" + madotCode + ")";
                groups.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
            }

            String[] tableHeaders = {
                    "STT",
                    "Mã YCSX",
                    "Dòng máy Laptop",
                    "Phiên bản BOM",
                    "SL Yêu cầu",
                    "Đã hoàn thành",
                    "Tiến độ (%)",
                    "Nhân viên phụ trách",
                    "Hạn hoàn thành",
                    "Trạng thái",
                    "Ghi chú"
            };

            int curRow = 0;
            for (Map.Entry<String, List<Map<String, Object>>> entry : groups.entrySet()) {
                String batchTitle = entry.getKey();
                List<Map<String, Object>> items = entry.getValue();

                // 1. Banner
                Row bannerRow = groupSheet.createRow(curRow);
                bannerRow.setHeightInPoints(28);
                Cell bannerCell = bannerRow.createCell(0);
                bannerCell.setCellValue("🏭 ĐỢT SẢN XUẤT: " + batchTitle.toUpperCase() + " [" + items.size() + " đơn hàng con]");
                bannerCell.setCellStyle(bannerStyle);
                for (int c = 1; c < tableHeaders.length; c++) {
                    bannerRow.createCell(c).setCellStyle(bannerStyle);
                }
                groupSheet.addMergedRegion(new CellRangeAddress(curRow, curRow, 0, tableHeaders.length - 1));
                curRow++;

                // 2. Header
                Row thRow = groupSheet.createRow(curRow);
                thRow.setHeightInPoints(25);
                for (int i = 0; i < tableHeaders.length; i++) {
                    Cell c = thRow.createCell(i);
                    c.setCellValue(tableHeaders[i]);
                    c.setCellStyle(headerStyle);
                }
                curRow++;

                // 3. Rows
                int stt = 1;
                int sumRequested = 0;
                int sumCompleted = 0;

                for (Map<String, Object> it : items) {
                    Row dataRow = groupSheet.createRow(curRow);
                    dataRow.setHeightInPoints(20);

                    Cell c0 = dataRow.createCell(0);
                    c0.setCellValue(stt++);
                    c0.setCellStyle(dataStyle);

                    Cell c1 = dataRow.createCell(1);
                    c1.setCellValue("YCSX-" + it.get("maycsx"));
                    c1.setCellStyle(dataStyle);

                    Cell c2 = dataRow.createCell(2);
                    c2.setCellValue((String) it.get("tensp"));
                    c2.setCellStyle(dataStyle);

                    Cell c3 = dataRow.createCell(3);
                    c3.setCellValue((String) it.get("phienban"));
                    c3.setCellStyle(dataStyle);

                    int req = it.get("soluongyeucau") != null ? ((Number) it.get("soluongyeucau")).intValue() : 0;
                    int comp = it.get("soluonghoanthanh") != null ? ((Number) it.get("soluonghoanthanh")).intValue() : 0;
                    sumRequested += req;
                    sumCompleted += comp;

                    Cell c4 = dataRow.createCell(4);
                    c4.setCellValue(req);
                    c4.setCellStyle(numberStyle);

                    Cell c5 = dataRow.createCell(5);
                    c5.setCellValue(comp);
                    c5.setCellStyle(numberStyle);

                    Cell c6 = dataRow.createCell(6);
                    double progress = req > 0 ? (double) comp / req : 0.0;
                    c6.setCellValue(progress);
                    c6.setCellStyle(percentStyle);

                    Cell c7 = dataRow.createCell(7);
                    c7.setCellValue((String) it.get("nguoiphutrach_ten"));
                    c7.setCellStyle(dataStyle);

                    Cell c8 = dataRow.createCell(8);
                    Object dVal = it.get("hanhoanthanh");
                    c8.setCellValue(dVal != null ? dVal.toString() : "-");
                    c8.setCellStyle(dataStyle);

                    Cell c9 = dataRow.createCell(9);
                    String st = (String) it.get("trangthai");
                    String stDisplay = switch (st != null ? st : "") {
                        case "HOAN_THANH" -> "Hoàn thành";
                        case "DANG_THUC_HIEN" -> "Đang sản xuất";
                        case "MOI_TAO" -> "Mới tạo";
                        case "HUY" -> "Đã hủy";
                        default -> st != null ? st : "";
                    };
                    c9.setCellValue(stDisplay);
                    c9.setCellStyle(dataStyle);

                    Cell c10 = dataRow.createCell(10);
                    c10.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) it.get("ghichu")));
                    c10.setCellStyle(dataStyle);

                    curRow++;
                }

                // 4. Subtotal
                Row subRow = groupSheet.createRow(curRow);
                subRow.setHeightInPoints(22);
                Cell s0 = subRow.createCell(0);
                s0.setCellValue("Tổng số lượng thành phẩm đợt này:");
                s0.setCellStyle(subtotalText);
                for (int c = 1; c < 4; c++) {
                    subRow.createCell(c).setCellStyle(subtotalStyle);
                }
                groupSheet.addMergedRegion(new CellRangeAddress(curRow, curRow, 0, 3));

                Cell sReq = subRow.createCell(4);
                sReq.setCellValue(sumRequested);
                sReq.setCellStyle(subtotalStyle);

                Cell sComp = subRow.createCell(5);
                sComp.setCellValue(sumCompleted);
                sComp.setCellStyle(subtotalStyle);

                for (int c = 6; c < tableHeaders.length; c++) {
                    subRow.createCell(c).setCellStyle(subtotalStyle);
                }
                curRow++;

                // 5. Spacer
                curRow++;
            }
            ExcelCoreUtils.autoSizeColumns(groupSheet, tableHeaders.length);

            // ====================================================
            // SHEET 2: TOÀN BỘ YCSX DẠNG BẢNG PHẲNG
            // ====================================================
            Sheet flatSheet = workbook.createSheet("TOAN_BO_YEU_CAU_SAN_XUAT");
            String[] flatHeaders = {
                    "STT",
                    "Mã YCSX",
                    "Tên đợt sản xuất",
                    "Mã đợt",
                    "Tên dòng máy Laptop",
                    "Phiên bản BOM",
                    "SL Yêu cầu",
                    "Đã hoàn thành",
                    "Tiến độ (%)",
                    "Hạn hoàn thành",
                    "Nhân viên phụ trách",
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
            for (Map<String, Object> it : rows) {
                Row row = flatSheet.createRow(fIdx);

                Cell c0 = row.createCell(0);
                c0.setCellValue(fIdx);
                c0.setCellStyle(dataStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue("YCSX-" + it.get("maycsx"));
                c1.setCellStyle(dataStyle);

                Cell c2 = row.createCell(2);
                c2.setCellValue((String) it.get("tendot"));
                c2.setCellStyle(dataStyle);

                Cell c3 = row.createCell(3);
                c3.setCellValue((String) it.get("madot_code"));
                c3.setCellStyle(dataStyle);

                Cell c4 = row.createCell(4);
                c4.setCellValue((String) it.get("tensp"));
                c4.setCellStyle(dataStyle);

                Cell c5 = row.createCell(5);
                c5.setCellValue((String) it.get("phienban"));
                c5.setCellStyle(dataStyle);

                int req = it.get("soluongyeucau") != null ? ((Number) it.get("soluongyeucau")).intValue() : 0;
                int comp = it.get("soluonghoanthanh") != null ? ((Number) it.get("soluonghoanthanh")).intValue() : 0;

                Cell c6 = row.createCell(6);
                c6.setCellValue(req);
                c6.setCellStyle(numberStyle);

                Cell c7 = row.createCell(7);
                c7.setCellValue(comp);
                c7.setCellStyle(numberStyle);

                Cell c8 = row.createCell(8);
                double progress = req > 0 ? (double) comp / req : 0.0;
                c8.setCellValue(progress);
                c8.setCellStyle(percentStyle);

                Cell c9 = row.createCell(9);
                Object dVal = it.get("hanhoanthanh");
                c9.setCellValue(dVal != null ? dVal.toString() : "-");
                c9.setCellStyle(dataStyle);

                Cell c10 = row.createCell(10);
                c10.setCellValue((String) it.get("nguoiphutrach_ten"));
                c10.setCellStyle(dataStyle);

                Cell c11 = row.createCell(11);
                String st = (String) it.get("trangthai");
                c11.setCellValue(st != null ? st : "");
                c11.setCellStyle(dataStyle);

                Cell c12 = row.createCell(12);
                c12.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) it.get("ghichu")));
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
     * 3. KIỂM TRA DỮ LIỆU & XEM TRƯỚC YÊU CẦU SẢN XUẤT
     */
    public ExcelImportResult<ProductionOrderExcelDto> validateAndPreview(InputStream is, long fileSize) throws IOException {
        ExcelSecurityValidator.validateFileSize(fileSize);

        byte[] fileBytes = is.readAllBytes();
        ExcelSecurityValidator.validateXlsxMagicBytes(new ByteArrayInputStream(fileBytes));

        ExcelImportResult<ProductionOrderExcelDto> result = new ExcelImportResult<>();

        // Tra cứu BOM hiệu lực: key = tensp_lower + "#" + phienban_lower -> mabom & masp
        List<Map<String, Object>> bomList = jdbc.queryForList(
                "SELECT b.mabom, b.masp, LOWER(TRIM(s.tensp)) AS prod_name, LOWER(TRIM(b.phienban)) AS ver_name, " +
                "s.tensp, b.phienban " +
                "FROM bom b JOIN sanpham s ON s.masp = b.masp " +
                "WHERE b.trangthai = 'HIEU_LUC'"
        );
        Map<String, Map<String, Object>> bomMap = new HashMap<>();
        for (Map<String, Object> b : bomList) {
            String key = b.get("prod_name") + "#" + b.get("ver_name");
            bomMap.put(key, b);
        }

        // Tra cứu nhân viên: hoten_lower -> manv
        List<Map<String, Object>> empList = jdbc.queryForList(
                "SELECT n.manv, LOWER(TRIM(n.hoten)) AS norm_name, n.hoten " +
                "FROM nhanvien n JOIN taikhoan t ON t.manv = n.manv " +
                "JOIN vaitro v ON v.vaitro_id = t.vaitro_id " +
                "WHERE n.trangthai = 0 AND v.ma_vaitro = 'NHAN_VIEN'"
        );
        Map<String, Integer> empMap = new HashMap<>();
        Map<String, String> empRealNames = new HashMap<>();
        Integer defaultEmployeeId = null;
        String defaultEmployeeName = "Nhân viên xưởng";
        if (!empList.isEmpty()) {
            defaultEmployeeId = ((Number) empList.get(0).get("manv")).intValue();
            defaultEmployeeName = (String) empList.get(0).get("hoten");
        }
        for (Map<String, Object> e : empList) {
            empMap.put((String) e.get("norm_name"), ((Number) e.get("manv")).intValue());
            empRealNames.put((String) e.get("norm_name"), (String) e.get("hoten"));
        }

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
                for (int c = 1; c <= 5; c++) {
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

                String batchName = ExcelCoreUtils.getCellStringValue(row.getCell(1));
                String prodName = ExcelCoreUtils.getCellStringValue(row.getCell(2));
                String bomVer = ExcelCoreUtils.getCellStringValue(row.getCell(3));
                String qtyStr = ExcelCoreUtils.getCellStringValue(row.getCell(4));
                String deadlineStr = ExcelCoreUtils.getCellStringValue(row.getCell(5));
                String assigneeInput = ExcelCoreUtils.getCellStringValue(row.getCell(6));
                String note = ExcelCoreUtils.getCellStringValue(row.getCell(7));

                // 1. Tên đợt sản xuất
                if (batchName.isEmpty()) {
                    result.addError(rowNumber, "Tên đợt SX", "", "Tên đợt sản xuất không được để trống.");
                    rowHasError = true;
                } else if (batchName.length() < 3 || batchName.length() > 150) {
                    result.addError(rowNumber, "Tên đợt SX", batchName, "Tên đợt sản xuất phải từ 3 đến 150 ký tự.");
                    rowHasError = true;
                }

                // 2. Kiểm tra BOM hợp lệ (khớp dòng máy & phiên bản)
                String bomKey = prodName.toLowerCase().trim() + "#" + bomVer.toLowerCase().trim();
                Map<String, Object> bomInfo = bomMap.get(bomKey);
                if (prodName.isEmpty()) {
                    result.addError(rowNumber, "Tên dòng máy Laptop", "", "Tên dòng máy Laptop không được để trống.");
                    rowHasError = true;
                } else if (bomVer.isEmpty()) {
                    result.addError(rowNumber, "Phiên bản BOM", "", "Phiên bản BOM không được để trống.");
                    rowHasError = true;
                } else if (bomInfo == null) {
                    result.addError(rowNumber, "Công thức BOM", prodName + " / " + bomVer,
                            "Không tìm thấy công thức BOM hiệu lực tương ứng. Vui lòng xem Sheet 'DANH_MUC_LAPTOP_BOM'.");
                    rowHasError = true;
                }

                // 3. Số lượng cần ráp
                int qty = 0;
                if (qtyStr.isEmpty()) {
                    result.addError(rowNumber, "Số lượng cần ráp", "", "Số lượng không được để trống.");
                    rowHasError = true;
                } else {
                    try {
                        qty = Integer.parseInt(qtyStr.replace(".", "").replace(",", "").trim());
                        if (qty <= 0) {
                            result.addError(rowNumber, "Số lượng cần ráp", qtyStr, "Số lượng phải lớn hơn 0.");
                            rowHasError = true;
                        }
                    } catch (NumberFormatException e) {
                        result.addError(rowNumber, "Số lượng cần ráp", qtyStr, "Số lượng phải là số nguyên.");
                        rowHasError = true;
                    }
                }

                // 4. Hạn hoàn thành
                LocalDate deadline = null;
                if (deadlineStr.isEmpty()) {
                    result.addError(rowNumber, "Hạn hoàn thành", "", "Hạn hoàn thành không được để trống.");
                    rowHasError = true;
                } else {
                    try {
                        deadline = LocalDate.parse(deadlineStr.trim(), DATE_FORMATTER);
                    } catch (DateTimeParseException e1) {
                        try {
                            deadline = LocalDate.parse(deadlineStr.trim());
                        } catch (DateTimeParseException e2) {
                            result.addError(rowNumber, "Hạn hoàn thành", deadlineStr, "Hạn hoàn thành không đúng định dạng dd/MM/yyyy (VD: 31/12/2026).");
                            rowHasError = true;
                        }
                    }
                }

                // 5. Nhân viên phụ trách
                Integer assigneeId = defaultEmployeeId;
                String finalAssigneeName = defaultEmployeeName;
                if (!assigneeInput.isEmpty()) {
                    String normEmp = assigneeInput.toLowerCase().trim();
                    if (empMap.containsKey(normEmp)) {
                        assigneeId = empMap.get(normEmp);
                        finalAssigneeName = empRealNames.get(normEmp);
                    } else {
                        result.addError(rowNumber, "Nhân viên phụ trách", assigneeInput, "Không tìm thấy nhân viên xưởng này. Vui lòng xem Sheet 'DANH_SACH_NHAN_VIEN_XUONG'.");
                        rowHasError = true;
                    }
                }

                if (!rowHasError && bomInfo != null) {
                    ProductionOrderExcelDto dto = new ProductionOrderExcelDto();
                    dto.setRowNumber(rowNumber);
                    dto.setBatchName(batchName.trim());
                    dto.setProductName((String) bomInfo.get("tensp"));
                    dto.setProductId(((Number) bomInfo.get("masp")).intValue());
                    dto.setBomVersion((String) bomInfo.get("phienban"));
                    dto.setBomId(((Number) bomInfo.get("mabom")).longValue());
                    dto.setQuantity(qty);
                    dto.setDeadline(deadline);
                    dto.setDeadlineStr(deadline != null ? deadline.format(DATE_FORMATTER) : "");
                    dto.setAssigneeId(assigneeId);
                    dto.setAssigneeName(finalAssigneeName);
                    dto.setNote(note);
                    dto.setAction("TAO_YCSX");

                    result.getValidRows().add(dto);
                }
            }
            result.setTotalRows(totalProcessed);
        }

        return result;
    }

    /**
     * 4. XÁC NHẬN NẠP HÀNG LOẠT YÊU CẦU SẢN XUẤT VÀO CSDL
     */
    @Transactional
    public int confirmImport(List<ProductionOrderExcelDto> dtos, String creatorEmail) {
        if (dtos == null || dtos.isEmpty()) {
            return 0;
        }

        // 1. Xác định ID người tạo từ email
        Integer creator = jdbc.queryForObject(
                "SELECT manv FROM taikhoan WHERE email = ?",
                Integer.class,
                creatorEmail
        );
        if (creator == null) {
            creator = 1; // Fallback
        }

        // 2. Gom nhóm theo Tên đợt sản xuất
        Map<String, List<ProductionOrderExcelDto>> batches = new LinkedHashMap<>();
        for (ProductionOrderExcelDto dto : dtos) {
            batches.computeIfAbsent(dto.getBatchName(), k -> new ArrayList<>()).add(dto);
        }

        int totalOrdersCreated = 0;

        for (Map.Entry<String, List<ProductionOrderExcelDto>> entry : batches.entrySet()) {
            String batchName = entry.getKey();
            List<ProductionOrderExcelDto> orderList = entry.getValue();

            // Tạo mã đợt tự động duy nhất
            String batchCode = "DOT-EXCEL-" + System.currentTimeMillis() % 1000000;

            KeyHolder keyHolder = new GeneratedKeyHolder();
            final Integer finalCreator = creator;
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO dot_san_xuat (madot_code, tendot, ghichu, nguoitao) VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, batchCode);
                ps.setString(2, batchName);
                ps.setString(3, "Nạp tự động từ tệp Excel đơn hàng kinh doanh");
                ps.setInt(4, finalCreator);
                return ps;
            }, keyHolder);

            long madot = Objects.requireNonNull(keyHolder.getKey()).longValue();

            // Thêm từng yêu cầu sản xuất con
            for (ProductionOrderExcelDto order : orderList) {
                jdbc.update(
                        """
                        INSERT INTO yeucausanxuat
                        (madot, masp, mabom, soluongyeucau, soluonghoanthanh, hanhoanthanh, nguoitao, nguoiphutrach, trangthai, ghichu)
                        VALUES (?, ?, ?, ?, 0, ?, ?, ?, 'MOI_TAO', ?)
                        """,
                        madot,
                        order.getProductId(),
                        order.getBomId(),
                        order.getQuantity(),
                        Date.valueOf(order.getDeadline()),
                        creator,
                        order.getAssigneeId(),
                        order.getNote()
                );
                totalOrdersCreated++;
            }
        }

        return totalOrdersCreated;
    }
}
