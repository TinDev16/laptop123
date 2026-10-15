package com.example.demo.Controller;

import com.example.demo.Service.WarehouseProductionHistoryService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WarehouseProductionHistoryController {

    private static final Set<String> TRANSACTION_TYPES = Set.of(
            "NHAP_NVL_SAN_XUAT",
            "XUAT_NVL_SAN_XUAT",
            "NHAP_THANH_PHAM_SAN_XUAT",
            "XUAT_THANH_PHAM_SAN_XUAT"
    );

    private final WarehouseProductionHistoryService service;

    public WarehouseProductionHistoryController(
            WarehouseProductionHistoryService service) {

        this.service = service;
    }


    // =====================================================
    // LỊCH SỬ KHO SẢN XUẤT
    // =====================================================

    @GetMapping("/kho-san-xuat/lich-su")
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

        if (from != null && to != null && to.isBefore(from)) {
            throw new IllegalArgumentException(
                    "Ngày kết thúc phải sau ngày bắt đầu."
            );
        }

        if (!type.isBlank()
                && !TRANSACTION_TYPES.contains(type)) {

            throw new IllegalArgumentException(
                    "Loại giao dịch kho sản xuất không hợp lệ."
            );
        }

        List<Map<String, Object>> rows =
                service.history(
                        from,
                        to,
                        kind,
                        type
                );

        model.addAttribute("rows", rows);

        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("kind", kind);
        model.addAttribute("type", type);

        return "mrp/warehouse-production-history";
    }


    // =====================================================
    // CHI TIẾT CHỨNG TỪ
    //
    // Ví dụ:
    // /kho-san-xuat/lich-su/chi-tiet
    //      ?sourceTable=ketqua_sanxuat
    //      &documentId=24
    // =====================================================

    @GetMapping("/kho-san-xuat/lich-su/chi-tiet")
    public String detail(

            @RequestParam String sourceTable,

            @RequestParam long documentId,

            Model model) {

        List<Map<String, Object>> rows =
                service.detail(
                        sourceTable,
                        documentId
                );

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không tìm thấy lịch sử của chứng từ."
            );
        }

        Map<String, Object> documentInfo = rows.get(0);

        model.addAttribute("rows", rows);

        model.addAttribute(
                "documentInfo",
                documentInfo
        );

        model.addAttribute(
                "sourceTable",
                sourceTable
        );

        model.addAttribute(
                "documentId",
                documentId
        );
        model.addAttribute(
                "documentTypeLabel",
                switch (sourceTable) {
                    case "phieuxuat_nvl" -> "Nhận nguyên vật liệu từ kho tổng";
                    case "ghi_nhan_san_xuat" -> "Ghi nhận sản xuất";
                    case "ketqua_sanxuat" -> "Bàn giao thành phẩm về kho tổng";
                    default -> "Chứng từ kho sản xuất";
                }
        );

        return "mrp/warehouse-production-history-detail";
    }
}
