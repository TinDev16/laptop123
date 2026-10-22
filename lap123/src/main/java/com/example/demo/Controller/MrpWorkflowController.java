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
import java.util.Map;
import org.springframework.web.bind.annotation.ResponseBody;

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

        var materials = documents.materials();

        model.addAttribute(
                "suppliers",
                documents.suppliers()
        );

        model.addAttribute(
                "materials",
                materials
        );

        model.addAttribute(
                "materialGroups",
                documents.groupMaterialsByType(materials)
        );

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
    public String confirmReceipt(
            @PathVariable long id,
            Authentication auth,
            RedirectAttributes flash) {

        requireManager(auth);

        try {
            warehouse.completeMaterialReceipt(id, auth.getName());

            flash.addFlashAttribute(
                    "success",
                    "Nhập nguyên vật liệu thành công."
            );

        } catch (IllegalArgumentException | IllegalStateException exception) {

            flash.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );
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
    public String materialIssueForm(
            Authentication auth,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String returnUrl,
            Model model) {

        model.addAttribute(
                "batches",
                documents.assignedBatches(auth.getName())
        );

        model.addAttribute("selectedBatch", batchId);

        // Lưu đường dẫn để nút quay lại trở về đúng trang trước đó
        model.addAttribute("returnUrl", returnUrl);

        if (batchId != null) {
            model.addAttribute(
                    "lines",
                    documents.bomRequestLinesByBatch(
                            batchId,
                            auth.getName()
                    )
            );
        }

        return "mrp/material-issue-form";
    }

    @PostMapping("/kho/xuat-nvl")
    public String createMaterialIssue(
            Authentication auth,
            @RequestParam long batchId,
            @RequestParam(defaultValue = "DINH_MUC") String loaiXuat,
            @RequestParam(required = false) String note,
            @RequestParam List<Long> materialId,
            @RequestParam List<Integer> quantity,
            RedirectAttributes flash) {

        try {
            long id = documents.createBatchMaterialIssue(
                    auth.getName(),
                    batchId,
                    loaiXuat,
                    note,
                    materialId,
                    quantity
            );

            flash.addFlashAttribute(
                    "success",
                    "XUAT_BU".equalsIgnoreCase(loaiXuat)
                            ? "Đã gửi đề nghị xuất bù linh kiện hư hỏng cho Quản lý phê duyệt."
                            : "Đã gửi đề nghị xuất NVL cho đợt sản xuất."
            );

            return "redirect:/kho/xuat-nvl/" + id;

        } catch (IllegalArgumentException | IllegalStateException exception) {
            flash.addFlashAttribute("error", exception.getMessage());

            return "redirect:/kho/xuat-nvl/create?batchId=" + batchId;
        }
    }


    @GetMapping("/kho/xuat-nvl/{id}")
    public String materialIssue(
            @PathVariable long id,
            Authentication auth,
            Model model) {

        model.addAttribute(
                "issue",
                documents.materialIssue(id, auth.getName(), manager(auth))
        );

        model.addAttribute(
                "requests",
                documents.materialIssueRequests(id)
        );

        model.addAttribute(
                "lines",
                documents.materialIssueLines(id)
        );

        model.addAttribute("manager", manager(auth));

        return "mrp/material-issue-detail";
    }

    @PostMapping("/kho/xuat-nvl/{id}/duyet")
    public String approveMaterialIssue(
            @PathVariable long id,
            Authentication auth,
            RedirectAttributes flash) {

        requireManager(auth);

        try {
            warehouse.issueMaterials(id, auth.getName());

            flash.addFlashAttribute(
                    "success",
                    "Duyệt và xuất NVL thành công."
            );

        } catch (IllegalArgumentException | IllegalStateException exception) {

            String message = exception.getMessage();

            if (message == null || message.isBlank()) {
                message = "Không thể xuất nguyên vật liệu. Vui lòng kiểm tra lại tồn kho.";
            }

            flash.addFlashAttribute("error", message);
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
    public String results(
            Authentication auth,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String trangthai,
            Model model) {

        requireManager(auth);

        List<Map<String, Object>> groups =
                documents.resultGroups(auth.getName(), true);

        List<Map<String, Object>> pendingGroups =
                new java.util.ArrayList<>();

        List<Map<String, Object>> approvedGroups =
                new java.util.ArrayList<>();

        for (Map<String, Object> group : groups) {

            Object rawItems = group.get("items");

            if (!(rawItems instanceof List<?> items)) {
                continue;
            }

            List<Map<String, Object>> filteredItems =
                    new java.util.ArrayList<>();

            boolean hasPending = false;
            boolean chapNhanToanBo = true;
            boolean hasRejected = false;

            for (Object rawItem : items) {

                if (!(rawItem instanceof Map<?, ?> rawMap)) {
                    continue;
                }

                Map<String, Object> item =
                        new java.util.LinkedHashMap<>();

                rawMap.forEach((key, value) ->
                        item.put(String.valueOf(key), value));

                // Lọc theo đợt sản xuất.
                if (batchId != null) {
                    Object rawBatchId = item.get("madot");

                    if (!(rawBatchId instanceof Number number)
                            || number.longValue() != batchId) {
                        continue;
                    }
                }

                // Lọc theo yêu cầu sản xuất.
                if (orderId != null) {
                    Object rawOrderId = item.get("mayeucau");

                    if (!(rawOrderId instanceof Number number)
                            || number.longValue() != orderId) {
                        continue;
                    }
                }

                String status = String.valueOf(item.get("trangthai"));

// Chỉ hiển thị kết quả thuộc trạng thái được yêu cầu.
                if (trangthai != null && !trangthai.isBlank()) {
                    if ("CHO_XAC_NHAN".equals(trangthai)
                            && !"CHO_XAC_NHAN".equals(status)) {
                        continue;
                    }
                }

// Bỏ qua trạng thái không hợp lệ.
                if (!java.util.Set.of(
                        "CHO_XAC_NHAN",
                        "DA_NHAP_KHO",
                        "TU_CHOI_LOI",
                        "TU_CHOI_SAI_YEU_CAU",
                        "HUY"
                ).contains(status)) {
                    continue;
                }

                filteredItems.add(item);

                if ("CHO_XAC_NHAN".equals(status)) {
                    hasPending = true;
                }

                if ("TU_CHOI_LOI".equals(status)
                        || "TU_CHOI_SAI_YEU_CAU".equals(status)) {
                    hasRejected = true;
                }

                if (!"DA_NHAP_KHO".equals(status)) {
                    chapNhanToanBo = false;
                }
            }

            // Không còn kết quả sau khi lọc.
            if (filteredItems.isEmpty()) {
                continue;
            }

            Map<String, Object> resultGroup =
                    new java.util.LinkedHashMap<>(group);

            resultGroup.put("items", filteredItems);
            resultGroup.put("soketqua", filteredItems.size());
            resultGroup.put(
                    "soluongtong",
                    sumResultQuantity(filteredItems)
            );

            if (hasPending) {
                resultGroup.put("trangthai", "CHO_XAC_NHAN");
                pendingGroups.add(resultGroup);
            } else {
                resultGroup.put("trangthai", "DA_DUYET");

                // Tất cả kết quả đều đã nhập kho thành công?
                resultGroup.put("chapNhanToanBo", chapNhanToanBo);

                // Có ít nhất một kết quả bị từ chối?
                resultGroup.put("hasRejected", hasRejected);

                approvedGroups.add(resultGroup);
            }
        }

        model.addAttribute("manager", true);
        model.addAttribute("pendingGroups", pendingGroups);
        model.addAttribute("approvedGroups", approvedGroups);
        model.addAttribute("selectedOrderId", orderId);
        model.addAttribute("selectedBatchId", batchId);
        model.addAttribute("selectedStatus", trangthai);

        return "mrp/results";
    }

    private int sumResultQuantity(List<Map<String, Object>> items) {
        return items.stream()
                .mapToInt(item -> {
                    Object quantity = item.get("soluonghoanthanh");

                    return quantity instanceof Number number
                            ? number.intValue()
                            : 0;
                })
                .sum();
    }

    @GetMapping("/san-xuat/ket-qua/create")
    public String resultForm() {
        return "redirect:/san-xuat/ghi-nhan";
    }

    @PostMapping("/san-xuat/ket-qua")
    public String createResult(
            RedirectAttributes flash) {
        flash.addFlashAttribute(
                "error",
                "Kết quả được tạo tự động khi ghi nhận sản xuất. Không cần nhập lại sản lượng."
        );
        return "redirect:/san-xuat/ghi-nhan";
    }

    @GetMapping("/san-xuat/ket-qua/{id}")
    public String result(@PathVariable long id, Authentication auth, Model model) {
        model.addAttribute("result", documents.result(id, auth.getName(), manager(auth)));
        model.addAttribute("manager", manager(auth));
        return "mrp/result-detail";
    }

    @PostMapping("/san-xuat/ket-qua/{id}/confirm")
    public String confirmResult(
            @PathVariable long id,
            Authentication auth,
            @RequestParam(required = false) String groupKey,
            RedirectAttributes flash) {

        requireManager(auth);

        try {
            warehouse.confirmProduction(id, auth.getName());

            flash.addFlashAttribute(
                    "success",
                    "Xác nhận nhập thành phẩm thành công."
            );

        } catch (IllegalArgumentException | IllegalStateException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
        }

        if (groupKey != null && !groupKey.isBlank()) {
            return "redirect:/san-xuat/ket-qua/nhom/" + groupKey;
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
        model.addAttribute("customers", documents.customers());
        return "mrp/finished-issue-form";
    }

    @GetMapping("/kho/xuat-thanh-pham/bom-price")
    @ResponseBody
    public Map<String, Object> finishedBomPrice(
            @RequestParam int bomId) {
        return documents.finishedBomPrice(bomId);
    }

    @PostMapping("/kho/xuat-thanh-pham")
    public String createFinishedIssue(
            Authentication auth,
            @RequestParam String tenDotDeNghi,
            @RequestParam List<Integer> productId,
            @RequestParam List<Integer> bomId,
            @RequestParam List<Integer> quantity,
            @RequestParam int customerId,
            @RequestParam String address,
            @RequestParam List<BigDecimal> unitPrice,
            @RequestParam List<Boolean> priceFixed,
            @RequestParam(required = false) String note,
            RedirectAttributes flash) {

        try {
            long id = documents.createFinishedIssueBatch(
                    auth.getName(),
                    tenDotDeNghi,
                    productId,
                    bomId,
                    quantity,
                    customerId,
                    address,
                    unitPrice,
                    priceFixed,
                    note
            );

            flash.addFlashAttribute(
                    "success",
                    "Đã gửi đề nghị xuất thành phẩm."
            );

            return "redirect:/kho/xuat-thanh-pham/" + id;

        } catch (IllegalArgumentException | IllegalStateException exception) {

            flash.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );

            return "redirect:/kho/xuat-thanh-pham/create";
        }
    }

    @GetMapping("/kho/xuat-thanh-pham/{id}")
    public String finishedIssue(@PathVariable long id, Authentication auth, Model model) {
        model.addAttribute("issue", documents.finishedIssue(id, auth.getName(), manager(auth)));
        model.addAttribute("lines", documents.finishedIssueLines(id));
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

    @GetMapping("/san-xuat/ket-qua/nhom/{groupKey}")
    public String resultGroup(
            @PathVariable String groupKey,
            @RequestParam(required = false) String returnUrl,
            Authentication auth,
            Model model) {

        boolean isManager = manager(auth);

        // Lấy danh sách nhóm theo đúng quyền của người đang đăng nhập.
        List<Map<String, Object>> groups =
                documents.resultGroups(auth.getName(), isManager);

        // Tìm đúng nhóm được chọn.
        Map<String, Object> selectedGroup = groups.stream()
                .filter(group -> groupKey.equals(
                        String.valueOf(group.get("ma_nhom_ghi_nhan"))
                ))
                .findFirst()
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Không tìm thấy nhóm kết quả hoặc bạn không có quyền xem."
                        )
                );

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> groupResults =
                (List<Map<String, Object>>) selectedGroup.get("items");

        // Chỉ cho phép quay lại trang danh sách kết quả nội bộ.
        String safeReturnUrl = "/san-xuat/ket-qua";

        if (isManager && returnUrl != null) {
            if (returnUrl.equals("/san-xuat/ket-qua")) {
                safeReturnUrl = returnUrl;
            } else if (returnUrl.matches(
                    "^/san-xuat/ket-qua\\?(batchId|orderId)=\\d+$")) {
                safeReturnUrl = returnUrl;
            }
        }

        model.addAttribute("manager", isManager);
        model.addAttribute("group", selectedGroup);
        model.addAttribute("results", groupResults);
        model.addAttribute("returnUrl", safeReturnUrl);

        return "mrp/result-detail";
    }

    @PostMapping("/san-xuat/ket-qua/{id}/reject")
    public String rejectResult(
            @PathVariable long id,
            @RequestParam String rejectionType,
            @RequestParam String reason,
            @RequestParam(required = false) Integer defectQuantity,
            @RequestParam(required = false) String groupKey,
            Authentication auth,
            RedirectAttributes flash) {

        requireManager(auth);

        try {
            warehouse.rejectProduction(
                    id,
                    rejectionType,
                    reason,
                    defectQuantity,
                    auth.getName()
            );

            flash.addFlashAttribute(
                    "success",
                    "Đã xử lý từ chối kết quả sản xuất."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }

        if (groupKey != null && !groupKey.isBlank()) {
            return "redirect:/san-xuat/ket-qua/nhom/" + groupKey;
        }

        return "redirect:/san-xuat/ket-qua/" + id;
    }

    @GetMapping("/kho/nhap-nvl/{id}/edit")
    public String editReceiptForm(
            @PathVariable long id,
            Model model,
            RedirectAttributes flash) {

        try {
            documents.requireDraftReceipt(id);

            var materials = documents.materials();

            model.addAttribute("receipt", documents.receipt(id));
            model.addAttribute("lines", documents.receiptLines(id));
            model.addAttribute("suppliers", documents.suppliers());
            model.addAttribute("materials", materials);
            model.addAttribute(
                    "materialGroups",
                    documents.groupMaterialsByType(materials)
            );

            return "mrp/receipt-edit";

        } catch (IllegalArgumentException | IllegalStateException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/kho/nhap-nvl/" + id;
        }
    }

    @PostMapping("/kho/nhap-nvl/{id}/edit")
    public String updateReceipt(
            @PathVariable long id,
            @RequestParam int supplierId,
            @RequestParam(required = false) String note,
            @RequestParam List<Long> materialId,
            @RequestParam List<Integer> quantity,
            @RequestParam List<BigDecimal> price,
            RedirectAttributes flash) {

        try {
            documents.updateDraftReceipt(
                    id,
                    supplierId,
                    note,
                    materialId,
                    quantity,
                    price
            );

            flash.addFlashAttribute(
                    "success",
                    "Cập nhật phiếu nhập bản nháp thành công."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/kho/nhap-nvl/" + id;
    }
}
