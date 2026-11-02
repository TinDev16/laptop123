package com.example.demo.Controller;

import com.example.demo.Service.BomService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpSession;
import com.example.demo.excel.core.ExcelImportResult;
import com.example.demo.excel.dto.BomExcelDto;
import com.example.demo.excel.service.BomExcelService;

@Controller
@RequestMapping("/bom")
public class BomController {

    private final BomService service;
    private final BomExcelService bomExcelService;

    public BomController(BomService service, BomExcelService bomExcelService) {
        this.service = service;
        this.bomExcelService = bomExcelService;
    }

    // =========================
    // DANH SÁCH BOM
    // =========================

    @GetMapping
    public String index(Model model) {
        model.addAttribute("boms", service.list());
        return "mrp/bom-list";
    }

    // =========================
    // FORM TẠO BOM
    // =========================

    @GetMapping("/create")
    public String createForm(Model model) {

        var materials = service.materials();

        model.addAttribute(
                "products",
                service.products()
        );

        model.addAttribute(
                "materials",
                materials
        );

        model.addAttribute(
                "materialGroups",
                service.groupMaterialsByType(materials)
        );

        return "mrp/bom-form";
    }

    // =========================
    // AJAX:
    // LẤY CÁC VERSION BOM
    // THEO THÀNH PHẨM
    // =========================

    @GetMapping("/versions")
    @ResponseBody
    public List<Map<String, Object>> versions(
            @RequestParam int productId) {
        return service.versions(productId);
    }

    // =========================
    // AJAX:
    // LẤY NVL THEO BOM
    // =========================

    @GetMapping("/materials-by-bom")
    @ResponseBody
    public List<Map<String, Object>> materialsByBom(
            @RequestParam long bomId) {
        return service.materialsByBom(bomId);
    }

    // =========================
    // LƯU BOM MỚI
    // =========================

    @PostMapping
    public String create(
            Authentication authentication,

            @RequestParam int productId,

            @RequestParam String version,

            @RequestParam(required = false)
            String note,

            @RequestParam
            List<Long> materialId,

            @RequestParam
            List<BigDecimal> quantity,

            @RequestParam
            List<BigDecimal> waste,

            RedirectAttributes redirect) {

        try {

            long id = service.create(
                    authentication.getName(),
                    productId,
                    version,
                    note,
                    materialId,
                    quantity,
                    waste
            );

            return "redirect:/bom/" + id;

        } catch (
                IllegalArgumentException |
                DataAccessException exception) {

            redirect.addFlashAttribute(
                    "error",
                    exception instanceof DataAccessException
                            ? "Không lưu được BOM. Kiểm tra sản phẩm, nguyên vật liệu và phiên bản."
                            : exception.getMessage()
            );

            return "redirect:/bom/create";
        }
    }

    // =========================
    // CHI TIẾT BOM
    // =========================

    @GetMapping("/{id}")
    public String detail(
            @PathVariable long id,
            @RequestParam(required = false, defaultValue = "") String from,
            Model model) {

        model.addAttribute(
                "bom",
                service.get(id)
        );

        model.addAttribute(
                "details",
                service.details(id)
        );

        String backUrl;

        if ("kho-san-xuat".equalsIgnoreCase(from)) {
            backUrl = "/kho-san-xuat/thanh-pham";
        } else if ("sanpham".equalsIgnoreCase(from)) {
            backUrl = "/sanpham";
        } else {
            backUrl = "/bom";
        }

        model.addAttribute("backUrl", backUrl);

        System.out.println("DEBUG from = " + from);
        System.out.println("DEBUG backUrl = " + backUrl);

        return "mrp/bom-detail";
    }

    // =========================
    // NGỪNG SỬ DỤNG BOM
    // =========================

    @PostMapping("/{id}/disable")
    public String disable(
            @PathVariable long id,
            RedirectAttributes redirect) {

        try {

            service.disable(id);

        } catch (IllegalArgumentException exception) {

            redirect.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );
        }

        return "redirect:/bom/" + id;
    }

    // =========================
    // EXCEL: TẢI FILE MẪU ĐỊNH MỨC KỸ THUẬT BOM
    // =========================
    @GetMapping("/excel/template")
    public ResponseEntity<byte[]> downloadBomTemplate() {
        try {
            byte[] content = bomExcelService.generateTemplate();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Mau_Nhap_Dinh_Muc_BOM.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =========================
    // EXCEL: XUẤT CÔNG THỨC ĐỊNH MỨC BOM
    // =========================
    @GetMapping("/excel/export")
    public ResponseEntity<byte[]> exportBomExcel() {
        try {
            byte[] content = bomExcelService.exportBom();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Danh_Sach_Dinh_Muc_BOM.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =========================
    // EXCEL: KIỂM TRA & XEM TRƯỚC ĐỊNH MỨC BOM
    // =========================
    @PostMapping("/excel/preview")
    @ResponseBody
    public ResponseEntity<ExcelImportResult<BomExcelDto>> previewBomExcel(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        try {
            var result = bomExcelService.validateAndPreview(file.getInputStream(), file.getSize());
            if (!result.hasErrors() && !result.getValidRows().isEmpty()) {
                session.setAttribute("PENDING_BOM_EXCEL_IMPORT", result.getValidRows());
            } else {
                session.removeAttribute("PENDING_BOM_EXCEL_IMPORT");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ExcelImportResult<BomExcelDto> errorResult = new ExcelImportResult<>();
            errorResult.addError(0, "Tệp tải lên", "", e.getMessage());
            return ResponseEntity.ok(errorResult);
        }
    }

    // =========================
    // EXCEL: XÁC NHẬN NHẬP DỮ LIỆU ĐỊNH MỨC BOM
    // =========================
    @PostMapping("/excel/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmBomExcel(HttpSession session, java.security.Principal principal) {
        @SuppressWarnings("unchecked")
        List<BomExcelDto> pending = (List<BomExcelDto>) session.getAttribute("PENDING_BOM_EXCEL_IMPORT");
        if (pending == null || pending.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không có dữ liệu hợp lệ đang chờ lưu. Vui lòng tải lại tệp."
            ));
        }
        try {
            String username = principal != null ? principal.getName() : null;
            int count = bomExcelService.confirmImport(pending, username);
            session.removeAttribute("PENDING_BOM_EXCEL_IMPORT");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "count", count,
                    "message", "Đã nạp thành công " + count + " định mức linh kiện vào công thức BOM."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi lưu dữ liệu BOM: " + e.getMessage()
            ));
        }
    }
}