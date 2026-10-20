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
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.TreeSet;

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
    public String index(
            Authentication auth,
            @RequestParam(required = false) Long orderId,
            Model model) {

        model.addAttribute(
                "orders",
                service.getOrders(auth.getName())
        );

        var groups = documents.resultGroups(auth.getName(), false);

        System.out.println("===== DEBUG RESULT GROUPS =====");
        System.out.println("Tong so nhom: " + groups.size());

        for (var group : groups) {
            System.out.println(
                    "KEY=" + group.get("ma_nhom_ghi_nhan")
                            + " | SO_KET_QUA=" + group.get("soketqua")
                            + " | TONG_SL=" + group.get("soluongtong")
            );
        }

        model.addAttribute("resultGroups", groups);
        model.addAttribute("selectedOrderId", orderId);

        return "mrp/ghi-nhan-san-xuat";
    }

    @GetMapping("/materials")
    @ResponseBody
    public Object materials(@RequestParam long bomId) {
        return service.getBomDetails(bomId);
    }

    @PostMapping
    public String record(
            Authentication auth,
            @RequestParam Map<String, String> params,
            @RequestParam(required = false) String note,
            RedirectAttributes flash) {

        try {
            Set<Integer> indexes = new TreeSet<>();

            // Tìm các dòng được gửi từ form: items[0].orderId, items[1].orderId...
            for (String key : params.keySet()) {
                if (key.startsWith("items[") && key.endsWith("].orderId")) {
                    String indexText = key.substring(
                            "items[".length(),
                            key.length() - "].orderId".length()
                    );
                    indexes.add(Integer.parseInt(indexText));
                }
            }

            if (indexes.isEmpty()) {
                throw new IllegalArgumentException(
                        "Vui lòng nhập số lượng cho ít nhất một sản phẩm."
                );
            }

            List<GhiNhanSanXuatService.ProductionItem> items =
                    new ArrayList<>();

            for (Integer index : indexes) {
                String prefix = "items[" + index + "].";

                String orderText = params.get(prefix + "orderId");
                String bomText = params.get(prefix + "bomId");
                String quantityText = params.get(prefix + "quantity");

                if (orderText == null || bomText == null) {
                    throw new IllegalArgumentException(
                            "Thông tin yêu cầu hoặc BOM không hợp lệ."
                    );
                }

                // Không ghi nhận các dòng để trống hoặc bằng 0.
                if (quantityText == null || quantityText.isBlank()) {
                    continue;
                }

                int quantity;
                try {
                    quantity = Integer.parseInt(quantityText.trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Số lượng sản xuất phải là số nguyên hợp lệ."
                    );
                }

                if (quantity == 0) {
                    continue;
                }

                items.add(new GhiNhanSanXuatService.ProductionItem(
                        Long.parseLong(orderText),
                        Long.parseLong(bomText),
                        quantity
                ));
            }

            if (items.isEmpty()) {
                throw new IllegalArgumentException(
                        "M chưa nhập số lượng sản xuất cho sản phẩm nào."
                );
            }

            List<Long> resultIds = service.recordProductionBatch(
                    auth.getName(),
                    items,
                    note
            );

            flash.addFlashAttribute(
                    "success",
                    "Ghi nhận thành công " + resultIds.size()
                            + " sản phẩm/yêu cầu. Các kết quả đã được gửi quản lý xác nhận."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {
            String message = e.getMessage();

            if (message != null &&
                    (message.toLowerCase().contains("không đủ")
                            || message.toLowerCase().contains("khong du")
                            || message.toLowerCase().contains("insufficient"))) {
                flash.addFlashAttribute("error", message);
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
