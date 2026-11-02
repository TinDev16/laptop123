package com.example.demo.Controller;

import com.example.demo.Service.WarehouseReportService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.excel.service.TransactionalExportExcelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
public class WarehouseReportController {

    private static final Set<String> REPORT_TRANSACTION_TYPES = Set.of(
            "NHAP_NVL",
            "XUAT_NVL",
            "NHAP_THANH_PHAM",
            "XUAT_THANH_PHAM"
    );

    private final WarehouseReportService service;
    private final TransactionalExportExcelService excelService;

    public WarehouseReportController(
            WarehouseReportService service,
            TransactionalExportExcelService excelService) {

        this.service = service;
        this.excelService = excelService;
    }

    // =====================================================
    // EXCEL: XUẤT LỊCH SỬ KHO TỔNG
    // =====================================================
    @GetMapping("/kho/lich-su/excel/export")
    public ResponseEntity<byte[]> exportMainWarehouseHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "") String kind,
            @RequestParam(defaultValue = "") String type) {
        try {
            List<Map<String, Object>> rows = service.history(from, to, kind, type);
            byte[] content = excelService.exportMainWarehouseHistory(rows, from, to, kind, type);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Lich_Su_Kho_Tong.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =====================================================
    // EXCEL: XUẤT BÁO CÁO CÂN ĐỐI CUNG CẦU MRP
    // =====================================================
    @GetMapping({"/baocao/can-doi-nvl/excel/export", "/baocao/excel/mrp"})
    public ResponseEntity<byte[]> exportMrpReport() {
        try {
            Map<String, Object> mrpData = service.getMrpShortageReport();
            byte[] content = excelService.exportMrpBalanceReport(mrpData);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Bao_Cao_Can_Doi_NVL_MRP.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =====================================================
    // EXCEL: XUẤT ĐỐI SOÁT HAO HỤT ĐỊNH MỨC BOM CHI TIẾT
    // =====================================================
    @GetMapping("/baocao/doi-soat-bom/{id}/excel/export")
    public ResponseEntity<byte[]> exportBomVarianceDetailExcel(
            @org.springframework.web.bind.annotation.PathVariable("id") Long maycsx) {
        try {
            Map<String, Object> detail = service.getBomVarianceDetail(maycsx);
            if (detail == null) {
                return ResponseEntity.notFound().build();
            }
            byte[] content = excelService.exportBomVarianceDetail(detail);
            String code = detail.get("madot_code") != null ? detail.get("madot_code").toString() : String.valueOf(maycsx);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Doi_Soat_BOM_" + code + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    // =====================================================
    // LỊCH SỬ KHO
    //
    // 1 dòng = 1 chứng từ
    // =====================================================

    @GetMapping("/kho/lich-su")
    public String history(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @RequestParam(defaultValue = "")
            String kind,

            @RequestParam(defaultValue = "")
            String type,

            Model model) {

        model.addAttribute(
                "rows",
                service.history(
                        from,
                        to,
                        kind,
                        type
                )
        );

        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("kind", kind);
        model.addAttribute("type", type);

        return "mrp/warehouse-history";
    }


    // =====================================================
    // CHI TIẾT CHỨNG TỪ
    //
    // Ví dụ:
    //
    // /kho/lich-su/chi-tiet
    //      ?sourceTable=phieuxuat_nvl
    //      &documentId=20
    // =====================================================

    @GetMapping("/kho/lich-su/chi-tiet")
    public String historyDetail(
            @RequestParam String sourceTable,
            @RequestParam long documentId,
            Model model) {

        List<Map<String, Object>> rows =
                service.historyDetail(
                        sourceTable,
                        documentId
                );

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không tìm thấy lịch sử của chứng từ."
            );
        }

        Map<String, Object> first = rows.get(0);

        model.addAttribute("rows", rows);
        model.addAttribute(
                "sourceTable",
                sourceTable
        );
        model.addAttribute(
                "documentId",
                documentId
        );
        model.addAttribute(
                "document",
                first
        );
        model.addAttribute(
                "documentTypeLabel",
                switch (sourceTable) {
                    case "phieunhap_nvl" -> "Phiếu nhập nguyên vật liệu";
                    case "phieuxuat_nvl" -> "Phiếu xuất nguyên vật liệu";
                    case "ketqua_sanxuat" -> "Nhập kho thành phẩm sản xuất";
                    case "phieuxuat_thanhpham" -> "Phiếu xuất thành phẩm";
                    default -> "Chứng từ kho";
                }
        );

        return "mrp/warehouse-history-detail";
    }


    // =====================================================
    // BÁO CÁO TOÀN DIỆN MRP & KHO
    // =====================================================

    private LocalDate[] resolveDateRange(Integer month, Integer quarter, Integer year) {
        LocalDate today = LocalDate.now();
        int selectedYear = year == null ? today.getYear() : year;
        LocalDate from;
        LocalDate to;

        if (month != null) {
            if (month < 1 || month > 12) {
                throw new IllegalArgumentException("Tháng không hợp lệ.");
            }
            from = LocalDate.of(selectedYear, month, 1);
            to = from.plusMonths(1).minusDays(1);
        } else if (quarter != null) {
            if (quarter < 1 || quarter > 4) {
                throw new IllegalArgumentException("Quý không hợp lệ.");
            }
            from = LocalDate.of(selectedYear, (quarter - 1) * 3 + 1, 1);
            to = from.plusMonths(3).minusDays(1);
        } else {
            // Khi không chọn tháng hoặc quý cụ thể, mặc định lấy cả năm để tổng hợp toàn diện
            from = LocalDate.of(selectedYear, 1, 1);
            to = LocalDate.of(selectedYear, 12, 31);
        }
        return new LocalDate[]{from, to};
    }

    @GetMapping("/baocao")
    public String report(
            @RequestParam(defaultValue = "xnt")
            String tab,

            @RequestParam(required = false)
            Integer month,

            @RequestParam(required = false)
            Integer quarter,

            @RequestParam(required = false)
            Integer year,

            @RequestParam(defaultValue = "")
            String type,

            @RequestParam(defaultValue = "ALL")
            String kind,

            Model model) {

        LocalDate[] range = resolveDateRange(month, quarter, year);
        LocalDate from = range[0];
        LocalDate to = range[1];

        // Validate loại giao dịch nếu có lọc chứng từ
        if (!type.isBlank() && !REPORT_TRANSACTION_TYPES.contains(type)) {
            throw new IllegalArgumentException("Loại giao dịch báo cáo không hợp lệ.");
        }

        // Dữ liệu chung
        model.addAttribute("tab", tab);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("month", month);
        model.addAttribute("quarter", quarter);
        model.addAttribute("year", year);
        model.addAttribute("selectedKind", kind);
        model.addAttribute("selectedType", type);

        // =====================================================
        // TAB 1: XUẤT - NHẬP - TỒN & GIÁ TRỊ VỐN SKU
        // =====================================================
        if ("xnt".equalsIgnoreCase(tab)) {
            Map<String, BigDecimal> totals = service.totals(from, to);
            Map<String, BigDecimal> currentStock = service.stockAt(to);
            List<Map<String, Object>> inventoryDetails = service.getInventoryDetails(from, to, kind);
            Map<String, Object> inventorySummary = service.getInventorySummary(inventoryDetails);

            model.addAttribute("totals", totals);
            model.addAttribute("currentStock", currentStock);
            model.addAttribute("inventoryDetails", inventoryDetails);
            model.addAttribute("inventorySummary", inventorySummary);
            model.addAttribute("showDetails", !type.isBlank());
            model.addAttribute("rows", type.isBlank() ? List.of() : service.history(from, to, "", type));
        }

        // =====================================================
        // TAB 2: HIỆU SUẤT & CHẤT LƯỢNG SẢN XUẤT (OTIF, LỖI)
        // =====================================================
        else if ("sanxuat".equalsIgnoreCase(tab)) {
            Map<String, Object> prodPerf = service.getProductionPerformance(from, to);
            model.addAttribute("prodPerf", prodPerf);
        }

        // =====================================================
        // TAB 3: CÂN ĐỐI CUNG - CẦU & THIẾU HỤT VẬT TƯ MRP
        // =====================================================
        else if ("mrp".equalsIgnoreCase(tab)) {
            Map<String, Object> mrpData = service.getMrpShortageReport();
            model.addAttribute("mrpData", mrpData);
        }

        // =====================================================
        // TAB 4: ĐỐI SOÁT ĐỊNH MỨC BOM & HAO HỤT THỰC TẾ
        // =====================================================
        else if ("bom".equalsIgnoreCase(tab)) {
            List<Map<String, Object>> bomVariances = service.getBomVarianceReport(from, to);
            model.addAttribute("bomVariances", bomVariances);
        }

        return "mrp/report";
    }

    // =====================================================
    // TRANG XEM CHI TIẾT ĐỐI SOÁT ĐỊNH MỨC BOM CHO 1 LỆNH SX
    // =====================================================
    @GetMapping("/baocao/doi-soat-bom/{id}")
    public String bomVarianceDetail(
            @org.springframework.web.bind.annotation.PathVariable("id") Long maycsx,
            Model model
    ) {
        Map<String, Object> detail = service.getBomVarianceDetail(maycsx);
        if (detail == null) {
            return "redirect:/baocao?tab=bom";
        }
        model.addAttribute("d", detail);
        return "mrp/bom-variance-detail";
    }

    // =====================================================
    // EXCEL: XUẤT BÁO CÁO TỔNG HỢP THEO TAB (.xlsx)
    // =====================================================
    @GetMapping("/baocao/export-excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(defaultValue = "xnt") String tab,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "ALL") String kind) {
        try {
            LocalDate[] range = resolveDateRange(month, quarter, year);
            LocalDate from = range[0];
            LocalDate to = range[1];

            byte[] content;
            String filename;

            if ("sanxuat".equalsIgnoreCase(tab)) {
                Map<String, Object> perf = service.getProductionPerformance(from, to);
                content = excelService.exportProductionPerformanceReport(perf, from, to);
                filename = "Bao_Cao_Hieu_Suat_San_Xuat_" + from + "_" + to + ".xlsx";
            } else if ("mrp".equalsIgnoreCase(tab)) {
                Map<String, Object> mrpData = service.getMrpShortageReport();
                content = excelService.exportMrpBalanceReport(mrpData);
                filename = "Bao_Cao_Can_Doi_NVL_MRP.xlsx";
            } else if ("bom".equalsIgnoreCase(tab)) {
                List<Map<String, Object>> bomVariances = service.getBomVarianceReport(from, to);
                content = excelService.exportBomVarianceReport(bomVariances, from, to);
                filename = "Bao_Cao_Doi_Soat_BOM_" + from + "_" + to + ".xlsx";
            } else {
                // Default: Tab XNT
                List<Map<String, Object>> details = service.getInventoryDetails(from, to, kind);
                Map<String, Object> summary = service.getInventorySummary(details);
                content = excelService.exportInventoryReport(details, summary, from, to, kind);
                filename = "Bao_Cao_Xuat_Nhap_Ton_" + from + "_" + to + ".xlsx";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =====================================================
    // XUẤT BÁO CÁO CSV (UTF-8 BOM CHO EXCEL)
    // =====================================================
    @GetMapping("/baocao/export-csv")
    public void exportCsv(
            @RequestParam(defaultValue = "xnt") String tab,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "ALL") String kind,
            jakarta.servlet.http.HttpServletResponse response
    ) throws java.io.IOException {
        LocalDate[] range = resolveDateRange(month, quarter, year);
        LocalDate from = range[0];
        LocalDate to = range[1];

        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String filename = "BaoCao_" + tab.toUpperCase() + "_" + from + "_" + to + ".csv";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        java.io.PrintWriter writer = response.getWriter();
        writer.write('\uFEFF'); // UTF-8 BOM để Excel hiển thị tiếng Việt chuẩn

        if ("sanxuat".equalsIgnoreCase(tab)) {
            writer.println("Mã lệnh,Đợt sản xuất,Sản phẩm,Kỹ thuật viên,Số lượng yêu cầu,Số lượng hoàn thành,Số lượng lỗi,Tỷ lệ lỗi (%),Hạn hoàn thành,Đánh giá tiến độ,Trạng thái");
            var perf = service.getProductionPerformance(from, to);
            @SuppressWarnings("unchecked")
            var orders = (List<Map<String, Object>>) perf.get("orders");
            for (var o : orders) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,%s,%s%%,\"%s\",\"%s\",\"%s\"%n",
                        o.get("madot_code"),
                        o.get("tendot"),
                        o.get("tensp"),
                        o.get("hoten_phutrach"),
                        o.get("soluongyeucau"),
                        o.get("soluonghoanthanh"),
                        o.get("soluongloi"),
                        o.get("ty_le_loi"),
                        o.get("hanhoanthanh") != null ? o.get("hanhoanthanh") : "",
                        o.get("danh_gia_tien_do"),
                        o.get("trangthai")
                );
            }
        } else if ("mrp".equalsIgnoreCase(tab)) {
            writer.println("Mã NVL,Tên NVL,ĐVT,Tổng nhu cầu sản xuất,Tồn kho tổng,Tồn xưởng,Tổng khả dụng,Số lượng thiếu,Đơn giá vốn,Chi phí dự kiến (VNĐ),Lead time (ngày),Trạng thái cung ứng");
            var mrp = service.getMrpShortageReport();
            @SuppressWarnings("unchecked")
            var items = (List<Map<String, Object>>) mrp.get("items");
            for (var i : items) {
                writer.printf("\"%s\",\"%s\",\"%s\",%s,%s,%s,%s,%s,%s,%s,%s,\"%s\"%n",
                        i.get("manvl_code"),
                        i.get("tennvl"),
                        i.get("donvitinh"),
                        i.get("tongnhucau"),
                        i.get("ton_kho_tong"),
                        i.get("ton_kho_sx"),
                        i.get("tong_kha_dung"),
                        i.get("soluongthieu"),
                        i.get("dongianhap"),
                        i.get("chi_phi_mua_du_kien"),
                        i.get("lead_time_ngay"),
                        i.get("trang_thai_cung_ung")
                );
            }
        } else if ("bom".equalsIgnoreCase(tab)) {
            writer.println("Lệnh SX,Sản phẩm,BOM Ver,Tiến độ lắp ráp,Trạng thái lệnh,Linh kiện,ĐVT,Định mức cho phép,Thực tế xuất,Đang trên chuyền,Chênh lệch,Đánh giá");
            var orders = service.getBomVarianceReport(from, to);
            for (var order : orders) {
                @SuppressWarnings("unchecked")
                var mats = (List<Map<String, Object>>) order.get("materials");
                for (var b : mats) {
                    writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,%s,%s,\"%s\"%n",
                            order.get("madot_code"),
                            order.get("tensp"),
                            order.get("phienban_bom"),
                            order.get("tiendo_text"),
                            order.get("trangthai"),
                            b.get("tennvl"),
                            b.get("donvitinh"),
                            b.get("dinhmuc_cho_phep"),
                            b.get("thuc_xuat"),
                            b.get("nvl_con_lai_chuyen"),
                            b.get("chenh_lech"),
                            b.get("danh_gia")
                    );
                }
            }
        } else {
            // Default XNT
            writer.println("Mã hàng,Tên hàng hóa,Loại hàng,Đơn vị tính,Tồn đầu kỳ,Nhập trong kỳ,Xuất trong kỳ,Tồn cuối kỳ,Đơn giá vốn,Thành tiền tồn cuối kỳ (VNĐ),Tình trạng");
            var details = service.getInventoryDetails(from, to, kind);
            for (var d : details) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,%s,%s,%s,%s,\"%s\"%n",
                        d.get("ma_code"),
                        d.get("ten_hang"),
                        d.get("loai_hang"),
                        d.get("donvitinh"),
                        d.get("ton_dau_ky"),
                        d.get("nhap_trong_ky"),
                        d.get("xuat_trong_ky"),
                        d.get("ton_cuoi_ky"),
                        d.get("don_gia"),
                        d.get("gia_tri_ton"),
                        d.get("canh_bao")
                );
            }
        }
        writer.flush();
    }
}
