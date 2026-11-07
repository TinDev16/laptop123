package com.example.demo.excel.service;

import com.example.demo.excel.core.ExcelCoreUtils;
import com.example.demo.excel.core.ExcelSecurityValidator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ProductionWarehouseExcelService {

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * 1. XUẤT EXCEL: TỒN KHO NGUYÊN VẬT LIỆU NƠI SẢN XUẤT
     */
    public byte[] exportMaterials(List<Map<String, Object>> materials) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("NVL_NOI_SAN_XUAT");

            CellStyle titleStyle = createTitleStyle(workbook, IndexedColors.DARK_BLUE.getIndex());
            CellStyle metaStyle = createMetaStyle(workbook);
            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);
            CellStyle numberStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0.00");
            CellStyle currencyStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0");
            CellStyle subtotalStyle = createSubtotalStyle(workbook);

            CellStyle centerDataStyle = workbook.createCellStyle();
            centerDataStyle.cloneStyleFrom(dataStyle);
            centerDataStyle.setAlignment(HorizontalAlignment.CENTER);

            // Title Banner
            Row r0 = sheet.createRow(0);
            r0.setHeightInPoints(32);
            Cell cTitle = r0.createCell(0);
            cTitle.setCellValue("BÁO CÁO TỒN KHO NGUYÊN VẬT LIỆU NƠI SẢN XUẤT");
            cTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Thời điểm xuất báo cáo: " + LocalDateTime.now().format(DATE_TIME_FMT));
            r1.getCell(0).setCellStyle(metaStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 8));

            String[] headers = {
                    "STT", "Phân loại NVL", "Mã linh kiện", "Tên nguyên vật liệu",
                    "Đơn vị tính", "Số lượng tồn tại xưởng", "Đơn giá nhập (VNĐ)",
                    "Thành tiền tồn kho (VNĐ)", "Trạng thái"
            };

            Row rH = sheet.createRow(3);
            rH.setHeightInPoints(26);
            for (int i = 0; i < headers.length; i++) {
                Cell c = rH.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx = 4;
            int stt = 1;
            double totalQty = 0;
            double totalValue = 0;

            if (materials == null || materials.isEmpty()) {
                Row emptyRow = sheet.createRow(rowIdx++);
                emptyRow.setHeightInPoints(24);
                Cell cEmpty = emptyRow.createCell(0);
                cEmpty.setCellValue("Chưa có nguyên vật liệu trong kho nơi sản xuất.");
                cEmpty.setCellStyle(centerDataStyle);
                sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, headers.length - 1));
            } else {
                for (Map<String, Object> m : materials) {
                    Row r = sheet.createRow(rowIdx++);
                    r.setHeightInPoints(22);

                    r.createCell(0).setCellValue(stt++);
                    r.getCell(0).setCellStyle(centerDataStyle);

                    r.createCell(1).setCellValue(safeStr(m.get("tenloai")));
                    r.getCell(1).setCellStyle(dataStyle);

                    r.createCell(2).setCellValue(safeStr(m.get("manvl_code")));
                    r.getCell(2).setCellStyle(centerDataStyle);

                    r.createCell(3).setCellValue(safeStr(m.get("tennvl")));
                    r.getCell(3).setCellStyle(dataStyle);

                    r.createCell(4).setCellValue(safeStr(m.get("donvitinh")));
                    r.getCell(4).setCellStyle(centerDataStyle);

                    double qty = m.get("soluongton") != null ? ((Number) m.get("soluongton")).doubleValue() : 0.0;
                    totalQty += qty;
                    Cell cQty = r.createCell(5);
                    cQty.setCellValue(qty);
                    cQty.setCellStyle(numberStyle);

                    double price = m.get("dongianhap") != null ? ((Number) m.get("dongianhap")).doubleValue() : 0.0;
                    Cell cPrice = r.createCell(6);
                    cPrice.setCellValue(price);
                    cPrice.setCellStyle(currencyStyle);

                    double amount = qty * price;
                    totalValue += amount;
                    Cell cAmount = r.createCell(7);
                    cAmount.setCellValue(amount);
                    cAmount.setCellStyle(currencyStyle);

                    int st = m.get("trangthai_nvl") != null ? ((Number) m.get("trangthai_nvl")).intValue() : 0;
                    String stText = (st == 0) ? "Đang sử dụng" : "Ngừng sử dụng";
                    Cell cSt = r.createCell(8);
                    cSt.setCellValue(stText);
                    cSt.setCellStyle(centerDataStyle);
                }

                // Summary Row
                Row rTotal = sheet.createRow(rowIdx);
                rTotal.setHeightInPoints(24);
                Cell cTotLabel = rTotal.createCell(0);
                cTotLabel.setCellValue("TỔNG CỘNG");
                cTotLabel.setCellStyle(subtotalStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 4));

                for (int i = 1; i <= 4; i++) {
                    rTotal.createCell(i).setCellStyle(subtotalStyle);
                }

                Cell cTotQty = rTotal.createCell(5);
                cTotQty.setCellValue(totalQty);
                cTotQty.setCellStyle(subtotalStyle);

                Cell cEmpty = rTotal.createCell(6);
                cEmpty.setCellValue("");
                cEmpty.setCellStyle(subtotalStyle);

                Cell cTotValue = rTotal.createCell(7);
                cTotValue.setCellValue(totalValue);
                cTotValue.setCellStyle(subtotalStyle);

                Cell cTotEnd = rTotal.createCell(8);
                cTotEnd.setCellValue("");
                cTotEnd.setCellStyle(subtotalStyle);
            }

            ExcelCoreUtils.autoSizeColumns(sheet, headers.length);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 2. XUẤT EXCEL: TỒN KHO THÀNH PHẨM NƠI SẢN XUẤT
     * - Sheet 1: Chi tiết theo từng phiên bản BOM (đầy đủ mọi cột, không bỏ sót dòng tồn kho = 0)
     * - Sheet 2: Tổng hợp theo từng dòng sản phẩm Laptop (giống giao diện bảng chính)
     */
    public byte[] exportProducts(List<Map<String, Object>> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle titleStyle = createTitleStyle(workbook, IndexedColors.DARK_BLUE.getIndex());
            CellStyle metaStyle = createMetaStyle(workbook);
            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);
            CellStyle numberStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0");
            CellStyle decimalStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0.00");
            CellStyle currencyStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0");
            CellStyle subtotalStyle = createSubtotalStyle(workbook);

            CellStyle centerDataStyle = workbook.createCellStyle();
            centerDataStyle.cloneStyleFrom(dataStyle);
            centerDataStyle.setAlignment(HorizontalAlignment.CENTER);

            // =========================================================
            // SHEET 1: CHI TIẾT THEO PHIÊN BẢN BOM
            // =========================================================
            Sheet sheet1 = workbook.createSheet("CHI_TIET_THEO_BOM");

            Row r0 = sheet1.createRow(0);
            r0.setHeightInPoints(32);
            Cell cTitle = r0.createCell(0);
            cTitle.setCellValue("BÁO CÁO TỒN KHO THÀNH PHẨM NƠI SẢN XUẤT - CHI TIẾT THEO BOM");
            cTitle.setCellStyle(titleStyle);
            sheet1.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));

            Row r1 = sheet1.createRow(1);
            r1.createCell(0).setCellValue("Thời điểm xuất báo cáo: " + LocalDateTime.now().format(DATE_TIME_FMT));
            r1.getCell(0).setCellStyle(metaStyle);
            sheet1.addMergedRegion(new CellRangeAddress(1, 1, 0, 10));

            String[] headers1 = {
                    "STT", "Mã SP", "Tên thành phẩm Laptop", "Đơn vị tính",
                    "Tổng tồn SP nơi SX", "Phiên bản BOM", "Trạng thái BOM",
                    "Tồn kho theo BOM", "Giá NVL định mức BOM (VNĐ)",
                    "Tổng giá trị ước tính (VNĐ)", "Trạng thái SP"
            };

            Row rH1 = sheet1.createRow(3);
            rH1.setHeightInPoints(26);
            for (int i = 0; i < headers1.length; i++) {
                Cell c = rH1.createCell(i);
                c.setCellValue(headers1[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx1 = 4;
            int stt1 = 1;
            long totalBomStock = 0;
            double totalBomValue = 0;

            if (rows == null || rows.isEmpty()) {
                Row emptyRow = sheet1.createRow(rowIdx1++);
                emptyRow.setHeightInPoints(24);
                Cell cEmpty = emptyRow.createCell(0);
                cEmpty.setCellValue("Chưa có dữ liệu thành phẩm trong kho nơi sản xuất.");
                cEmpty.setCellStyle(centerDataStyle);
                sheet1.addMergedRegion(new CellRangeAddress(4, 4, 0, headers1.length - 1));
            } else {
                for (Map<String, Object> rData : rows) {
                    Row r = sheet1.createRow(rowIdx1++);
                    r.setHeightInPoints(22);

                    // 0. STT
                    r.createCell(0).setCellValue(stt1++);
                    r.getCell(0).setCellStyle(centerDataStyle);

                    // 1. Mã SP
                    r.createCell(1).setCellValue("SP-" + rData.get("masp"));
                    r.getCell(1).setCellStyle(centerDataStyle);

                    // 2. Tên thành phẩm
                    r.createCell(2).setCellValue(safeStr(rData.get("tensp")));
                    r.getCell(2).setCellStyle(dataStyle);

                    // 3. ĐVT
                    r.createCell(3).setCellValue(safeStr(rData.get("donvitinh")));
                    r.getCell(3).setCellStyle(centerDataStyle);

                    // 4. Tổng tồn SP
                    double totalProdStock = rData.get("tong_san_pham") != null ? ((Number) rData.get("tong_san_pham")).doubleValue() : 0.0;
                    Cell cTotProd = r.createCell(4);
                    cTotProd.setCellValue(totalProdStock);
                    cTotProd.setCellStyle(decimalStyle);

                    // 5. Phiên bản BOM
                    String phienban = safeStr(rData.get("phienban"));
                    Object mabom = rData.get("mabom");
                    String bomLabel;
                    if (!phienban.isEmpty()) {
                        bomLabel = "BOM " + phienban + (mabom != null ? " (Mã: " + mabom + ")" : "");
                    } else if (mabom != null) {
                        bomLabel = "BOM #" + mabom;
                    } else {
                        bomLabel = "Chưa có BOM";
                    }
                    r.createCell(5).setCellValue(bomLabel);
                    r.getCell(5).setCellStyle(centerDataStyle);

                    // 6. Trạng thái BOM
                    String stBomRaw = safeStr(rData.get("trangthai_bom"));
                    String stBomText = "HIEU_LUC".equalsIgnoreCase(stBomRaw) ? "Hiệu lực"
                            : "NGUNG_SU_DUNG".equalsIgnoreCase(stBomRaw) ? "Ngừng sử dụng"
                            : (!stBomRaw.isEmpty() ? stBomRaw : "—");
                    r.createCell(6).setCellValue(stBomText);
                    r.getCell(6).setCellStyle(centerDataStyle);

                    // 7. Tồn kho theo BOM
                    long qty = rData.get("soluongton") != null ? ((Number) rData.get("soluongton")).longValue() : 0L;
                    totalBomStock += qty;
                    Cell cQty = r.createCell(7);
                    cQty.setCellValue(qty);
                    cQty.setCellStyle(numberStyle);

                    // 8. Giá NVL định mức BOM
                    double bomPrice = rData.get("tong_gia_nvl") != null ? ((Number) rData.get("tong_gia_nvl")).doubleValue() : 0.0;
                    Cell cBomPrice = r.createCell(8);
                    cBomPrice.setCellValue(bomPrice);
                    cBomPrice.setCellStyle(currencyStyle);

                    // 9. Tổng giá trị ước tính
                    double amount = qty * bomPrice;
                    totalBomValue += amount;
                    Cell cAmount = r.createCell(9);
                    cAmount.setCellValue(amount);
                    cAmount.setCellStyle(currencyStyle);

                    // 10. Trạng thái SP
                    int st = rData.get("trangthai_sp") != null ? ((Number) rData.get("trangthai_sp")).intValue() : 0;
                    String stText = (st == 0) ? "Đang bán" : "Đã ngừng bán";
                    Cell cSt = r.createCell(10);
                    cSt.setCellValue(stText);
                    cSt.setCellStyle(centerDataStyle);
                }

                // Summary Row Sheet 1
                Row rTotal1 = sheet1.createRow(rowIdx1);
                rTotal1.setHeightInPoints(24);
                Cell cTotLabel1 = rTotal1.createCell(0);
                cTotLabel1.setCellValue("TỔNG CỘNG TỒN KHO THEO BOM");
                cTotLabel1.setCellStyle(subtotalStyle);
                sheet1.addMergedRegion(new CellRangeAddress(rowIdx1, rowIdx1, 0, 6));

                for (int i = 1; i <= 6; i++) {
                    rTotal1.createCell(i).setCellStyle(subtotalStyle);
                }

                Cell cTotQty1 = rTotal1.createCell(7);
                cTotQty1.setCellValue(totalBomStock);
                cTotQty1.setCellStyle(subtotalStyle);

                Cell cEmpty1 = rTotal1.createCell(8);
                cEmpty1.setCellValue("");
                cEmpty1.setCellStyle(subtotalStyle);

                Cell cTotVal1 = rTotal1.createCell(9);
                cTotVal1.setCellValue(totalBomValue);
                cTotVal1.setCellStyle(subtotalStyle);

                Cell cTotEnd1 = rTotal1.createCell(10);
                cTotEnd1.setCellValue("");
                cTotEnd1.setCellStyle(subtotalStyle);
            }

            ExcelCoreUtils.autoSizeColumns(sheet1, headers1.length);

            // =========================================================
            // SHEET 2: TỔNG HỢP THEO TỪNG DÒNG SẢN PHẨM (TƯƠNG TỰ GIAO DIỆN WEB)
            // =========================================================
            Sheet sheet2 = workbook.createSheet("TONG_HOP_THEO_SAN_PHAM");

            Row r0_2 = sheet2.createRow(0);
            r0_2.setHeightInPoints(32);
            Cell cTitle2 = r0_2.createCell(0);
            cTitle2.setCellValue("TỔNG HỢP TỒN KHO THÀNH PHẨM TẠI NƠI SẢN XUẤT");
            cTitle2.setCellStyle(titleStyle);
            sheet2.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            Row r1_2 = sheet2.createRow(1);
            r1_2.createCell(0).setCellValue("Thời điểm xuất báo cáo: " + LocalDateTime.now().format(DATE_TIME_FMT));
            r1_2.getCell(0).setCellStyle(metaStyle);
            sheet2.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

            String[] headers2 = {
                    "STT", "Mã SP", "Tên thành phẩm Laptop", "Đơn vị tính",
                    "Tổng tồn kho nơi sản xuất", "Số phiên bản BOM", "Trạng thái SP"
            };

            Row rH2 = sheet2.createRow(3);
            rH2.setHeightInPoints(26);
            for (int i = 0; i < headers2.length; i++) {
                Cell c = rH2.createCell(i);
                c.setCellValue(headers2[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx2 = 4;
            int stt2 = 1;
            double grandTotalProductStock = 0;

            if (rows == null || rows.isEmpty()) {
                Row emptyRow2 = sheet2.createRow(rowIdx2++);
                emptyRow2.setHeightInPoints(24);
                Cell cEmpty = emptyRow2.createCell(0);
                cEmpty.setCellValue("Chưa có dữ liệu thành phẩm trong kho nơi sản xuất.");
                cEmpty.setCellStyle(centerDataStyle);
                sheet2.addMergedRegion(new CellRangeAddress(4, 4, 0, headers2.length - 1));
            } else {
                // Gom nhóm theo masp
                Map<Object, List<Map<String, Object>>> groupedByProduct = new LinkedHashMap<>();
                for (Map<String, Object> rData : rows) {
                    Object masp = rData.get("masp");
                    groupedByProduct.computeIfAbsent(masp, k -> new ArrayList<>()).add(rData);
                }

                for (Map.Entry<Object, List<Map<String, Object>>> entry : groupedByProduct.entrySet()) {
                    List<Map<String, Object>> pRows = entry.getValue();
                    Map<String, Object> firstRow = pRows.get(0);

                    Row r = sheet2.createRow(rowIdx2++);
                    r.setHeightInPoints(22);

                    r.createCell(0).setCellValue(stt2++);
                    r.getCell(0).setCellStyle(centerDataStyle);

                    r.createCell(1).setCellValue("SP-" + firstRow.get("masp"));
                    r.getCell(1).setCellStyle(centerDataStyle);

                    r.createCell(2).setCellValue(safeStr(firstRow.get("tensp")));
                    r.getCell(2).setCellStyle(dataStyle);

                    r.createCell(3).setCellValue(safeStr(firstRow.get("donvitinh")));
                    r.getCell(3).setCellStyle(centerDataStyle);

                    double prodStock = firstRow.get("tong_san_pham") != null ? ((Number) firstRow.get("tong_san_pham")).doubleValue() : 0.0;
                    grandTotalProductStock += prodStock;
                    Cell cStock = r.createCell(4);
                    cStock.setCellValue(prodStock);
                    cStock.setCellStyle(decimalStyle);

                    // Đếm số phiên bản BOM distinct
                    Set<Object> uniqueBoms = new HashSet<>();
                    for (Map<String, Object> pr : pRows) {
                        if (pr.get("mabom") != null) {
                            uniqueBoms.add(pr.get("mabom"));
                        }
                    }
                    r.createCell(5).setCellValue(uniqueBoms.size() + " phiên bản");
                    r.getCell(5).setCellStyle(centerDataStyle);

                    int st = firstRow.get("trangthai_sp") != null ? ((Number) firstRow.get("trangthai_sp")).intValue() : 0;
                    String stText = (st == 0) ? "Đang bán" : "Đã ngừng bán";
                    Cell cSt = r.createCell(6);
                    cSt.setCellValue(stText);
                    cSt.setCellStyle(centerDataStyle);
                }

                // Summary Row Sheet 2
                Row rTotal2 = sheet2.createRow(rowIdx2);
                rTotal2.setHeightInPoints(24);
                Cell cTotLabel2 = rTotal2.createCell(0);
                cTotLabel2.setCellValue("TỔNG TỒN TOÀN XƯỞNG SẢN XUẤT");
                cTotLabel2.setCellStyle(subtotalStyle);
                sheet2.addMergedRegion(new CellRangeAddress(rowIdx2, rowIdx2, 0, 3));

                for (int i = 1; i <= 3; i++) {
                    rTotal2.createCell(i).setCellStyle(subtotalStyle);
                }

                Cell cTotQty2 = rTotal2.createCell(4);
                cTotQty2.setCellValue(grandTotalProductStock);
                cTotQty2.setCellStyle(subtotalStyle);

                Cell cEmpty2 = rTotal2.createCell(5);
                cEmpty2.setCellValue("");
                cEmpty2.setCellStyle(subtotalStyle);

                Cell cTotEnd2 = rTotal2.createCell(6);
                cTotEnd2.setCellValue("");
                cTotEnd2.setCellStyle(subtotalStyle);
            }

            ExcelCoreUtils.autoSizeColumns(sheet2, headers2.length);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 3. XUẤT EXCEL: KHO LỖI SẢN XUẤT & PHẾ PHẨM (KÈM LÝ DO LỖI)
     */
    public byte[] exportDefects(List<Map<String, Object>> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("KHO_LOI_SAN_XUAT");

            CellStyle titleStyle = createTitleStyle(workbook, IndexedColors.DARK_RED.getIndex());
            CellStyle metaStyle = createMetaStyle(workbook);
            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);
            CellStyle numberStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0");
            CellStyle currencyStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0");
            CellStyle subtotalStyle = createSubtotalStyle(workbook);

            CellStyle centerDataStyle = workbook.createCellStyle();
            centerDataStyle.cloneStyleFrom(dataStyle);
            centerDataStyle.setAlignment(HorizontalAlignment.CENTER);

            // Title Banner
            Row r0 = sheet.createRow(0);
            r0.setHeightInPoints(32);
            Cell cTitle = r0.createCell(0);
            cTitle.setCellValue("BÁO CÁO THỐNG KÊ SẢN PHẨM LỖI TẠI NƠI SẢN XUẤT");
            cTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Thời điểm xuất báo cáo: " + LocalDateTime.now().format(DATE_TIME_FMT));
            r1.getCell(0).setCellStyle(metaStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 8));

            String[] headers = {
                    "STT", "Mã SP", "Tên thành phẩm Laptop", "Phiên bản BOM",
                    "Đơn vị tính", "Số lượng lỗi ghi nhận", "Lý do ghi nhận lỗi",
                    "Ước tính giá trị NVL thiệt hại (VNĐ)", "Trạng thái SP"
            };

            Row rH = sheet.createRow(3);
            rH.setHeightInPoints(26);
            for (int i = 0; i < headers.length; i++) {
                Cell c = rH.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx = 4;
            int stt = 1;
            long totalDefects = 0;
            double totalDamage = 0;

            if (rows == null || rows.isEmpty()) {
                Row emptyRow = sheet.createRow(rowIdx++);
                emptyRow.setHeightInPoints(24);
                Cell cEmpty = emptyRow.createCell(0);
                cEmpty.setCellValue("Chưa có sản phẩm lỗi ghi nhận trong kho sản xuất.");
                cEmpty.setCellStyle(centerDataStyle);
                sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, headers.length - 1));
            } else {
                for (Map<String, Object> rData : rows) {
                    Row r = sheet.createRow(rowIdx++);
                    r.setHeightInPoints(22);

                    r.createCell(0).setCellValue(stt++);
                    r.getCell(0).setCellStyle(centerDataStyle);

                    r.createCell(1).setCellValue("SP-" + rData.get("masp"));
                    r.getCell(1).setCellStyle(centerDataStyle);

                    r.createCell(2).setCellValue(safeStr(rData.get("tensp")));
                    r.getCell(2).setCellStyle(dataStyle);

                    String phienban = safeStr(rData.get("phienban"));
                    Object mabom = rData.get("mabom");
                    String bomLabel = !phienban.isEmpty() ? "BOM " + phienban : (mabom != null ? "BOM #" + mabom : "Mặc định");
                    r.createCell(3).setCellValue(bomLabel);
                    r.getCell(3).setCellStyle(centerDataStyle);

                    r.createCell(4).setCellValue(safeStr(rData.get("donvitinh")));
                    r.getCell(4).setCellStyle(centerDataStyle);

                    long qty = rData.get("soluongton") != null ? ((Number) rData.get("soluongton")).longValue() : 0L;
                    totalDefects += qty;
                    Cell cQty = r.createCell(5);
                    cQty.setCellValue(qty);
                    cQty.setCellStyle(numberStyle);

                    String reason = safeStr(rData.get("ly_do_loi"));
                    r.createCell(6).setCellValue(reason.isEmpty() ? "Lỗi kiểm tra chất lượng (QC)" : reason);
                    r.getCell(6).setCellStyle(dataStyle);

                    double bomPrice = rData.get("tong_gia_nvl") != null ? ((Number) rData.get("tong_gia_nvl")).doubleValue() : 0.0;
                    double damage = qty * bomPrice;
                    totalDamage += damage;
                    Cell cDamage = r.createCell(7);
                    cDamage.setCellValue(damage);
                    cDamage.setCellStyle(currencyStyle);

                    int st = rData.get("trangthai_sp") != null ? ((Number) rData.get("trangthai_sp")).intValue() : 0;
                    String stText = (st == 0) ? "Đang bán" : "Đã ngừng bán";
                    Cell cSt = r.createCell(8);
                    cSt.setCellValue(stText);
                    cSt.setCellStyle(centerDataStyle);
                }

                // Summary Row
                Row rTotal = sheet.createRow(rowIdx);
                rTotal.setHeightInPoints(24);
                Cell cTotLabel = rTotal.createCell(0);
                cTotLabel.setCellValue("TỔNG CỘNG LỖI");
                cTotLabel.setCellStyle(subtotalStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 4));

                for (int i = 1; i <= 4; i++) {
                    rTotal.createCell(i).setCellStyle(subtotalStyle);
                }

                Cell cTotQty = rTotal.createCell(5);
                cTotQty.setCellValue(totalDefects);
                cTotQty.setCellStyle(subtotalStyle);

                Cell cEmptyReason = rTotal.createCell(6);
                cEmptyReason.setCellValue("");
                cEmptyReason.setCellStyle(subtotalStyle);

                Cell cTotDamage = rTotal.createCell(7);
                cTotDamage.setCellValue(totalDamage);
                cTotDamage.setCellStyle(subtotalStyle);

                Cell cTotEnd = rTotal.createCell(8);
                cTotEnd.setCellValue("");
                cTotEnd.setCellStyle(subtotalStyle);
            }

            ExcelCoreUtils.autoSizeColumns(sheet, headers.length);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private CellStyle createTitleStyle(Workbook wb, short bgColor) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 13);
        style.setFont(font);
        style.setFillForegroundColor(bgColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createMetaStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setItalic(true);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        return style;
    }

    private CellStyle createSubtotalStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.DOUBLE);
        return style;
    }

    private String safeStr(Object val) {
        if (val == null) return "";
        return ExcelSecurityValidator.sanitizeForFormulaInjection(val.toString().trim());
    }
}
