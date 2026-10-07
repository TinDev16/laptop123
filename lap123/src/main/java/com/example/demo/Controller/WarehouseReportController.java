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

@Controller
public class WarehouseReportController {

    private static final Set<String> REPORT_TRANSACTION_TYPES = Set.of(
            "NHAP_NVL",
            "XUAT_NVL",
            "NHAP_THANH_PHAM",
            "XUAT_THANH_PHAM"
    );

    private final WarehouseReportService service;

    public WarehouseReportController(
            WarehouseReportService service) {

        this.service = service;
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

        return "mrp/warehouse-history-detail";
    }


    // =====================================================
    // BÁO CÁO
    // =====================================================

    @GetMapping("/baocao")
    public String report(
            @RequestParam(required = false)
            Integer month,

            @RequestParam(required = false)
            Integer quarter,

            @RequestParam(required = false)
            Integer year,

            @RequestParam(defaultValue = "")
            String type,

            Model model) {

        LocalDate today = LocalDate.now();

        int selectedYear =
                year == null
                        ? today.getYear()
                        : year;

        LocalDate from;
        LocalDate to;

        // =====================================================
        // ƯU TIÊN THÁNG
        //
        // Có tháng -> lấy tháng đó.
        // Năm có thì lấy năm đã nhập.
        // Không có năm -> lấy năm hiện tại.
        //
        // Nếu vừa có tháng + quý -> vẫn ưu tiên tháng.
        // =====================================================

        if (month != null) {

            if (month < 1 || month > 12) {
                throw new IllegalArgumentException(
                        "Tháng không hợp lệ."
                );
            }

            from = LocalDate.of(
                    selectedYear,
                    month,
                    1
            );

            to = from
                    .plusMonths(1)
                    .minusDays(1);
        }

        // =====================================================
        // KHÔNG CÓ THÁNG -> XÉT QUÝ
        //
        // Có quý -> lấy quý đó.
        // Năm có thì lấy năm đã nhập.
        // Không có năm -> lấy năm hiện tại.
        // =====================================================

        else if (quarter != null) {

            if (quarter < 1 || quarter > 4) {
                throw new IllegalArgumentException(
                        "Quý không hợp lệ."
                );
            }

            from = LocalDate.of(
                    selectedYear,
                    (quarter - 1) * 3 + 1,
                    1
            );

            to = from
                    .plusMonths(3)
                    .minusDays(1);
        }

        // =====================================================
        // KHÔNG CÓ THÁNG, KHÔNG CÓ QUÝ
        //
        // Có năm -> lấy cả năm đó.
        // Không có gì -> mặc định tháng hiện tại.
        // =====================================================

        else if (year != null) {

            from = LocalDate.of(
                    year,
                    1,
                    1
            );

            to = LocalDate.of(
                    year,
                    12,
                    31
            );
        }

        else {

            // Mặc định khi mở trang:
            // tháng hiện tại + năm hiện tại

            from = LocalDate.of(
                    today.getYear(),
                    today.getMonthValue(),
                    1
            );

            to = from
                    .plusMonths(1)
                    .minusDays(1);
        }


        // =====================================================
        // VALIDATE LOẠI GIAO DỊCH
        // =====================================================

        if (!type.isBlank()
                && !REPORT_TRANSACTION_TYPES.contains(type)) {

            throw new IllegalArgumentException(
                    "Loại giao dịch báo cáo không hợp lệ."
            );
        }


        // =====================================================
        // LẤY DỮ LIỆU
        // =====================================================

        Map<String, BigDecimal> totals =
                service.totals(
                        from,
                        to
                );

        Map<String, BigDecimal> currentStock =
                service.stockAt(to);


        // =====================================================
        // GỬI SANG VIEW
        // =====================================================

        model.addAttribute(
                "from",
                from
        );

        model.addAttribute(
                "to",
                to
        );

        model.addAttribute(
                "month",
                month
        );

        model.addAttribute(
                "quarter",
                quarter
        );

        model.addAttribute(
                "year",
                year
        );

        model.addAttribute(
                "totals",
                totals
        );

        model.addAttribute(
                "currentStock",
                currentStock
        );

        model.addAttribute(
                "selectedType",
                type
        );

        model.addAttribute(
                "showDetails",
                !type.isBlank()
        );

        model.addAttribute(
                "rows",
                type.isBlank()
                        ? List.of()
                        : service.history(
                        from,
                        to,
                        "",
                        type
                )
        );

        return "mrp/report";
    }
}