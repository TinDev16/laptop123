package com.example.demo.Controller;

import com.example.demo.Service.ProductionOrderService;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
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

    public ProductionOrderController(ProductionOrderService service) {
        this.service = service;
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
            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String search
    ) {
        boolean isManager = manager(auth);

        model.addAttribute(
                "orders",
                service.list(
                        auth.getName(),
                        isManager,
                        search
                )
        );

        model.addAttribute(
                "manager",
                isManager
        );

        model.addAttribute(
                "search",
                search
        );

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

        model.addAttribute(
                "boms",
                service.activeBoms()
        );

        model.addAttribute(
                "employees",
                service.employees()
        );

        return "mrp/order-form";
    }

    // =========================================================
    // TẠO YÊU CẦU
    // =========================================================

    @PostMapping
    public String create(
            Authentication auth,
            @RequestParam long bomId,
            @RequestParam int employeeId,
            @RequestParam int quantity,
            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate deadline,
            @RequestParam(required = false) String note,
            RedirectAttributes redirect
    ) {
        if (!manager(auth)) {
            throw new AccessDeniedException(
                    "Không có quyền tạo yêu cầu sản xuất."
            );
        }

        try {
            service.create(
                    auth.getName(),
                    bomId,
                    employeeId,
                    quantity,
                    deadline,
                    note
            );

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

        model.addAttribute(
                "needs",
                service.materialNeeds(order)
        );

        model.addAttribute(
                "manager",
                isManager
        );

        return "mrp/order-detail";
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
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
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

            return "redirect:/san-xuat/yeu-cau/" + id;

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
            service.cancel(
                    auth.getName(),
                    id,
                    reason
            );

            redirect.addFlashAttribute(
                    "success",
                    "Đã hủy yêu cầu sản xuất."
            );

        } catch (IllegalArgumentException exception) {

            redirect.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );
        }

        return "redirect:/san-xuat/yeu-cau/" + id;
    }

    // =========================================================
    // LỊCH SỬ CHỈNH SỬA
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
                    "Không có quyền xem lịch sử chỉnh sửa."
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
}