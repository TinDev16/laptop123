package com.example.demo.Controller;

import com.example.demo.Service.GhiNhanSanXuatService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/san-xuat/ghi-nhan")
public class GhiNhanSanXuatController {

    private final GhiNhanSanXuatService service;

    public GhiNhanSanXuatController(GhiNhanSanXuatService service) {
        this.service = service;
    }

    @GetMapping
    public String index(Authentication auth, Model model) {
        model.addAttribute("orders", service.getOrders(auth.getName()));
        return "mrp/ghi-nhan-san-xuat";
    }

    @GetMapping("/materials")
    @ResponseBody
    public Object materials(@RequestParam long bomId) {
        return service.getBomDetails(bomId);
    }

    @PostMapping
    public String record(Authentication auth,
                         @RequestParam long orderId,
                         @RequestParam long bomId,
                         @RequestParam int quantity,
                         RedirectAttributes flash) {
        try {
            service.recordProduction(
                    auth.getName(),
                    orderId,
                    bomId,
                    quantity
            );

            flash.addFlashAttribute(
                    "success",
                    "Ghi nhận sản xuất thành công. NVL đã được trừ và thành phẩm đã được nhập vào kho sản xuất. Tiến độ YCSX sẽ chỉ cập nhật sau khi quản lý xác nhận kết quả."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {
            flash.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/san-xuat/ghi-nhan";
    }
}