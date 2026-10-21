package com.example.demo.Controller;

import com.example.demo.Service.ProductionOrderService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.example.demo.Service.MrpDocumentService;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/san-xuat/yeu-cau")
public class ProductionOrderController {

    private final ProductionOrderService service;
    private final MrpDocumentService mrpDocumentService;

    public ProductionOrderController(
            ProductionOrderService service,
            MrpDocumentService mrpDocumentService
    ) {
        this.service = service;
        this.mrpDocumentService = mrpDocumentService;
    }

    // =========================================================
    // KIỂM TRA QUYỀN QUẢN LÝ KHO
    // =========================================================

    private boolean manager(Authentication auth) {
        return auth.getAuthorities()
                .stream()
                .anyMatch(a ->
                        "ROLE_QUAN_LY_KHO".equals(
                                a.getAuthority()
                        )
                );
    }

    // =========================================================
    // DANH SÁCH YÊU CẦU SẢN XUẤT
    // =========================================================

    @GetMapping
    public String list(
            Authentication auth,
            Model model,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") String trangthai
    ) {
        boolean isManager = manager(auth);

        List<Map<String, Object>> groupedOrders =
                service.listGrouped(auth.getName(), isManager, search);

        // Lọc danh sách theo trạng thái KPI sau khi đã gom nhóm theo đợt.
        if (!trangthai.isBlank()) {
            groupedOrders = groupedOrders.stream()
                    .filter(order -> {
                        String status = String.valueOf(order.get("trangthai"));

                        return switch (trangthai) {
                            case "DANG_SAN_XUAT" ->
                                    "DANG_THUC_HIEN".equals(status);

                            case "HOAN_THANH" ->
                                    "HOAN_THANH".equals(status);

                            case "HUY" ->
                                    "HUY".equals(status);

                            default -> true;
                        };
                    })
                    .toList();
        }

        model.addAttribute("orders", groupedOrders);
        model.addAttribute("manager", isManager);
        model.addAttribute("search", search);
        model.addAttribute("trangthai", trangthai);

        return "mrp/orders";
    }

    // =========================================================
    // FORM TẠO YÊU CẦU
    // =========================================================

    @GetMapping("/create")
    public String createForm(
            Authentication auth,
            Model model
    ) {
        if (!manager(auth)) {
            return "redirect:/san-xuat/yeu-cau";
        }

        model.addAttribute("products", service.activeProducts());
        model.addAttribute("employees", service.employees());

        return "mrp/order-form";
    }

    @GetMapping("/products/{productId}/boms")
    @ResponseBody
    public List<Map<String, Object>> getBomsByProduct(
            @PathVariable long productId,
            Authentication auth
    ) {
        if (!manager(auth)) {
            throw new AccessDeniedException("Không có quyền xem BOM sản xuất.");
        }

        return service.activeBomsByProduct(productId);
    }

    // =========================================================
    // TẠO YÊU CẦU
    // =========================================================

    @PostMapping
    public String create(
            Authentication auth,
            @RequestParam String batchName,
            @RequestParam List<Long> bomId,
            @RequestParam List<Integer> employeeId,
            @RequestParam List<Integer> quantity,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            List<LocalDate> deadline,
            @RequestParam(required = false) String note,
            RedirectAttributes redirect
    ) {
        if (!manager(auth)) {
            throw new AccessDeniedException(
                    "Không có quyền tạo yêu cầu sản xuất."
            );
        }

        try {
            int created = service.createBatch(
                    auth.getName(),
                    batchName,
                    bomId,
                    employeeId,
                    quantity,
                    deadline,
                    note
            );

            redirect.addFlashAttribute("createdCount", created);
            redirect.addFlashAttribute(
                    "success",
                    "Tạo yêu cầu sản xuất thành công."
            );

            return "redirect:/san-xuat/yeu-cau";

        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );

            return "redirect:/san-xuat/yeu-cau/create";
        }
    }

    // =========================================================
    // CHI TIẾT YÊU CẦU
    // =========================================================

    @GetMapping("/{id}")
    public String detail(
            @PathVariable long id,
            Authentication auth,
            Model model
    ) {
        boolean isManager = manager(auth);

        Map<String, Object> order =
                service.get(
                        id,
                        auth.getName(),
                        isManager
                );

        model.addAttribute(
                "order",
                order
        );

        var needs = service.materialNeeds(order);
        long shortageCount = needs.stream()
                .filter(need -> need.shortage().signum() > 0)
                .count();

        model.addAttribute("needs", needs);
        model.addAttribute("shortageCount", shortageCount);
        model.addAttribute("hasMaterialShortage", shortageCount > 0);

        model.addAttribute(
                "manager",
                isManager
        );
        model.addAttribute(
                "pendingResultCount",
                isManager ? service.pendingResultCount(id) : 0
        );

        return "mrp/order-detail";
    }

    @GetMapping("/dot/{batchId}")
    public String batchDetail(
            @PathVariable Long batchId,
            Authentication auth,
            Model model
    ) {
        boolean isManager = manager(auth);
        String email = auth.getName();

        model.addAttribute(
                "batch",
                service.getBatchInfo(batchId, email, isManager)
        );

        model.addAttribute(
                "orders",
                service.getBatchOrders(batchId, email, isManager)
        );

        model.addAttribute("manager", isManager);

        // Kiểm tra đợt có kết quả sản xuất đang chờ duyệt hay không.
        model.addAttribute(
                "batchHasPendingResults",
                isManager && mrpDocumentService.hasPendingBatchResults(batchId)
        );

        if (!isManager) {
            List<Map<String, Object>> batchMaterialLines =
                    mrpDocumentService.bomRequestLinesByBatch(
                            batchId,
                            email
                    );

            boolean hasShortage = batchMaterialLines.stream()
                    .anyMatch(line ->
                            line.get("thieu_hut") != null
                                    && ((Number) line.get("thieu_hut")).doubleValue() > 0
                    );

            boolean pendingIssue =
                    mrpDocumentService.hasPendingBatchMaterialIssue(
                            batchId,
                            email
                    );

            // Kiểm tra còn yêu cầu nào chưa sản xuất đủ hay không.
            List<Map<String, Object>> batchOrders =
                    service.getBatchOrders(batchId, email, false);

            boolean hasRemainingProduction = batchOrders.stream()
                    .anyMatch(order -> {
                        String status = String.valueOf(order.get("trangthai"));

                        if ("HUY".equals(status)
                                || "HOAN_THANH".equals(status)) {
                            return false;
                        }

                        Object remainingValue =
                                order.get("soluongconphaisanxuat");

                        return remainingValue instanceof Number
                                && ((Number) remainingValue).doubleValue() > 0;
                    });

            model.addAttribute("batchMaterialLines", batchMaterialLines);
            model.addAttribute("batchHasShortage", hasShortage);
            model.addAttribute("batchPendingIssue", pendingIssue);
            model.addAttribute("batchHasRemainingProduction", hasRemainingProduction);
        }

        return "mrp/batch-detail";
    }

    // =========================================================
    // FORM SỬA YÊU CẦU
    // GET /san-xuat/yeu-cau/{id}/edit
    // =========================================================

    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable long id,
            Authentication auth,
            Model model,
            RedirectAttributes redirect
    ) {
        if (!manager(auth)) {
            throw new AccessDeniedException(
                    "Không có quyền chỉnh sửa yêu cầu sản xuất."
            );
        }

        try {
            Map<String, Object> order =
                    service.getForEdit(id);

            String status =
                    String.valueOf(
                            order.get("trangthai")
                    );

            // Không cho mở form sửa nếu đã hoàn thành
            // hoặc đã hủy.
            if ("HOAN_THANH".equals(status) ||
                    "HUY".equals(status)) {

                redirect.addFlashAttribute(
                        "error",
                        "Yêu cầu này không thể chỉnh sửa."
                );

                return "redirect:/san-xuat/yeu-cau/" + id;
            }

            model.addAttribute(
                    "order",
                    order
            );

            model.addAttribute(
                    "employees",
                    service.employees()
            );

            return "mrp/order-edit";

        } catch (IllegalArgumentException exception) {

            redirect.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );

            return "redirect:/san-xuat/yeu-cau";
        }
    }

    // =========================================================
    // XỬ LÝ SỬA YÊU CẦU
    // POST /san-xuat/yeu-cau/{id}/edit
    // =========================================================

    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable long id,
            Authentication auth,
            @RequestParam int quantity,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate deadline,
            @RequestParam int employeeId,
            @RequestParam(required = false) String note,
            RedirectAttributes redirect
    ) {
        if (!manager(auth)) {
            throw new AccessDeniedException(
                    "Không có quyền chỉnh sửa yêu cầu sản xuất."
            );
        }

        try {
            // Lấy mã đợt trước khi cập nhật
            Map<String, Object> order = service.getForEdit(id);
            Object madot = order.get("madot");

            // Cập nhật yêu cầu
            service.update(
                    auth.getName(),
                    id,
                    quantity,
                    deadline,
                    employeeId,
                    note
            );

            redirect.addFlashAttribute(
                    "success",
                    "Cập nhật yêu cầu sản xuất thành công."
            );

            // Quay lại trang chi tiết đợt sản xuất
            if (madot != null) {
                return "redirect:/san-xuat/yeu-cau/dot/" + madot;
            }

            // Yêu cầu không thuộc đợt nào
            return "redirect:/san-xuat/yeu-cau";

        } catch (Exception e) {
            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/san-xuat/yeu-cau/" + id + "/edit";
        }
    }

    // =========================================================
    // HỦY YÊU CẦU
    // POST /san-xuat/yeu-cau/{id}/cancel
    // =========================================================

    // =========================================================
// HỦY YÊU CẦU
// POST /san-xuat/yeu-cau/{id}/cancel
// =========================================================
    @PostMapping("/{id}/cancel")
    public String cancel(
            @PathVariable long id,
            Authentication auth,
            @RequestParam String reason,
            RedirectAttributes redirect
    ) {
        if (!manager(auth)) {
            throw new AccessDeniedException(
                    "Không có quyền hủy yêu cầu sản xuất."
            );
        }

        try {
            // Lấy mã đợt sản xuất trước khi hủy yêu cầu
            Map<String, Object> order = service.getForEdit(id);
            Object madot = order.get("madot");

            // Thực hiện hủy yêu cầu
            service.cancel(
                    auth.getName(),
                    id,
                    reason
            );

            redirect.addFlashAttribute(
                    "success",
                    "Đã hủy yêu cầu sản xuất."
            );

            // Quay lại đúng trang chi tiết đợt sản xuất
            if (madot != null) {
                return "redirect:/san-xuat/yeu-cau/dot/" + madot;
            }

        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );

            // Khi lỗi, vẫn ưu tiên quay lại trang đợt sản xuất
            try {
                Map<String, Object> order = service.getForEdit(id);
                Object madot = order.get("madot");

                if (madot != null) {
                    return "redirect:/san-xuat/yeu-cau/dot/" + madot;
                }
            } catch (IllegalArgumentException ignored) {
                // Không tìm thấy yêu cầu hoặc đợt sản xuất
            }
        }

        // Yêu cầu không thuộc đợt nào
        return "redirect:/san-xuat/yeu-cau";
    }

    @PostMapping("/{id}/repair-result")
    public String repairMissingResult(
            @PathVariable long id,
            Authentication auth,
            RedirectAttributes redirect
    ) {
        try {
            long resultId = service.repairMissingProductionResult(
                    id,
                    auth.getName(),
                    manager(auth)
            );
            redirect.addFlashAttribute(
                    "success",
                    "Đã khôi phục kết quả sản xuất từ sản lượng ghi nhận tại xưởng."
            );

            return manager(auth)
                    ? "redirect:/san-xuat/ket-qua/" + resultId
                    : "redirect:/san-xuat/yeu-cau/" + id;

        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
            return "redirect:/san-xuat/yeu-cau/" + id;
        }
    }

    // =========================================================
// HỦY TOÀN BỘ ĐỢT SẢN XUẤT
// POST /san-xuat/yeu-cau/dot/{batchId}/cancel
// =========================================================
    @PostMapping("/dot/{batchId}/cancel")
    public String cancelBatch(
            @PathVariable Long batchId,
            Authentication auth,
            @RequestParam String reason,
            RedirectAttributes redirect
    ) {
        if (!manager(auth)) {
            throw new AccessDeniedException(
                    "Không có quyền hủy đợt sản xuất."
            );
        }

        try {
            // Hủy toàn bộ yêu cầu thuộc đợt.
            // Service phải kiểm tra trạng thái trước khi cập nhật.
            service.cancelBatch(
                    auth.getName(),
                    batchId,
                    reason
            );

            redirect.addFlashAttribute(
                    "success",
                    "Đã hủy toàn bộ yêu cầu trong đợt sản xuất."
            );

        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirect.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );
        }

        return "redirect:/san-xuat/yeu-cau";
    }

    // =========================================================
    // LỊCH SỬ HOẠT ĐỘNG
    // GET /san-xuat/yeu-cau/{id}/history
    // =========================================================

    @GetMapping("/{id}/history")
    public String history(
            @PathVariable long id,
            Authentication auth,
            Model model,
            RedirectAttributes redirect
    ) {
        if (!manager(auth)) {
            throw new AccessDeniedException(
                "Không có quyền xem lịch sử hoạt động."
            );
        }

        try {
            Map<String, Object> order =
                    service.getForEdit(id);

            model.addAttribute(
                    "order",
                    order
            );

            model.addAttribute(
                    "history",
                    service.history(id)
            );

            return "mrp/order-history";

        } catch (IllegalArgumentException exception) {

            redirect.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );

            return "redirect:/san-xuat/yeu-cau";
        }
    }

    @PostMapping("/dot/{batchId}/uncancel")
    public String uncancelBatch(
            @PathVariable Long batchId,
            Authentication auth,
            RedirectAttributes redirect
    ) {
        if (!manager(auth)) {
            throw new AccessDeniedException(
                    "Không có quyền bỏ hủy đợt sản xuất."
            );
        }

        try {
            service.uncancelBatch(
                    auth.getName(),
                    batchId
            );

            redirect.addFlashAttribute(
                    "success",
                    "Đã bỏ hủy đợt sản xuất."
            );

        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );
        }

        return "redirect:/san-xuat/yeu-cau";
    }

    @PostMapping("/{id}/uncancel")
    public String uncancel(
            @PathVariable long id,
            @RequestParam(required = false) Long batchId,
            Authentication auth,
            RedirectAttributes redirect) {

        if (!manager(auth)) {
            throw new AccessDeniedException(
                    "Không có quyền bỏ hủy yêu cầu sản xuất."
            );
        }

        try {
            service.uncancel(auth.getName(), id);
            redirect.addFlashAttribute(
                    "success",
                    "Đã bỏ hủy yêu cầu sản xuất."
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        if (batchId != null) {
            return "redirect:/san-xuat/yeu-cau/dot/" + batchId;
        }

        return "redirect:/san-xuat/yeu-cau";
    }
}
