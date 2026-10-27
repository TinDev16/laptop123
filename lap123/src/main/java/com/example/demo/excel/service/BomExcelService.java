package com.example.demo.excel.service;

import com.example.demo.excel.core.ExcelCoreUtils;
import com.example.demo.excel.core.ExcelImportResult;
import com.example.demo.excel.core.ExcelSecurityValidator;
import com.example.demo.excel.dto.BomExcelDto;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Service
public class BomExcelService {

    private final JdbcTemplate jdbc;

    public BomExcelService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 1. TẠO FILE MẪU EXCEL ĐỊNH MỨC KỸ THUẬT (BOM)
     */
    public byte[] generateTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Sheet 1: Mẫu nhập liệu
            Sheet sheet = workbook.createSheet("DINH_MUC_BOM");

            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle instructionStyle = ExcelCoreUtils.createInstructionStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);
            CellStyle numberStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0.00");
            CellStyle percentStyle = ExcelCoreUtils.createNumberStyle(workbook, "0.00\"%\"");

            String[] headers = {
                    "STT",
                    "Tên dòng máy Laptop (*)",
                    "Phiên bản BOM (*)",
                    "Mã linh kiện NVL (*)",
                    "Tên linh kiện (Tham khảo)",
                    "Định mức / 1 máy (*)",
                    "Tỷ lệ hao hụt (%)",
                    "Ghi chú dòng linh kiện",
                    "Ghi chú phiên bản BOM"
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
            instCell.setCellValue("HƯỚNG DẪN: Các cột dấu (*) là bắt buộc. 'Tên dòng máy Laptop' và 'Mã linh kiện NVL' tra cứu từ Sheet tham khảo. Không sửa tiêu đề cột.");
            instCell.setCellStyle(instructionStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, headers.length - 1));

            // Dòng mẫu 1
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue(1);
            r2.createCell(1).setCellValue("Laptop ASUS ROG Strix G16");
            r2.createCell(2).setCellValue("2026.1");
            r2.createCell(3).setCellValue("NVL-CPU-002");
            r2.createCell(4).setCellValue("CPU Intel Core i7 5th");
            Cell r2c5 = r2.createCell(5);
            r2c5.setCellValue(1.0);
            r2c5.setCellStyle(numberStyle);
            Cell r2c6 = r2.createCell(6);
            r2c6.setCellValue(0.0);
            r2c6.setCellStyle(percentStyle);
            r2.createCell(7).setCellValue("Lắp khe CPU socket chính");
            r2.createCell(8).setCellValue("Phiên bản tiêu chuẩn Gaming 2026");

            // Dòng mẫu 2
            Row r3 = sheet.createRow(3);
            r3.createCell(0).setCellValue(2);
            r3.createCell(1).setCellValue("Laptop ASUS ROG Strix G16");
            r3.createCell(2).setCellValue("2026.1");
            r3.createCell(3).setCellValue("NVL-RAM-001");
            r3.createCell(4).setCellValue("Kingston 8GB DDR4 3200MHz SO-DIMM");
            Cell r3c5 = r3.createCell(5);
            r3c5.setCellValue(2.0);
            r3c5.setCellStyle(numberStyle);
            Cell r3c6 = r3.createCell(6);
            r3c6.setCellValue(1.5);
            r3c6.setCellStyle(percentStyle);
            r3.createCell(7).setCellValue("2 thanh kênh đôi Dual Channel");
            r3.createCell(8).setCellValue("Phiên bản tiêu chuẩn Gaming 2026");

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
            // SHEET 2: DANH MỤC LAPTOP (TRA CỨU)
            // ====================================================
            Sheet prodSheet = workbook.createSheet("DANH_MUC_LAPTOP");
            Row phRow = prodSheet.createRow(0);
            phRow.setHeightInPoints(24);
            String[] prodHeaders = {"Mã máy", "Tên dòng máy Laptop", "Thương hiệu"};
            for (int i = 0; i < prodHeaders.length; i++) {
                Cell cell = phRow.createCell(i);
                cell.setCellValue(prodHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Map<String, Object>> prods = jdbc.queryForList(
                    "SELECT s.masp, s.tensp, COALESCE(t.tenthuonghieu, 'Khác') AS tenthuonghieu " +
                    "FROM sanpham s LEFT JOIN thuonghieu t ON s.mathuonghieu = t.mathuonghieu " +
                    "ORDER BY s.tensp ASC"
            );
            int pRowIdx = 1;
            for (Map<String, Object> p : prods) {
                Row row = prodSheet.createRow(pRowIdx++);
                Cell c0 = row.createCell(0);
                c0.setCellValue(((Number) p.get("masp")).intValue());
                c0.setCellStyle(dataStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue((String) p.get("tensp"));
                c1.setCellStyle(dataStyle);

                Cell c2 = row.createCell(2);
                c2.setCellValue((String) p.get("tenthuonghieu"));
                c2.setCellStyle(dataStyle);
            }
            ExcelCoreUtils.autoSizeColumns(prodSheet, prodHeaders.length);

            // ====================================================
            // SHEET 3: DANH MỤC LINH KIỆN NVL (TRA CỨU)
            // ====================================================
            Sheet matSheet = workbook.createSheet("DANH_MUC_LINH_KIEN_NVL");
            Row mhRow = matSheet.createRow(0);
            mhRow.setHeightInPoints(24);
            String[] matHeaders = {"Mã linh kiện NVL", "Tên linh kiện", "Loại NVL", "ĐVT", "Đơn giá nhập hiện tại"};
            for (int i = 0; i < matHeaders.length; i++) {
                Cell cell = mhRow.createCell(i);
                cell.setCellValue(matHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Map<String, Object>> mats = jdbc.queryForList(
                    "SELECT n.manvl_code, n.tennvl, COALESCE(l.tenloai, 'Chưa phân loại') AS tenloai, " +
                    "n.donvitinh, n.dongianhap " +
                    "FROM nguyenvatlieu n LEFT JOIN loai_nguyen_vat_lieu l ON n.maloai = l.maloai " +
                    "WHERE n.trangthai = 1 ORDER BY l.tenloai ASC, n.tennvl ASC"
            );
            int mRowIdx = 1;
            CellStyle currencyStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0 ₫");
            for (Map<String, Object> m : mats) {
                Row row = matSheet.createRow(mRowIdx++);
                Cell c0 = row.createCell(0);
                c0.setCellValue((String) m.get("manvl_code"));
                c0.setCellStyle(dataStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue((String) m.get("tennvl"));
                c1.setCellStyle(dataStyle);

                Cell c2 = row.createCell(2);
                c2.setCellValue((String) m.get("tenloai"));
                c2.setCellStyle(dataStyle);

                Cell c3 = row.createCell(3);
                c3.setCellValue((String) m.get("donvitinh"));
                c3.setCellStyle(dataStyle);

                Cell c4 = row.createCell(4);
                BigDecimal price = m.get("dongianhap") != null ? (BigDecimal) m.get("dongianhap") : BigDecimal.ZERO;
                c4.setCellValue(price.doubleValue());
                c4.setCellStyle(currencyStyle);
            }
            ExcelCoreUtils.autoSizeColumns(matSheet, matHeaders.length);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 2. XUẤT DANH SÁCH ĐỊNH MỨC KỸ THUẬT BOM RA EXCEL
     * (CHIA NHIỀU BẢNG TRÊN 1 FILE THEO TỪNG DÒNG MÁY & PHIÊN BẢN)
     */
    public byte[] exportBom() throws IOException {
        String sql = """
            SELECT
                b.mabom,
                b.phienban,
                b.trangthai,
                b.ghichu AS bom_ghichu,
                s.masp,
                s.tensp,
                c.id AS chitiet_id,
                n.manvl_code,
                n.tennvl,
                COALESCE(l.tenloai, 'Chưa phân loại') AS tenloai,
                n.donvitinh,
                c.soluong_dinhmuc,
                c.tyle_haohut,
                n.dongianhap,
                c.ghichu AS chitiet_ghichu
            FROM bom b
            JOIN sanpham s ON s.masp = b.masp
            LEFT JOIN bom_chitiet c ON c.mabom = b.mabom
            LEFT JOIN nguyenvatlieu n ON n.manvl = c.manvl
            LEFT JOIN loai_nguyen_vat_lieu l ON l.maloai = n.maloai
            ORDER BY s.tensp ASC, b.phienban ASC, c.id ASC
        """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql);

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);
            CellStyle numberStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0.00");
            CellStyle percentStyle = ExcelCoreUtils.createNumberStyle(workbook, "0.00\"%\"");
            CellStyle currencyStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0 ₫");

            // Style Banner nhóm BOM
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

            // Style Subtotal dòng tổng
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
            // SHEET 1: CHIA NHIỀU BẢNG THEO DÒNG LAPTOP & PHIÊN BẢN BOM
            // ====================================================
            Sheet groupSheet = workbook.createSheet("THEO_PHIEN_BAN_BOM");

            // Gom nhóm theo key "tensp | phienban | mabom"
            Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
            for (Map<String, Object> r : rows) {
                String tensp = (String) r.get("tensp");
                String phienban = (String) r.get("phienban");
                String trangthai = (String) r.get("trangthai");
                String key = tensp + " — Phiên bản " + phienban + " (" + ("HIEU_LUC".equals(trangthai) ? "Hiệu lực" : "Ngừng sử dụng") + ")";
                groups.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
            }

            String[] tableHeaders = {
                    "STT",
                    "Mã linh kiện",
                    "Tên linh kiện NVL",
                    "Phân loại kỹ thuật",
                    "Định mức / 1 máy",
                    "ĐVT",
                    "Hao hụt (%)",
                    "Đơn giá nhập NVL",
                    "Thành tiền linh kiện",
                    "Ghi chú vị trí lắp ráp"
            };

            int curRow = 0;
            for (Map.Entry<String, List<Map<String, Object>>> entry : groups.entrySet()) {
                String groupTitle = entry.getKey();
                List<Map<String, Object>> items = entry.getValue();

                // Lọc bỏ các dòng null do LEFT JOIN (khi BOM chưa có chi tiết)
                List<Map<String, Object>> validItems = new ArrayList<>();
                for (Map<String, Object> it : items) {
                    if (it.get("chitiet_id") != null) {
                        validItems.add(it);
                    }
                }

                // 1. Banner nhóm
                Row bannerRow = groupSheet.createRow(curRow);
                bannerRow.setHeightInPoints(28);
                Cell bannerCell = bannerRow.createCell(0);
                bannerCell.setCellValue("⚙️ CÔNG THỨC BOM: " + groupTitle + " [" + validItems.size() + " linh kiện]");
                bannerCell.setCellStyle(bannerStyle);
                for (int c = 1; c < tableHeaders.length; c++) {
                    Cell bc = bannerRow.createCell(c);
                    bc.setCellStyle(bannerStyle);
                }
                groupSheet.addMergedRegion(new CellRangeAddress(curRow, curRow, 0, tableHeaders.length - 1));
                curRow++;

                // 2. Tiêu đề cột của bảng này
                Row thRow = groupSheet.createRow(curRow);
                thRow.setHeightInPoints(25);
                for (int i = 0; i < tableHeaders.length; i++) {
                    Cell c = thRow.createCell(i);
                    c.setCellValue(tableHeaders[i]);
                    c.setCellStyle(headerStyle);
                }
                curRow++;

                // 3. Dữ liệu các dòng linh kiện trong BOM
                int stt = 1;
                BigDecimal totalBomCost = BigDecimal.ZERO;

                if (validItems.isEmpty()) {
                    Row emptyRow = groupSheet.createRow(curRow);
                    Cell ec = emptyRow.createCell(0);
                    ec.setCellValue("Chưa có linh kiện nào trong phiên bản BOM này.");
                    ec.setCellStyle(dataStyle);
                    for (int c = 1; c < tableHeaders.length; c++) {
                        emptyRow.createCell(c).setCellStyle(dataStyle);
                    }
                    groupSheet.addMergedRegion(new CellRangeAddress(curRow, curRow, 0, tableHeaders.length - 1));
                    curRow++;
                } else {
                    for (Map<String, Object> it : validItems) {
                        Row dataRow = groupSheet.createRow(curRow);
                        dataRow.setHeightInPoints(20);

                        Cell c0 = dataRow.createCell(0);
                        c0.setCellValue(stt++);
                        c0.setCellStyle(dataStyle);

                        Cell c1 = dataRow.createCell(1);
                        c1.setCellValue((String) it.get("manvl_code"));
                        c1.setCellStyle(dataStyle);

                        Cell c2 = dataRow.createCell(2);
                        c2.setCellValue((String) it.get("tennvl"));
                        c2.setCellStyle(dataStyle);

                        Cell c3 = dataRow.createCell(3);
                        c3.setCellValue((String) it.get("tenloai"));
                        c3.setCellStyle(dataStyle);

                        Cell c4 = dataRow.createCell(4);
                        BigDecimal qty = it.get("soluong_dinhmuc") != null ? (BigDecimal) it.get("soluong_dinhmuc") : BigDecimal.ZERO;
                        c4.setCellValue(qty.doubleValue());
                        c4.setCellStyle(numberStyle);

                        Cell c5 = dataRow.createCell(5);
                        c5.setCellValue((String) it.get("donvitinh"));
                        c5.setCellStyle(dataStyle);

                        Cell c6 = dataRow.createCell(6);
                        BigDecimal waste = it.get("tyle_haohut") != null ? (BigDecimal) it.get("tyle_haohut") : BigDecimal.ZERO;
                        c6.setCellValue(waste.doubleValue());
                        c6.setCellStyle(percentStyle);

                        Cell c7 = dataRow.createCell(7);
                        BigDecimal price = it.get("dongianhap") != null ? (BigDecimal) it.get("dongianhap") : BigDecimal.ZERO;
                        c7.setCellValue(price.doubleValue());
                        c7.setCellStyle(currencyStyle);

                        // Thành tiền = Định mức * Đơn giá nhập
                        BigDecimal itemCost = qty.multiply(price);
                        totalBomCost = totalBomCost.add(itemCost);

                        Cell c8 = dataRow.createCell(8);
                        c8.setCellValue(itemCost.doubleValue());
                        c8.setCellStyle(currencyStyle);

                        Cell c9 = dataRow.createCell(9);
                        c9.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) it.get("chitiet_ghichu")));
                        c9.setCellStyle(dataStyle);

                        curRow++;
                    }

                    // 4. Subtotal row (Giá thành BOM 1 máy)
                    Row subtotalRow = groupSheet.createRow(curRow);
                    subtotalRow.setHeightInPoints(22);
                    Cell st0 = subtotalRow.createCell(0);
                    st0.setCellValue("Tổng chi phí linh kiện định mức / 1 máy (Giá BOM):");
                    st0.setCellStyle(subtotalText);
                    for (int c = 1; c < 8; c++) {
                        Cell sc = subtotalRow.createCell(c);
                        sc.setCellStyle(subtotalStyle);
                    }
                    groupSheet.addMergedRegion(new CellRangeAddress(curRow, curRow, 0, 7));

                    Cell stCost = subtotalRow.createCell(8);
                    stCost.setCellValue(totalBomCost.doubleValue());
                    stCost.setCellStyle(currencyStyle);

                    Cell stEnd = subtotalRow.createCell(9);
                    stEnd.setCellStyle(subtotalStyle);
                    curRow++;
                }

                // 5. Spacer
                curRow++;
            }
            ExcelCoreUtils.autoSizeColumns(groupSheet, tableHeaders.length);

            // ====================================================
            // SHEET 2: TOÀN BỘ ĐỊNH MỨC DẠNG BẢNG PHẲNG
            // ====================================================
            Sheet flatSheet = workbook.createSheet("TOAN_BO_DINH_MUC_BOM");
            String[] flatHeaders = {
                    "STT",
                    "Mã BOM",
                    "Tên dòng máy Laptop",
                    "Phiên bản BOM",
                    "Trạng thái BOM",
                    "Mã linh kiện NVL",
                    "Tên linh kiện",
                    "Loại NVL",
                    "Định mức / 1 máy",
                    "ĐVT",
                    "Hao hụt (%)",
                    "Đơn giá nhập",
                    "Thành tiền",
                    "Ghi chú linh kiện",
                    "Ghi chú phiên bản"
            };

            Row fhRow = flatSheet.createRow(0);
            fhRow.setHeightInPoints(28);
            for (int i = 0; i < flatHeaders.length; i++) {
                Cell cell = fhRow.createCell(i);
                cell.setCellValue(flatHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            int fIdx = 1;
            for (Map<String, Object> r : rows) {
                if (r.get("chitiet_id") == null) continue;

                Row row = flatSheet.createRow(fIdx);
                row.createCell(0).setCellValue(fIdx);
                row.getCell(0).setCellStyle(dataStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue(((Number) r.get("mabom")).intValue());
                c1.setCellStyle(dataStyle);

                Cell c2 = row.createCell(2);
                c2.setCellValue((String) r.get("tensp"));
                c2.setCellStyle(dataStyle);

                Cell c3 = row.createCell(3);
                c3.setCellValue((String) r.get("phienban"));
                c3.setCellStyle(dataStyle);

                Cell c4 = row.createCell(4);
                String tt = (String) r.get("trangthai");
                c4.setCellValue("HIEU_LUC".equals(tt) ? "Hiệu lực" : "Ngừng sử dụng");
                c4.setCellStyle(dataStyle);

                Cell c5 = row.createCell(5);
                c5.setCellValue((String) r.get("manvl_code"));
                c5.setCellStyle(dataStyle);

                Cell c6 = row.createCell(6);
                c6.setCellValue((String) r.get("tennvl"));
                c6.setCellStyle(dataStyle);

                Cell c7 = row.createCell(7);
                c7.setCellValue((String) r.get("tenloai"));
                c7.setCellStyle(dataStyle);

                Cell c8 = row.createCell(8);
                BigDecimal qty = r.get("soluong_dinhmuc") != null ? (BigDecimal) r.get("soluong_dinhmuc") : BigDecimal.ZERO;
                c8.setCellValue(qty.doubleValue());
                c8.setCellStyle(numberStyle);

                Cell c9 = row.createCell(9);
                c9.setCellValue((String) r.get("donvitinh"));
                c9.setCellStyle(dataStyle);

                Cell c10 = row.createCell(10);
                BigDecimal waste = r.get("tyle_haohut") != null ? (BigDecimal) r.get("tyle_haohut") : BigDecimal.ZERO;
                c10.setCellValue(waste.doubleValue());
                c10.setCellStyle(percentStyle);

                Cell c11 = row.createCell(11);
                BigDecimal price = r.get("dongianhap") != null ? (BigDecimal) r.get("dongianhap") : BigDecimal.ZERO;
                c11.setCellValue(price.doubleValue());
                c11.setCellStyle(currencyStyle);

                Cell c12 = row.createCell(12);
                c12.setCellValue(qty.multiply(price).doubleValue());
                c12.setCellStyle(currencyStyle);

                Cell c13 = row.createCell(13);
                c13.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) r.get("chitiet_ghichu")));
                c13.setCellStyle(dataStyle);

                Cell c14 = row.createCell(14);
                c14.setCellValue(ExcelSecurityValidator.sanitizeForFormulaInjection((String) r.get("bom_ghichu")));
                c14.setCellStyle(dataStyle);

                fIdx++;
            }
            ExcelCoreUtils.autoSizeColumns(flatSheet, flatHeaders.length);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 3. KIỂM TRA DỮ LIỆU & XEM TRƯỚC ĐỊNH MỨC BOM
     */
    public ExcelImportResult<BomExcelDto> validateAndPreview(InputStream is, long fileSize) throws IOException {
        ExcelSecurityValidator.validateFileSize(fileSize);

        byte[] fileBytes = is.readAllBytes();
        ExcelSecurityValidator.validateXlsxMagicBytes(new ByteArrayInputStream(fileBytes));

        ExcelImportResult<BomExcelDto> result = new ExcelImportResult<>();

        // Tra cứu danh sách laptop: tensp (chuẩn hóa chữ thường) -> masp
        List<Map<String, Object>> prodList = jdbc.queryForList("SELECT masp, LOWER(TRIM(tensp)) AS norm_name, tensp FROM sanpham");
        Map<String, Integer> prodMap = new HashMap<>();
        Map<String, String> prodRealNames = new HashMap<>();
        for (Map<String, Object> p : prodList) {
            String norm = (String) p.get("norm_name");
            prodMap.put(norm, ((Number) p.get("masp")).intValue());
            prodRealNames.put(norm, (String) p.get("tensp"));
        }

        // Tra cứu danh sách nguyên vật liệu hoạt động: manvl_code (chuẩn hóa chữ hoa) -> info
        List<Map<String, Object>> matList = jdbc.queryForList(
                "SELECT n.manvl, UPPER(TRIM(n.manvl_code)) AS code, n.tennvl, n.donvitinh, n.dongianhap, " +
                "COALESCE(l.tenloai, 'Chưa phân loại') AS tenloai " +
                "FROM nguyenvatlieu n LEFT JOIN loai_nguyen_vat_lieu l ON n.maloai = l.maloai " +
                "WHERE n.trangthai = 1"
        );
        Map<String, Map<String, Object>> matMap = new HashMap<>();
        for (Map<String, Object> m : matList) {
            matMap.put((String) m.get("code"), m);
        }

        // Theo dõi linh kiện trùng lặp trong cùng 1 BOM (productId + version + materialCode)
        Set<String> uniqueBomItems = new HashSet<>();

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

                // Kiểm tra dòng trống
                boolean empty = true;
                for (int c = 1; c <= 6; c++) {
                    if (!ExcelCoreUtils.getCellStringValue(row.getCell(c)).isEmpty()) {
                        empty = false;
                        break;
                    }
                }
                if (empty) continue;

                // Bỏ qua dòng hướng dẫn nếu có chữ HƯỚNG DẪN
                String firstCell = ExcelCoreUtils.getCellStringValue(row.getCell(0));
                String secondCell = ExcelCoreUtils.getCellStringValue(row.getCell(1));
                if (firstCell.toUpperCase().contains("HƯỚNG DẪN") || secondCell.toUpperCase().contains("HƯỚNG DẪN")) {
                    continue;
                }

                totalProcessed++;
                int rowNumber = r + 1; // 1-based
                boolean rowHasError = false;

                String prodName = ExcelCoreUtils.getCellStringValue(row.getCell(1));
                String version = ExcelCoreUtils.getCellStringValue(row.getCell(2));
                String matCode = ExcelCoreUtils.getCellStringValue(row.getCell(3)).toUpperCase();
                String inputQty = ExcelCoreUtils.getCellStringValue(row.getCell(5));
                String inputWaste = ExcelCoreUtils.getCellStringValue(row.getCell(6));
                String lineNote = ExcelCoreUtils.getCellStringValue(row.getCell(7));
                String bomNote = ExcelCoreUtils.getCellStringValue(row.getCell(8));

                // 1. Kiểm tra Tên laptop
                if (prodName.isEmpty()) {
                    result.addError(rowNumber, "Tên dòng máy Laptop", "", "Tên dòng máy Laptop không được để trống.");
                    rowHasError = true;
                } else if (!prodMap.containsKey(prodName.toLowerCase().trim())) {
                    result.addError(rowNumber, "Tên dòng máy Laptop", prodName, "Không tìm thấy dòng máy Laptop này trong hệ thống. Vui lòng xem Sheet 'DANH_MUC_LAPTOP'.");
                    rowHasError = true;
                }

                // 2. Kiểm tra Phiên bản BOM
                if (version.isEmpty()) {
                    result.addError(rowNumber, "Phiên bản BOM", "", "Phiên bản BOM không được để trống (ví dụ: 2026.1 hoặc V1.0).");
                    rowHasError = true;
                } else if (version.length() > 30) {
                    result.addError(rowNumber, "Phiên bản BOM", version, "Phiên bản BOM tối đa 30 ký tự.");
                    rowHasError = true;
                }

                // 3. Kiểm tra Mã linh kiện NVL
                if (matCode.isEmpty()) {
                    result.addError(rowNumber, "Mã linh kiện NVL", "", "Mã linh kiện NVL không được để trống.");
                    rowHasError = true;
                } else if (!matMap.containsKey(matCode)) {
                    result.addError(rowNumber, "Mã linh kiện NVL", matCode, "Mã linh kiện không tồn tại hoặc đã bị ngừng sử dụng. Vui lòng xem Sheet 'DANH_MUC_LINH_KIEN_NVL'.");
                    rowHasError = true;
                }

                // 4. Kiểm tra Định mức / 1 máy
                BigDecimal qty = null;
                if (inputQty.isEmpty()) {
                    result.addError(rowNumber, "Định mức / 1 máy", "", "Định mức linh kiện không được để trống.");
                    rowHasError = true;
                } else {
                    try {
                        String cleanQty = inputQty.replace(",", ".");
                        qty = new BigDecimal(cleanQty);
                        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                            result.addError(rowNumber, "Định mức / 1 máy", inputQty, "Định mức linh kiện phải lớn hơn 0.");
                            rowHasError = true;
                        }
                    } catch (NumberFormatException e) {
                        result.addError(rowNumber, "Định mức / 1 máy", inputQty, "Định mức phải là số hợp lệ.");
                        rowHasError = true;
                    }
                }

                // 5. Kiểm tra Tỷ lệ hao hụt (%)
                BigDecimal waste = BigDecimal.ZERO;
                if (!inputWaste.isEmpty()) {
                    try {
                        String cleanWaste = inputWaste.replace("%", "").replace(",", ".").trim();
                        waste = new BigDecimal(cleanWaste);
                        if (waste.compareTo(BigDecimal.ZERO) < 0 || waste.compareTo(new BigDecimal("100")) > 0) {
                            result.addError(rowNumber, "Tỷ lệ hao hụt", inputWaste, "Tỷ lệ hao hụt phải từ 0% đến 100%.");
                            rowHasError = true;
                        }
                    } catch (NumberFormatException e) {
                        result.addError(rowNumber, "Tỷ lệ hao hụt", inputWaste, "Tỷ lệ hao hụt phải là số.");
                        rowHasError = true;
                    }
                }

                // 6. Kiểm tra trùng lặp linh kiện trong cùng 1 BOM
                String bomItemKey = prodName.toLowerCase().trim() + "#" + version.toLowerCase().trim() + "#" + matCode;
                if (!uniqueBomItems.add(bomItemKey)) {
                    result.addError(rowNumber, "Mã linh kiện NVL", matCode, "Linh kiện bị khai báo trùng lặp cho cùng một dòng máy và phiên bản BOM.");
                    rowHasError = true;
                }

                // Nếu hợp lệ -> gom vào danh sách valid
                if (!rowHasError) {
                    BomExcelDto dto = new BomExcelDto();
                    dto.setRowNumber(rowNumber);
                    dto.setProductName(prodRealNames.get(prodName.toLowerCase().trim()));
                    dto.setProductId(prodMap.get(prodName.toLowerCase().trim()));
                    dto.setVersion(version.trim());
                    dto.setMaterialCode(matCode);

                    Map<String, Object> matInfo = matMap.get(matCode);
                    dto.setMaterialId(((Number) matInfo.get("manvl")).longValue());
                    dto.setMaterialName((String) matInfo.get("tennvl"));
                    dto.setCategoryName((String) matInfo.get("tenloai"));
                    dto.setUnit((String) matInfo.get("donvitinh"));
                    dto.setMaterialPrice((BigDecimal) matInfo.get("dongianhap"));

                    dto.setQuantity(qty);
                    dto.setWastePercentage(waste);
                    dto.setNote(lineNote);
                    dto.setAction("NAP_DINH_MUC");

                    result.getValidRows().add(dto);
                }
            }
            result.setTotalRows(totalProcessed);
        }

        return result;
    }

    /**
     * 4. XÁC NHẬN NẠP DỮ LIỆU ĐỊNH MỨC BOM VÀO CSDL
     * Toàn bộ hoặc không (All-or-Nothing @Transactional)
     */
    @Transactional
    public int confirmImport(List<BomExcelDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return 0;
        }

        // Gom nhóm theo (productId, version)
        Map<String, List<BomExcelDto>> bomGroups = new LinkedHashMap<>();
        for (BomExcelDto dto : dtos) {
            String key = dto.getProductId() + "#" + dto.getVersion().toLowerCase().trim();
            bomGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(dto);
        }

        int totalItemsInserted = 0;

        for (Map.Entry<String, List<BomExcelDto>> entry : bomGroups.entrySet()) {
            List<BomExcelDto> groupItems = entry.getValue();
            BomExcelDto sample = groupItems.get(0);
            int productId = sample.getProductId();
            String version = sample.getVersion();

            // 1. Kiểm tra BOM đã tồn tại cho (productId, version) chưa
            List<Map<String, Object>> existingBoms = jdbc.queryForList(
                    "SELECT mabom FROM bom WHERE masp = ? AND LOWER(TRIM(phienban)) = LOWER(TRIM(?))",
                    productId, version
            );

            long mabom;
            if (!existingBoms.isEmpty()) {
                mabom = ((Number) existingBoms.get(0).get("mabom")).longValue();
                // Xóa chi tiết cũ để nạp chi tiết mới chuẩn xác
                jdbc.update("DELETE FROM bom_chitiet WHERE mabom = ?", mabom);
                // Đảm bảo trạng thái BOM là hiệu lực
                jdbc.update("UPDATE bom SET trangthai = 'HIEU_LUC' WHERE mabom = ?", mabom);
            } else {
                // Tạo mới BOM header
                KeyHolder keyHolder = new GeneratedKeyHolder();
                jdbc.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO bom (masp, phienban, trangthai, ghichu) VALUES (?, ?, 'HIEU_LUC', ?)",
                            Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setInt(1, productId);
                    ps.setString(2, version);
                    ps.setString(3, "Nạp từ file Excel định mức kỹ thuật");
                    return ps;
                }, keyHolder);

                mabom = Objects.requireNonNull(keyHolder.getKey()).longValue();
            }

            // 2. Chèn các dòng chi tiết vào bom_chitiet
            for (BomExcelDto item : groupItems) {
                jdbc.update(
                        "INSERT INTO bom_chitiet (mabom, manvl, soluong_dinhmuc, tyle_haohut, ghichu) " +
                        "VALUES (?, ?, ?, ?, ?)",
                        mabom,
                        item.getMaterialId(),
                        item.getQuantity(),
                        item.getWastePercentage(),
                        item.getNote()
                );
                totalItemsInserted++;
            }
        }

        return totalItemsInserted;
    }
}
