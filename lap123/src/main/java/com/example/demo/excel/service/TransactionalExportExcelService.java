package com.example.demo.excel.service;

import com.example.demo.excel.core.ExcelCoreUtils;
import com.example.demo.excel.core.ExcelSecurityValidator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class TransactionalExportExcelService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =========================================================================
    // 1. XUẤT LỊCH SỬ KHO SẢN XUẤT
    // =========================================================================
    public byte[] exportProductionWarehouseHistory(List<Map<String, Object>> rows, LocalDate from, LocalDate to, String kind, String type) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("LICH_SU_KHO_SX");

            CellStyle titleStyle = createTitleStyle(workbook, IndexedColors.DARK_BLUE.getIndex());
            CellStyle metaStyle = createMetaStyle(workbook);
            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);
            CellStyle numberStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0");
            CellStyle subtotalStyle = createSubtotalStyle(workbook);

            // Title Banner
            Row r0 = sheet.createRow(0);
            r0.setHeightInPoints(32);
            Cell cTitle = r0.createCell(0);
            cTitle.setCellValue("BÁO CÁO LỊCH SỬ BIẾN ĐỘNG KHO SẢN XUẤT");
            cTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));

            // Metadata info
            Row r1 = sheet.createRow(1);
            String period = (from != null ? from.format(DATE_FMT) : "Tất cả") + " - " + (to != null ? to.format(DATE_FMT) : "Hiện tại");
            r1.createCell(0).setCellValue("Thời gian: " + period + " | Loại hàng: " + (kind.isBlank() ? "Tất cả" : kind) + " | Giao dịch: " + (type.isBlank() ? "Tất cả" : type));
            r1.getCell(0).setCellStyle(metaStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 10));

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Thời điểm xuất báo cáo: " + LocalDateTime.now().format(DATE_TIME_FMT));
            r2.getCell(0).setCellStyle(metaStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 10));

            // Headers
            String[] headers = {
                    "STT", "Thời gian", "Kho", "Loại hàng", "Loại giao dịch",
                    "Chi tiết hàng hóa", "Tổng số lượng", "Người thao tác",
                    "Nhân viên sản xuất", "Đợt / Lệnh SX", "Mã chứng từ"
            };
            Row r3 = sheet.createRow(4);
            r3.setHeightInPoints(26);
            for (int i = 0; i < headers.length; i++) {
                Cell c = r3.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx = 5;
            double grandTotalQty = 0;
            int stt = 1;

            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    Row r = sheet.createRow(rowIdx++);
                    r.setHeightInPoints(22);

                    r.createCell(0).setCellValue(stt++);
                    r.getCell(0).setCellStyle(dataStyle);

                    Object tg = row.get("thoi_gian");
                    r.createCell(1).setCellValue(tg != null ? tg.toString() : "");
                    r.getCell(1).setCellStyle(dataStyle);

                    r.createCell(2).setCellValue(safeStr(row.get("loai_kho")));
                    r.getCell(2).setCellStyle(dataStyle);

                    r.createCell(3).setCellValue(safeStr(row.get("loai_hang")));
                    r.getCell(3).setCellStyle(dataStyle);

                    r.createCell(4).setCellValue(safeStr(row.get("loai_giao_dich")));
                    r.getCell(4).setCellStyle(dataStyle);

                    r.createCell(5).setCellValue(safeStr(row.get("hanghoa")));
                    r.getCell(5).setCellStyle(dataStyle);

                    double qty = toDouble(row.get("tong_so_luong"));
                    grandTotalQty += qty;
                    Cell cQty = r.createCell(6);
                    cQty.setCellValue(qty);
                    cQty.setCellStyle(numberStyle);

                    r.createCell(7).setCellValue(safeStr(row.get("nguoithuchien")));
                    r.getCell(7).setCellStyle(dataStyle);

                    r.createCell(8).setCellValue(safeStr(row.get("nhanviensanxuat")));
                    r.getCell(8).setCellStyle(dataStyle);

                    String dot = safeStr(row.get("madot_code"));
                    if (dot.isEmpty()) dot = safeStr(row.get("mayeucau"));
                    r.createCell(9).setCellValue(dot);
                    r.getCell(9).setCellStyle(dataStyle);

                    String doc = safeStr(row.get("bang_tham_chieu")) + " #" + safeStr(row.get("ma_tham_chieu"));
                    r.createCell(10).setCellValue(doc);
                    r.getCell(10).setCellStyle(dataStyle);
                }
            }

            // Subtotal row
            Row rSub = sheet.createRow(rowIdx);
            rSub.setHeightInPoints(24);
            Cell cSubLabel = rSub.createCell(0);
            cSubLabel.setCellValue("TỔNG CỘNG (" + (stt - 1) + " bản ghi):");
            cSubLabel.setCellStyle(subtotalStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 5));

            Cell cSubQty = rSub.createCell(6);
            cSubQty.setCellValue(grandTotalQty);
            cSubQty.setCellStyle(createSubtotalNumberStyle(workbook, "#,##0"));

            for (int col = 7; col <= 10; col++) {
                Cell c = rSub.createCell(col);
                c.setCellStyle(subtotalStyle);
            }

            ExcelCoreUtils.autoSizeColumns(sheet, headers.length);
            return toByteArray(workbook);
        }
    }

    // =========================================================================
    // 2. XUẤT LỊCH SỬ KHO TỔNG
    // =========================================================================
    public byte[] exportMainWarehouseHistory(List<Map<String, Object>> rows, LocalDate from, LocalDate to, String kind, String type) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("LICH_SU_KHO_TONG");

            CellStyle titleStyle = createTitleStyle(workbook, IndexedColors.DARK_BLUE.getIndex());
            CellStyle metaStyle = createMetaStyle(workbook);
            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);
            CellStyle numberStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0");
            CellStyle subtotalStyle = createSubtotalStyle(workbook);

            // Title
            Row r0 = sheet.createRow(0);
            r0.setHeightInPoints(32);
            Cell cTitle = r0.createCell(0);
            cTitle.setCellValue("BÁO CÁO LỊCH SỬ KHO TỔNG (CHỨNG TỪ GIAO DỊCH)");
            cTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            // Metadata
            Row r1 = sheet.createRow(1);
            String period = (from != null ? from.format(DATE_FMT) : "Tất cả") + " - " + (to != null ? to.format(DATE_FMT) : "Hiện tại");
            r1.createCell(0).setCellValue("Khoảng thời gian: " + period + " | Loại hàng: " + (kind.isBlank() ? "Tất cả" : kind) + " | Giao dịch: " + (type.isBlank() ? "Tất cả" : type));
            r1.getCell(0).setCellStyle(metaStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 9));

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Thời điểm xuất báo cáo: " + LocalDateTime.now().format(DATE_TIME_FMT));
            r2.getCell(0).setCellStyle(metaStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 9));

            // Headers
            String[] headers = {
                    "STT", "Mã chứng từ", "Bảng tham chiếu", "Thời gian", "Loại hàng",
                    "Loại giao dịch", "Mặt hàng phát sinh", "Số dòng hàng", "Tổng số lượng", "Người thực hiện"
            };
            Row r4 = sheet.createRow(4);
            r4.setHeightInPoints(26);
            for (int i = 0; i < headers.length; i++) {
                Cell c = r4.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx = 5;
            double grandTotalQty = 0;
            int stt = 1;

            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    Row r = sheet.createRow(rowIdx++);
                    r.setHeightInPoints(22);

                    r.createCell(0).setCellValue(stt++);
                    r.getCell(0).setCellStyle(dataStyle);

                    r.createCell(1).setCellValue(safeStr(row.get("ma_chung_tu")));
                    r.getCell(1).setCellStyle(dataStyle);

                    r.createCell(2).setCellValue(safeStr(row.get("bang_tham_chieu")));
                    r.getCell(2).setCellStyle(dataStyle);

                    Object tg = row.get("ngay_giao_dich");
                    r.createCell(3).setCellValue(tg != null ? tg.toString() : "");
                    r.getCell(3).setCellStyle(dataStyle);

                    r.createCell(4).setCellValue(safeStr(row.get("loai_hang")));
                    r.getCell(4).setCellStyle(dataStyle);

                    r.createCell(5).setCellValue(safeStr(row.get("loai_giao_dich")));
                    r.getCell(5).setCellStyle(dataStyle);

                    r.createCell(6).setCellValue(safeStr(row.get("hanghoa")));
                    r.getCell(6).setCellStyle(dataStyle);

                    r.createCell(7).setCellValue(toDouble(row.get("so_dong")));
                    r.getCell(7).setCellStyle(numberStyle);

                    double qty = toDouble(row.get("tong_so_luong"));
                    grandTotalQty += qty;
                    Cell cQty = r.createCell(8);
                    cQty.setCellValue(qty);
                    cQty.setCellStyle(numberStyle);

                    String user = safeStr(row.get("ten_nguoi_thuc_hien"));
                    if (user.isEmpty()) user = safeStr(row.get("ma_nguoi_thuc_hien"));
                    r.createCell(9).setCellValue(user);
                    r.getCell(9).setCellStyle(dataStyle);
                }
            }

            // Subtotal
            Row rSub = sheet.createRow(rowIdx);
            rSub.setHeightInPoints(24);
            Cell cSubLabel = rSub.createCell(0);
            cSubLabel.setCellValue("TỔNG CỘNG (" + (stt - 1) + " chứng từ):");
            cSubLabel.setCellStyle(subtotalStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 7));

            Cell cSubQty = rSub.createCell(8);
            cSubQty.setCellValue(grandTotalQty);
            cSubQty.setCellStyle(createSubtotalNumberStyle(workbook, "#,##0"));

            Cell cUserSub = rSub.createCell(9);
            cUserSub.setCellStyle(subtotalStyle);

            ExcelCoreUtils.autoSizeColumns(sheet, headers.length);
            return toByteArray(workbook);
        }
    }

    // =========================================================================
    // 3. XUẤT PHIẾU XUẤT NVL & XUẤT BÙ
    // =========================================================================
    public byte[] exportMaterialIssues(List<Map<String, Object>> issues) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("PHIEU_XUAT_NVL");

            CellStyle titleStyle = createTitleStyle(workbook, IndexedColors.DARK_BLUE.getIndex());
            CellStyle metaStyle = createMetaStyle(workbook);
            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);
            CellStyle numberStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0");
            CellStyle subtotalStyle = createSubtotalStyle(workbook);

            // Title
            Row r0 = sheet.createRow(0);
            r0.setHeightInPoints(32);
            Cell cTitle = r0.createCell(0);
            cTitle.setCellValue("DANH SÁCH PHIẾU ĐỀ NGHỊ XUẤT NGUYÊN VẬT LIỆU");
            cTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Thời điểm xuất danh sách: " + LocalDateTime.now().format(DATE_TIME_FMT));
            r1.getCell(0).setCellStyle(metaStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 8));

            // Headers
            String[] headers = {
                    "STT", "Mã phiếu", "Đợt sản xuất", "Sản phẩm", "Số yêu cầu",
                    "Người lập", "Ngày lập", "Trạng thái", "Ghi chú"
            };
            Row r3 = sheet.createRow(3);
            r3.setHeightInPoints(26);
            for (int i = 0; i < headers.length; i++) {
                Cell c = r3.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx = 4;
            int stt = 1;
            double sumRequests = 0;

            if (issues != null) {
                for (Map<String, Object> is : issues) {
                    Row r = sheet.createRow(rowIdx++);
                    r.setHeightInPoints(22);

                    r.createCell(0).setCellValue(stt++);
                    r.getCell(0).setCellStyle(dataStyle);

                    r.createCell(1).setCellValue("PXNVL-" + safeStr(is.get("mapx")));
                    r.getCell(1).setCellStyle(dataStyle);

                    r.createCell(2).setCellValue(safeStr(is.get("tendot")));
                    r.getCell(2).setCellStyle(dataStyle);

                    r.createCell(3).setCellValue(safeStr(is.get("tensp")));
                    r.getCell(3).setCellStyle(dataStyle);

                    double reqCount = toDouble(is.get("soyeucau"));
                    sumRequests += reqCount;
                    Cell cReq = r.createCell(4);
                    cReq.setCellValue(reqCount);
                    cReq.setCellStyle(numberStyle);

                    r.createCell(5).setCellValue(safeStr(is.get("hoten")));
                    r.getCell(5).setCellStyle(dataStyle);

                    Object nl = is.get("ngaylap");
                    r.createCell(6).setCellValue(nl != null ? nl.toString() : "");
                    r.getCell(6).setCellStyle(dataStyle);

                    r.createCell(7).setCellValue(safeStr(is.get("trangthai")));
                    r.getCell(7).setCellStyle(dataStyle);

                    r.createCell(8).setCellValue(safeStr(is.get("ghichu")));
                    r.getCell(8).setCellStyle(dataStyle);
                }
            }

            // Subtotal
            Row rSub = sheet.createRow(rowIdx);
            rSub.setHeightInPoints(24);
            Cell cSub = rSub.createCell(0);
            cSub.setCellValue("TỔNG CỘNG (" + (stt - 1) + " phiếu):");
            cSub.setCellStyle(subtotalStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 3));

            Cell cSum = rSub.createCell(4);
            cSum.setCellValue(sumRequests);
            cSum.setCellStyle(createSubtotalNumberStyle(workbook, "#,##0"));

            for (int col = 5; col <= 8; col++) {
                Cell c = rSub.createCell(col);
                c.setCellStyle(subtotalStyle);
            }

            ExcelCoreUtils.autoSizeColumns(sheet, headers.length);
            return toByteArray(workbook);
        }
    }

    // =========================================================================
    // 4. XUẤT PHIẾU NHẬP KHO NVL
    // =========================================================================
    public byte[] exportMaterialReceipts(List<Map<String, Object>> receipts) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("PHIEU_NHAP_NVL");

            CellStyle titleStyle = createTitleStyle(workbook, IndexedColors.DARK_BLUE.getIndex());
            CellStyle metaStyle = createMetaStyle(workbook);
            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);
            CellStyle currencyStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0 ₫");
            CellStyle subtotalStyle = createSubtotalStyle(workbook);

            // Title
            Row r0 = sheet.createRow(0);
            r0.setHeightInPoints(32);
            Cell cTitle = r0.createCell(0);
            cTitle.setCellValue("DANH SÁCH PHIẾU NHẬP NGUYÊN VẬT LIỆU");
            cTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Thời điểm xuất danh sách: " + LocalDateTime.now().format(DATE_TIME_FMT));
            r1.getCell(0).setCellStyle(metaStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));

            // Headers
            String[] headers = {
                    "STT", "Mã phiếu nhập", "Nhà cung cấp", "Người lập phiếu",
                    "Ngày tạo", "Tổng tiền hàng (VNĐ)", "Trạng thái", "Ghi chú"
            };
            Row r3 = sheet.createRow(3);
            r3.setHeightInPoints(26);
            for (int i = 0; i < headers.length; i++) {
                Cell c = r3.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx = 4;
            int stt = 1;
            double grandTotalMoney = 0;

            if (receipts != null) {
                for (Map<String, Object> r : receipts) {
                    Row row = sheet.createRow(rowIdx++);
                    row.setHeightInPoints(22);

                    row.createCell(0).setCellValue(stt++);
                    row.getCell(0).setCellStyle(dataStyle);

                    row.createCell(1).setCellValue(safeStr(r.get("maphieu")));
                    row.getCell(1).setCellStyle(dataStyle);

                    row.createCell(2).setCellValue(safeStr(r.get("tenncc")));
                    row.getCell(2).setCellStyle(dataStyle);

                    row.createCell(3).setCellValue(safeStr(r.get("hoten")));
                    row.getCell(3).setCellStyle(dataStyle);

                    Object nt = r.get("ngaytao");
                    row.createCell(4).setCellValue(nt != null ? nt.toString() : "");
                    row.getCell(4).setCellStyle(dataStyle);

                    double money = toDouble(r.get("tongtien"));
                    grandTotalMoney += money;
                    Cell cMoney = row.createCell(5);
                    cMoney.setCellValue(money);
                    cMoney.setCellStyle(currencyStyle);

                    row.createCell(6).setCellValue(safeStr(r.get("trangthai")));
                    row.getCell(6).setCellStyle(dataStyle);

                    row.createCell(7).setCellValue(safeStr(r.get("ghichu")));
                    row.getCell(7).setCellStyle(dataStyle);
                }
            }

            // Subtotal
            Row rSub = sheet.createRow(rowIdx);
            rSub.setHeightInPoints(24);
            Cell cSub = rSub.createCell(0);
            cSub.setCellValue("TỔNG CỘNG (" + (stt - 1) + " phiếu):");
            cSub.setCellStyle(subtotalStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 4));

            Cell cSumMoney = rSub.createCell(5);
            cSumMoney.setCellValue(grandTotalMoney);
            cSumMoney.setCellStyle(createSubtotalNumberStyle(workbook, "#,##0 ₫"));

            rSub.createCell(6).setCellStyle(subtotalStyle);
            rSub.createCell(7).setCellStyle(subtotalStyle);

            ExcelCoreUtils.autoSizeColumns(sheet, headers.length);
            return toByteArray(workbook);
        }
    }

    // =========================================================================
    // 5. XUẤT BÁO CÁO CÂN ĐỐI CUNG - CẦU & THIẾU HỤT VẬT TƯ MRP
    // =========================================================================
    @SuppressWarnings("unchecked")
    public byte[] exportMrpBalanceReport(Map<String, Object> mrpData) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("CAN_DOI_NVL_MRP");

            CellStyle titleStyle = createTitleStyle(workbook, IndexedColors.DARK_BLUE.getIndex());
            CellStyle metaStyle = createMetaStyle(workbook);
            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);
            CellStyle numberStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0");
            CellStyle currencyStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0 ₫");
            CellStyle subtotalStyle = createSubtotalStyle(workbook);

            // Title Banner
            Row r0 = sheet.createRow(0);
            r0.setHeightInPoints(32);
            Cell cTitle = r0.createCell(0);
            cTitle.setCellValue("BÁO CÁO HOẠCH ĐỊNH CÂN ĐỐI CUNG - CẦU VẬT TƯ (MRP)");
            cTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Hệ thống tính toán cân đối dựa trên Tổng nhu cầu theo Kế hoạch sản xuất & Khả dụng kho hiện hành");
            r1.getCell(0).setCellStyle(metaStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 11));

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Thời điểm xuất báo cáo: " + LocalDateTime.now().format(DATE_TIME_FMT));
            r2.getCell(0).setCellStyle(metaStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 11));

            // Summary Table
            Map<String, Object> summary = mrpData != null ? (Map<String, Object>) mrpData.get("summary") : null;
            if (summary != null) {
                Row rKpiH = sheet.createRow(4);
                rKpiH.createCell(0).setCellValue("Chỉ số tổng hợp");
                rKpiH.createCell(1).setCellValue("Giá trị");
                rKpiH.getCell(0).setCellStyle(headerStyle);
                rKpiH.getCell(1).setCellStyle(headerStyle);

                createKpiRow(sheet, 5, "Số chủng loại linh kiện thiếu hụt:", toDouble(summary.get("total_shortage_types")), dataStyle, numberStyle);
                createKpiRow(sheet, 6, "Tổng số lượng vật tư cần mua bổ sung:", toDouble(summary.get("total_shortage_qty")), dataStyle, numberStyle);
                createKpiRow(sheet, 7, "Tổng chi phí mua sắm dự kiến (VNĐ):", toDouble(summary.get("total_estimated_cost")), dataStyle, currencyStyle);
                createKpiRow(sheet, 8, "Số lệnh sản xuất đang chờ vật tư:", toDouble(summary.get("blocked_orders_count")), dataStyle, numberStyle);
            }

            // Detail Table
            int tableStartRow = 10;
            Row rH = sheet.createRow(tableStartRow);
            rH.setHeightInPoints(28);
            String[] headers = {
                    "STT", "Mã NVL", "Tên nguyên vật liệu", "ĐVT", "Tổng nhu cầu SX",
                    "Tồn kho tổng", "Tồn kho xưởng SX", "Tổng khả dụng", "Số lượng thiếu",
                    "Đơn giá vốn", "Chi phí mua dự kiến", "Thời gian giao hàng (ngày)", "Trạng thái cung ứng"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell c = rH.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx = tableStartRow + 1;
            int stt = 1;
            double grandShortageQty = 0;
            double grandCost = 0;

            List<Map<String, Object>> items = mrpData != null ? (List<Map<String, Object>>) mrpData.get("items") : null;
            if (items != null) {
                for (Map<String, Object> item : items) {
                    Row r = sheet.createRow(rowIdx++);
                    r.setHeightInPoints(22);

                    r.createCell(0).setCellValue(stt++);
                    r.getCell(0).setCellStyle(dataStyle);

                    r.createCell(1).setCellValue(safeStr(item.get("manvl_code")));
                    r.getCell(1).setCellStyle(dataStyle);

                    r.createCell(2).setCellValue(safeStr(item.get("tennvl")));
                    r.getCell(2).setCellStyle(dataStyle);

                    r.createCell(3).setCellValue(safeStr(item.get("donvitinh")));
                    r.getCell(3).setCellStyle(dataStyle);

                    Cell cReq = r.createCell(4);
                    cReq.setCellValue(toDouble(item.get("tongnhucau")));
                    cReq.setCellStyle(numberStyle);

                    Cell cMain = r.createCell(5);
                    cMain.setCellValue(toDouble(item.get("ton_kho_tong")));
                    cMain.setCellStyle(numberStyle);

                    Cell cProd = r.createCell(6);
                    cProd.setCellValue(toDouble(item.get("ton_kho_sx")));
                    cProd.setCellStyle(numberStyle);

                    Cell cAvail = r.createCell(7);
                    cAvail.setCellValue(toDouble(item.get("tong_kha_dung")));
                    cAvail.setCellStyle(numberStyle);

                    double shortQty = toDouble(item.get("soluongthieu"));
                    grandShortageQty += shortQty;
                    Cell cShort = r.createCell(8);
                    cShort.setCellValue(shortQty);
                    cShort.setCellStyle(numberStyle);

                    Cell cPrice = r.createCell(9);
                    cPrice.setCellValue(toDouble(item.get("dongianhap")));
                    cPrice.setCellStyle(currencyStyle);

                    double cost = toDouble(item.get("chi_phi_mua_du_kien"));
                    grandCost += cost;
                    Cell cCost = r.createCell(10);
                    cCost.setCellValue(cost);
                    cCost.setCellStyle(currencyStyle);

                    Cell cLt = r.createCell(11);
                    cLt.setCellValue(toDouble(item.get("lead_time_ngay")));
                    cLt.setCellStyle(numberStyle);

                    r.createCell(12).setCellValue(safeStr(item.get("trang_thai_cung_ung")));
                    r.getCell(12).setCellStyle(dataStyle);
                }
            }

            // Subtotal
            Row rSub = sheet.createRow(rowIdx);
            rSub.setHeightInPoints(24);
            Cell cSub = rSub.createCell(0);
            cSub.setCellValue("TỔNG CỘNG (" + (stt - 1) + " NVL):");
            cSub.setCellStyle(subtotalStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 7));

            Cell cSubShort = rSub.createCell(8);
            cSubShort.setCellValue(grandShortageQty);
            cSubShort.setCellStyle(createSubtotalNumberStyle(workbook, "#,##0"));

            rSub.createCell(9).setCellStyle(subtotalStyle);

            Cell cSubCost = rSub.createCell(10);
            cSubCost.setCellValue(grandCost);
            cSubCost.setCellStyle(createSubtotalNumberStyle(workbook, "#,##0 ₫"));

            rSub.createCell(11).setCellStyle(subtotalStyle);
            rSub.createCell(12).setCellStyle(subtotalStyle);

            ExcelCoreUtils.autoSizeColumns(sheet, headers.length);
            return toByteArray(workbook);
        }
    }

    // =========================================================================
    // 6. XUẤT BÁO CÁO ĐỐI SOÁT HAO HỤT BOM CHI TIẾT
    // =========================================================================
    @SuppressWarnings("unchecked")
    public byte[] exportBomVarianceDetail(Map<String, Object> d) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("DOI_SOAT_BOM");

            CellStyle titleStyle = createTitleStyle(workbook, IndexedColors.DARK_BLUE.getIndex());
            CellStyle metaStyle = createMetaStyle(workbook);
            CellStyle sectionHeaderStyle = createSectionHeaderStyle(workbook);
            CellStyle headerStyle = ExcelCoreUtils.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelCoreUtils.createDataStyle(workbook);
            CellStyle numberStyle = ExcelCoreUtils.createNumberStyle(workbook, "#,##0");
            CellStyle subtotalStyle = createSubtotalStyle(workbook);

            // Title Banner
            Row r0 = sheet.createRow(0);
            r0.setHeightInPoints(32);
            Cell cTitle = r0.createCell(0);
            cTitle.setCellValue("BÁO CÁO ĐỐI SOÁT ĐỊNH MỨC BOM & HAO HỤT LẮP RÁP");
            cTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            // Section 1: Thông tin lệnh sản xuất
            Row r1 = sheet.createRow(2);
            r1.createCell(0).setCellValue("1. THÔNG TIN LỆNH / ĐỢT SẢN XUẤT");
            r1.getCell(0).setCellStyle(sectionHeaderStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 8));

            createKeyValueRow(sheet, 3, "Mã đợt / Lệnh SX:", safeStr(d.get("madot_code")), "Sản phẩm Laptop:", safeStr(d.get("tensp")), dataStyle);
            createKeyValueRow(sheet, 4, "Phiên bản BOM:", safeStr(d.get("phienban_bom")), "Tiến độ lắp ráp:", safeStr(d.get("tiendo_text")) + " máy", dataStyle);
            createKeyValueRow(sheet, 5, "Trạng thái lệnh:", Boolean.TRUE.equals(d.get("is_completed")) ? "ĐÃ HOÀN THÀNH" : "ĐANG THỰC HIỆN", "Đánh giá chung:", safeStr(d.get("danh_gia_tong_quan")), dataStyle);
            createKeyValueRow(sheet, 6, "Người phụ trách:", safeStr(d.get("nguoiphutrach_ten")), "Ngày xuất báo cáo:", LocalDateTime.now().format(DATE_TIME_FMT), dataStyle);

            // Section 2: Bảng đối soát vật tư
            Row rSec2 = sheet.createRow(8);
            rSec2.createCell(0).setCellValue("2. BẢNG ĐỐI SOÁT TIÊU HAO ĐỊNH MỨC TỪNG LINH KIỆN");
            rSec2.getCell(0).setCellStyle(sectionHeaderStyle);
            sheet.addMergedRegion(new CellRangeAddress(8, 8, 0, 8));

            String[] matHeaders = {
                    "STT", "Linh kiện", "ĐVT", "Định mức cho phép", "Thực tế xuất xưởng",
                    "Còn lại trên chuyền", "Tiêu hao thực tế", "Chênh lệch", "Đánh giá vật tư"
            };
            Row rMatH = sheet.createRow(9);
            rMatH.setHeightInPoints(26);
            for (int i = 0; i < matHeaders.length; i++) {
                Cell c = rMatH.createCell(i);
                c.setCellValue(matHeaders[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx = 10;
            int stt = 1;
            double sumDinhmuc = 0, sumThucxuat = 0, sumConlai = 0, sumTieuhao = 0, sumChenhlech = 0;

            List<Map<String, Object>> mats = (List<Map<String, Object>>) d.get("materials");
            if (mats != null) {
                for (Map<String, Object> m : mats) {
                    Row r = sheet.createRow(rowIdx++);
                    r.setHeightInPoints(22);

                    r.createCell(0).setCellValue(stt++);
                    r.getCell(0).setCellStyle(dataStyle);

                    r.createCell(1).setCellValue(safeStr(m.get("tennvl")));
                    r.getCell(1).setCellStyle(dataStyle);

                    r.createCell(2).setCellValue(safeStr(m.get("donvitinh")));
                    r.getCell(2).setCellStyle(dataStyle);

                    double dm = toDouble(m.get("dinhmuc_cho_phep"));
                    sumDinhmuc += dm;
                    Cell cDm = r.createCell(3);
                    cDm.setCellValue(dm);
                    cDm.setCellStyle(numberStyle);

                    double tx = toDouble(m.get("thuc_xuat"));
                    sumThucxuat += tx;
                    Cell cTx = r.createCell(4);
                    cTx.setCellValue(tx);
                    cTx.setCellStyle(numberStyle);

                    double cl = toDouble(m.get("nvl_con_lai_chuyen"));
                    sumConlai += cl;
                    Cell cCl = r.createCell(5);
                    cCl.setCellValue(cl);
                    cCl.setCellStyle(numberStyle);

                    double th = toDouble(m.get("tieu_hao_thuc_te"));
                    sumTieuhao += th;
                    Cell cTh = r.createCell(6);
                    cTh.setCellValue(th);
                    cTh.setCellStyle(numberStyle);

                    double ch = toDouble(m.get("chenh_lech"));
                    sumChenhlech += ch;
                    Cell cCh = r.createCell(7);
                    cCh.setCellValue(ch);
                    cCh.setCellStyle(numberStyle);

                    r.createCell(8).setCellValue(safeStr(m.get("danh_gia")));
                    r.getCell(8).setCellStyle(dataStyle);
                }
            }

            // Subtotal Vật tư
            Row rMatSub = sheet.createRow(rowIdx++);
            rMatSub.setHeightInPoints(24);
            Cell cSubM = rMatSub.createCell(0);
            cSubM.setCellValue("TỔNG CỘNG LINH KIỆN:");
            cSubM.setCellStyle(subtotalStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 2));

            createSubtotalNumberCell(rMatSub, 3, sumDinhmuc, workbook);
            createSubtotalNumberCell(rMatSub, 4, sumThucxuat, workbook);
            createSubtotalNumberCell(rMatSub, 5, sumConlai, workbook);
            createSubtotalNumberCell(rMatSub, 6, sumTieuhao, workbook);
            createSubtotalNumberCell(rMatSub, 7, sumChenhlech, workbook);
            rMatSub.createCell(8).setCellStyle(subtotalStyle);

            // Section 3: Phiếu xuất liên quan
            rowIdx += 1;
            Row rSec3 = sheet.createRow(rowIdx++);
            rSec3.createCell(0).setCellValue("3. DANH SÁCH PHIẾU XUẤT NVL LIÊN QUAN");
            rSec3.getCell(0).setCellStyle(sectionHeaderStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 8));

            String[] pxHeaders = {"Mã phiếu", "Ngày lập", "Ngày duyệt", "Người lập", "Người duyệt", "Tổng số lượng", "Trạng thái phiếu"};
            Row rPxH = sheet.createRow(rowIdx++);
            rPxH.setHeightInPoints(26);
            for (int i = 0; i < pxHeaders.length; i++) {
                Cell c = rPxH.createCell(i);
                c.setCellValue(pxHeaders[i]);
                c.setCellStyle(headerStyle);
            }

            List<Map<String, Object>> phieuXuats = (List<Map<String, Object>>) d.get("phieu_xuats");
            if (phieuXuats != null && !phieuXuats.isEmpty()) {
                for (Map<String, Object> px : phieuXuats) {
                    Row r = sheet.createRow(rowIdx++);
                    r.setHeightInPoints(22);

                    r.createCell(0).setCellValue("PXNVL-" + safeStr(px.get("mapx")));
                    r.getCell(0).setCellStyle(dataStyle);

                    Object nl = px.get("ngaylap");
                    r.createCell(1).setCellValue(nl != null ? nl.toString() : "");
                    r.getCell(1).setCellStyle(dataStyle);

                    Object nd = px.get("ngayduyet");
                    r.createCell(2).setCellValue(nd != null ? nd.toString() : "---");
                    r.getCell(2).setCellStyle(dataStyle);

                    r.createCell(3).setCellValue(safeStr(px.get("nguoilap_ten")));
                    r.getCell(3).setCellStyle(dataStyle);

                    r.createCell(4).setCellValue(safeStr(px.get("nguoiduyet_ten")));
                    r.getCell(4).setCellStyle(dataStyle);

                    Cell cQty = r.createCell(5);
                    cQty.setCellValue(toDouble(px.get("tong_soluong")));
                    cQty.setCellStyle(numberStyle);

                    r.createCell(6).setCellValue(safeStr(px.get("trangthai")));
                    r.getCell(6).setCellStyle(dataStyle);
                }
            } else {
                Row rEmpty = sheet.createRow(rowIdx++);
                rEmpty.createCell(0).setCellValue("Chưa ghi nhận phiếu xuất NVL nào cho lệnh này.");
                rEmpty.getCell(0).setCellStyle(dataStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 6));
            }

            ExcelCoreUtils.autoSizeColumns(sheet, 9);
            return toByteArray(workbook);
        }
    }

    // =========================================================================
    // HELPER STYLES & CONVERSIONS
    // =========================================================================
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

    private CellStyle createSectionHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
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
        style.setVerticalAlignment(VerticalAlignment.CENTER);
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
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createSubtotalNumberStyle(Workbook wb, String formatPattern) {
        CellStyle style = createSubtotalStyle(wb);
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat(formatPattern));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private void createSubtotalNumberCell(Row row, int colIdx, double value, Workbook wb) {
        Cell c = row.createCell(colIdx);
        c.setCellValue(value);
        c.setCellStyle(createSubtotalNumberStyle(wb, "#,##0"));
    }

    private void createKpiRow(Sheet sheet, int rowIdx, String label, double value, CellStyle lblStyle, CellStyle valStyle) {
        Row r = sheet.createRow(rowIdx);
        Cell cLbl = r.createCell(0);
        cLbl.setCellValue(label);
        cLbl.setCellStyle(lblStyle);

        Cell cVal = r.createCell(1);
        cVal.setCellValue(value);
        cVal.setCellStyle(valStyle);
    }

    private void createKeyValueRow(Sheet sheet, int rowIdx, String k1, String v1, String k2, String v2, CellStyle style) {
        Row r = sheet.createRow(rowIdx);
        r.createCell(0).setCellValue(k1);
        r.getCell(0).setCellStyle(style);
        r.createCell(1).setCellValue(v1);
        r.getCell(1).setCellStyle(style);

        r.createCell(3).setCellValue(k2);
        r.getCell(3).setCellStyle(style);
        r.createCell(4).setCellValue(v2);
        r.getCell(4).setCellStyle(style);
    }

    private String safeStr(Object obj) {
        if (obj == null) return "";
        return ExcelSecurityValidator.sanitizeForFormulaInjection(obj.toString().trim());
    }

    private double toDouble(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number num) return num.doubleValue();
        try {
            return Double.parseDouble(obj.toString().trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private byte[] toByteArray(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            workbook.write(bos);
            return bos.toByteArray();
        }
    }
}
