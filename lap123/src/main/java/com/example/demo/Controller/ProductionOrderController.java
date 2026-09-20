package com.example.demo.Controller;

import com.example.demo.Service.ProductionOrderService;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
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

    private boolean manager(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> "ROLE_QUAN_LY_KHO".equals(a.getAuthority()));
    }

    @GetMapping
    public String list(Authentication auth, Model model) {
        model.addAttribute("orders", service.list(auth.getName(), manager(auth)));
        model.addAttribute("manager", manager(auth));
        return "mrp/orders";
    }

    @GetMapping("/create")
    public String createForm(Authentication auth, Model model) {
        if (!manager(auth)) return "redirect:/san-xuat/yeu-cau";
        model.addAttribute("boms", service.activeBoms());
        model.addAttribute("employees", service.employees());
        return "mrp/order-form";
    }

    @PostMapping
    public String create(Authentication auth, @RequestParam long bomId, @RequestParam int employeeId,
                         @RequestParam int quantity,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline,
                         @RequestParam(required = false) String note, RedirectAttributes redirect) {
        if (!manager(auth)) throw new org.springframework.security.access.AccessDeniedException("Không có quyền tạo yêu cầu sản xuất.");
        try {
            service.create(auth.getName(), bomId, employeeId, quantity, deadline, note);
            return "redirect:/san-xuat/yeu-cau";
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
            return "redirect:/san-xuat/yeu-cau/create";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable long id, Authentication auth, Model model) {
        Map<String, Object> order = service.get(id, auth.getName(), manager(auth));
        model.addAttribute("order", order);
        model.addAttribute("needs", service.materialNeeds(order));
        return "mrp/order-detail";
    }
}
