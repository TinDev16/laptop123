package com.example.demo.Controller;

import com.example.demo.Service.WarehouseReportService;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WarehouseReportController {
    private final WarehouseReportService service;

    public WarehouseReportController(WarehouseReportService service) {
        this.service = service;
    }

    @GetMapping("/kho/lich-su")
    public String history(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                          @RequestParam(defaultValue = "") String kind, @RequestParam(defaultValue = "") String type,
                          Model model) {
        model.addAttribute("rows", service.history(from, to, kind, type));
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("kind", kind);
        model.addAttribute("type", type);
        return "mrp/warehouse-history";
    }

    @GetMapping("/baocao")
    public String report(@RequestParam(defaultValue = "month") String period,
                         @RequestParam(required = false) Integer month,
                         @RequestParam(required = false) Integer quarter,
                         @RequestParam(required = false) Integer year,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                         Model model) {
        LocalDate today = LocalDate.now();
        int selectedYear = year == null ? today.getYear() : year;
        if (from == null || to == null) {
            if ("year".equals(period)) {
                from = LocalDate.of(selectedYear, 1, 1);
                to = LocalDate.of(selectedYear, 12, 31);
            } else if ("quarter".equals(period)) {
                int selectedQuarter = quarter == null ? (today.getMonthValue() - 1) / 3 + 1 : quarter;
                if (selectedQuarter < 1 || selectedQuarter > 4) throw new IllegalArgumentException("Quý không hợp lệ.");
                from = LocalDate.of(selectedYear, (selectedQuarter - 1) * 3 + 1, 1);
                to = from.plusMonths(3).minusDays(1);
            } else {
                int selectedMonth = month == null ? today.getMonthValue() : month;
                if (selectedMonth < 1 || selectedMonth > 12) throw new IllegalArgumentException("Tháng không hợp lệ.");
                from = LocalDate.of(selectedYear, selectedMonth, 1);
                to = from.plusMonths(1).minusDays(1);
            }
        }
        if (to.isBefore(from)) throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu.");
        Map<String, BigDecimal> totals = service.totals(from, to);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("period", period);
        model.addAttribute("totals", totals);
        model.addAttribute("rows", service.history(from, to, "", ""));
        return "mrp/report";
    }
}
