package com.example.demo.Controller;

import com.example.demo.Service.MrpDocumentService;
import com.example.demo.Service.MrpWarehouseService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MrpWorkflowController {
    private final MrpDocumentService documents;
    private final MrpWarehouseService warehouse;

    public MrpWorkflowController(MrpDocumentService documents, MrpWarehouseService warehouse) {
        this.documents = documents;
        this.warehouse = warehouse;
    }

    private boolean manager(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(a -> "ROLE_QUAN_LY_KHO".equals(a.getAuthority()));
    }

    private void requireManager(Authentication authentication) {
        if (!manager(authentication)) throw new AccessDeniedException("Chỉ quản lý kho được xử lý phiếu.");
    }

    @GetMapping("/kho/nhap-nvl")
    public String receipts(Model model) {
        model.addAttribute("receipts", documents.receipts());
        return "mrp/receipts";
    }

    @GetMapping("/kho/nhap-nvl/create")
    public String receiptForm(Model model) {
        model.addAttribute("suppliers", documents.suppliers());
        model.addAttribute("materials", documents.materials());
        return "mrp/receipt-form";
    }

    @PostMapping("/kho/nhap-nvl")
    public String createReceipt(Authentication auth, @RequestParam int supplierId,
                                @RequestParam(required = false) String note,
                                @RequestParam List<Long> materialId, @RequestParam List<Integer> quantity,
                                @RequestParam List<BigDecimal> price, RedirectAttributes flash) {
        try {
            long id = documents.createReceipt(auth.getName(), supplierId, note, materialId, quantity, price);
            flash.addFlashAttribute("success", "Tạo phiếu nhập thành công.");
            return "redirect:/kho/nhap-nvl/" + id;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
            return "redirect:/kho/nhap-nvl/create";
        }
    }

    @GetMapping("/kho/nhap-nvl/{id}")
    public String receipt(@PathVariable long id, Model model) {
        model.addAttribute("receipt", documents.receipt(id));
        model.addAttribute("lines", documents.receiptLines(id));
        return "mrp/receipt-detail";
    }

    @PostMapping("/kho/nhap-nvl/{id}/confirm")
    public String confirmReceipt(@PathVariable long id, RedirectAttributes flash) {
        try {
            warehouse.completeMaterialReceipt(id);
            flash.addFlashAttribute("success", "Nhập nguyên vật liệu thành công.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/kho/nhap-nvl/" + id;
    }

        @PostMapping("/kho/nhap-nvl/{id}/delete")
    public String deleteDraftReceipt(@PathVariable long id, RedirectAttributes flash) {
        try {
            documents.deleteDraftReceipt(id);
            flash.addFlashAttribute("success", "Đã xóa phiếu nhập bản nháp.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/kho/nhap-nvl";
    }
@GetMapping("/kho/xuat-nvl")
    public String materialIssues(Authentication auth, Model model) {
        model.addAttribute("manager", manager(auth));
        model.addAttribute("issues", documents.materialIssues(auth.getName(), manager(auth)));
        return "mrp/material-issues";
    }

    @GetMapping("/kho/xuat-nvl/create")
    public String materialIssueForm(Authentication auth, @RequestParam(required = false) Long orderId, Model model) {
        model.addAttribute("orders", documents.assignedOrders(auth.getName()));
        model.addAttribute("selectedOrder", orderId);
        if (orderId != null) model.addAttribute("lines", documents.bomRequestLines(orderId, auth.getName()));
        return "mrp/material-issue-form";
    }

    @PostMapping("/kho/xuat-nvl")
    public String createMaterialIssue(Authentication auth, @RequestParam long orderId,
                                      @RequestParam(required = false) String note,
                                      @RequestParam List<Long> materialId, @RequestParam List<Integer> quantity,
                                      RedirectAttributes flash) {
        try {
            long id = documents.createMaterialIssue(auth.getName(), orderId, note, materialId, quantity);
            flash.addFlashAttribute("success", "Đã gửi đề nghị xuất NVL.");
            return "redirect:/kho/xuat-nvl/" + id;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
            return "redirect:/kho/xuat-nvl/create?orderId=" + orderId;
        }
    }

    @GetMapping("/kho/xuat-nvl/{id}")
    public String materialIssue(@PathVariable long id, Authentication auth, Model model) {
        model.addAttribute("issue", documents.materialIssue(id, auth.getName(), manager(auth)));
        model.addAttribute("lines", documents.materialIssueLines(id));
        model.addAttribute("manager", manager(auth));
        return "mrp/material-issue-detail";
    }

    @PostMapping("/kho/xuat-nvl/{id}/duyet")
    public String approveMaterialIssue(@PathVariable long id, Authentication auth, RedirectAttributes flash) {
        requireManager(auth);
        try {
            warehouse.issueMaterials(id, auth.getName());
            flash.addFlashAttribute("success", "Duyệt và xuất NVL thành công.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/kho/xuat-nvl/" + id;
    }

    @PostMapping("/kho/xuat-nvl/{id}/tu-choi")
    public String rejectMaterialIssue(@PathVariable long id, @RequestParam String reason,
                                      Authentication auth, RedirectAttributes flash) {
        requireManager(auth);
        try {
            warehouse.rejectMaterialIssue(id, reason, auth.getName());
            flash.addFlashAttribute("success", "Đã từ chối phiếu xuất NVL.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/kho/xuat-nvl/" + id;
    }

    @GetMapping("/san-xuat/ket-qua")
    public String results(Authentication auth, Model model) {
        model.addAttribute("manager", manager(auth));
        model.addAttribute("results", documents.results(auth.getName(), manager(auth)));
        return "mrp/results";
    }

    @GetMapping("/san-xuat/ket-qua/create")
    public String resultForm(Authentication auth, Model model) {
        model.addAttribute("orders", documents.assignedOrders(auth.getName()));
        return "mrp/result-form";
    }

    @PostMapping("/san-xuat/ket-qua")
    public String createResult(Authentication auth, @RequestParam long orderId, @RequestParam int completed,
                               @RequestParam(required = false) String note,
                               RedirectAttributes flash) {
        try {
            long id = documents.createResult(auth.getName(), orderId, completed, 0, note);
            flash.addFlashAttribute("success", "Đã gửi kết quả sản xuất để xác nhận.");
            return "redirect:/san-xuat/ket-qua/" + id;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
            return "redirect:/san-xuat/ket-qua/create";
        }
    }

    @GetMapping("/san-xuat/ket-qua/{id}")
    public String result(@PathVariable long id, Authentication auth, Model model) {
        model.addAttribute("result", documents.result(id, auth.getName(), manager(auth)));
        model.addAttribute("manager", manager(auth));
        return "mrp/result-detail";
    }

    @PostMapping("/san-xuat/ket-qua/{id}/confirm")
    public String confirmResult(@PathVariable long id, Authentication auth, RedirectAttributes flash) {
        requireManager(auth);
        try {
            warehouse.confirmProduction(id, auth.getName());
            flash.addFlashAttribute("success", "Xác nhận nhập thành phẩm thành công.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/san-xuat/ket-qua/" + id;
    }

    @GetMapping("/kho/xuat-thanh-pham")
    public String finishedIssues(Authentication auth, Model model) {
        model.addAttribute("manager", manager(auth));
        model.addAttribute("issues", documents.finishedIssues(auth.getName(), manager(auth)));
        return "mrp/finished-issues";
    }

    @GetMapping("/kho/xuat-thanh-pham/create")
    public String finishedIssueForm(Model model) {
        model.addAttribute("products", documents.products());
        return "mrp/finished-issue-form";
    }

    @PostMapping("/kho/xuat-thanh-pham")
    public String createFinishedIssue(Authentication auth, @RequestParam int productId,
                                      @RequestParam int quantity, @RequestParam(required = false) String note,
                                      RedirectAttributes flash) {
        try {
            long id = documents.createFinishedIssue(auth.getName(), productId, quantity, note);
            flash.addFlashAttribute("success", "Đã gửi đề nghị xuất thành phẩm.");
            return "redirect:/kho/xuat-thanh-pham/" + id;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
            return "redirect:/kho/xuat-thanh-pham/create";
        }
    }

    @GetMapping("/kho/xuat-thanh-pham/{id}")
    public String finishedIssue(@PathVariable long id, Authentication auth, Model model) {
        model.addAttribute("issue", documents.finishedIssue(id, auth.getName(), manager(auth)));
        model.addAttribute("manager", manager(auth));
        return "mrp/finished-issue-detail";
    }

    @PostMapping("/kho/xuat-thanh-pham/{id}/duyet")
    public String approveFinishedIssue(@PathVariable long id, Authentication auth, RedirectAttributes flash) {
        requireManager(auth);
        try {
            warehouse.issueFinishedProducts(id, auth.getName());
            flash.addFlashAttribute("success", "Duyệt và xuất thành phẩm thành công.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/kho/xuat-thanh-pham/" + id;
    }

    @PostMapping("/kho/xuat-thanh-pham/{id}/tu-choi")
    public String rejectFinishedIssue(@PathVariable long id, @RequestParam String reason,
                                      Authentication auth, RedirectAttributes flash) {
        requireManager(auth);
        try {
            warehouse.rejectFinishedIssue(id, reason, auth.getName());
            flash.addFlashAttribute("success", "Đã từ chối phiếu xuất thành phẩm.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/kho/xuat-thanh-pham/" + id;
    }
}
