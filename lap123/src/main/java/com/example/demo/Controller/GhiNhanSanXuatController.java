package com.example.demo.Controller;

import com.example.demo.Service.GhiNhanSanXuatService;
import com.example.demo.Service.MrpDocumentService;
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
    private final MrpDocumentService documents;

    public GhiNhanSanXuatController(
            GhiNhanSanXuatService service,
            MrpDocumentService documents) {
        this.service = service;
        this.documents = documents;
    }

    @GetMapping
    public String index(Authentication auth, Model model) {
        model.addAttribute("orders", service.getOrders(auth.getName()));
        model.addAttribute("results", documents.results(auth.getName(), false));
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
                         @RequestParam(required = false) String note,
                         RedirectAttributes flash) {
        try {
            long resultId = service.recordProduction(
                    auth.getName(),
                    orderId,
                    bomId,
                    quantity,
                    note
            );

            flash.addFlashAttribute(
                    "success",
                    "Ghi nhận thành công. Thành phẩm đã vào kho sản xuất và kết quả #"
                            + resultId
                            + " đã được gửi quản lý xác nhận chuyển sang kho tổng."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {
            String message = e.getMessage();

            if (message != null &&
                    (message.toLowerCase().contains("không đủ")
                            || message.toLowerCase().contains("khong du")
                            || message.toLowerCase().contains("insufficient"))) {

                flash.addFlashAttribute(
                        "error",
                        "Nguyên vật liệu không đủ để sản xuất số lượng này! Vui lòng kiểm tra tồn kho sản xuất."
                );

            } else {
                flash.addFlashAttribute(
                        "error",
                        message != null ? message : "Ghi nhận sản xuất thất bại."
                );
            }
        }

        return "redirect:/san-xuat/ghi-nhan";
    }
}
