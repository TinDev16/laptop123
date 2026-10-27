package com.example.demo.Controller;

import com.example.demo.Service.MaterialCatalogService;
import com.example.demo.excel.core.ExcelImportResult;
import com.example.demo.excel.dto.MaterialExcelDto;
import com.example.demo.excel.service.MaterialExcelService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.example.demo.excel.dto.MaterialTypeExcelDto;
import com.example.demo.excel.service.MaterialTypeExcelService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/nguyen-vat-lieu")
public class MaterialCatalogController {

    private final MaterialCatalogService service;
    private final MaterialExcelService excelService;
    private final MaterialTypeExcelService typeExcelService;

    public MaterialCatalogController(
            MaterialCatalogService service,
            MaterialExcelService excelService,
            MaterialTypeExcelService typeExcelService) {
        this.service = service;
        this.excelService = excelService;
        this.typeExcelService = typeExcelService;
    }

    // =========================
    // DANH SÁCH NGUYÊN VẬT LIỆU
    // =========================

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "") String q,
            Model model) {

        var materials = service.list(q);

        model.addAttribute("materials", materials);

        model.addAttribute(
                "materialGroups",
                service.groupByType(materials)
        );

        model.addAttribute("q", q);

        return "mrp/materials";
    }

    // =========================
    // FORM THÊM NGUYÊN VẬT LIỆU
    // =========================

    @GetMapping("/create")
    public String createForm(Model model) {

        /*
         * Khi tạo mới:
         * Chỉ lấy các loại NVL đang hoạt động.
         */
        model.addAttribute(
                "types",
                service.activeTypes()
        );

        return "mrp/material-form";
    }

    // =========================
    // FORM SỬA NGUYÊN VẬT LIỆU
    // =========================

    @GetMapping("/{id}")
    public String editForm(
            @PathVariable long id,
            Model model,
            RedirectAttributes flash) {

        try {

            /*
             * Lấy thông tin NVL đang sửa.
             */
            model.addAttribute(
                    "material",
                    service.get(id)
            );

            /*
             * Khi sửa:
             *
             * - Hiển thị tất cả loại đang hoạt động.
             * - Nếu loại hiện tại của NVL đã ngừng sử dụng
             *   thì vẫn hiển thị loại đó để giữ dữ liệu cũ.
             */
            model.addAttribute(
                    "types",
                    service.typesForEdit(id)
            );

            return "mrp/material-form";

        } catch (IllegalArgumentException exception) {

            flash.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );

            return "redirect:/nguyen-vat-lieu";
        }
    }

    // =========================
    // LƯU NGUYÊN VẬT LIỆU
    // =========================

    @PostMapping
    public String save(
            @RequestParam(required = false) Long id,
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam Integer typeId,
            @RequestParam String unit,
            @RequestParam BigDecimal price,
            @RequestParam int minimum,
            @RequestParam String status,
            @RequestParam(required = false) String note,
            RedirectAttributes flash) {

        try {

            service.save(
                    id,
                    code,
                    name,
                    typeId,
                    unit,
                    price,
                    minimum,
                    status,
                    note
            );

            flash.addFlashAttribute(
                    "success",
                    "Đã lưu nguyên vật liệu."
            );

            return "redirect:/nguyen-vat-lieu";

        } catch (IllegalArgumentException | DataAccessException exception) {

            String message =
                    exception instanceof DataAccessException
                            ? "Không lưu được NVL. Kiểm tra mã NVL và loại NVL."
                            : exception.getMessage();

            flash.addFlashAttribute(
                    "error",
                    message
            );

            return id == null
                    ? "redirect:/nguyen-vat-lieu/create"
                    : "redirect:/nguyen-vat-lieu/" + id;
        }
    }

    // =========================
    // TRANG QUẢN LÝ LOẠI NVL
    // =========================

    @GetMapping("/loai")
    public String types(Model model) {

        model.addAttribute(
                "types",
                service.types()
        );

        return "mrp/material-types";
    }

    // =========================
    // FORM SỬA LOẠI NVL
    // =========================

    @GetMapping("/loai/{id}")
    public String editType(
            @PathVariable int id,
            Model model,
            RedirectAttributes flash) {

        try {

            model.addAttribute(
                    "type",
                    service.getType(id)
            );

            return "mrp/material-type-form";

        } catch (IllegalArgumentException exception) {

            flash.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );

            return "redirect:/nguyen-vat-lieu/loai";
        }
    }

    // =========================
    // THÊM LOẠI NVL
    // =========================

    @PostMapping("/loai")
    public String addType(
            @RequestParam String name,
            RedirectAttributes flash) {

        try {

            service.addType(name);

            flash.addFlashAttribute(
                    "success",
                    "Đã thêm loại nguyên vật liệu."
            );

        } catch (IllegalArgumentException | DataAccessException exception) {

            String message =
                    exception instanceof DataAccessException
                            ? "Không thể thêm loại. Tên loại có thể đã tồn tại."
                            : exception.getMessage();

            flash.addFlashAttribute(
                    "error",
                    message
            );
        }

        return "redirect:/nguyen-vat-lieu/loai";
    }

    // =========================
    // CẬP NHẬT LOẠI NVL
    // =========================

    @PostMapping("/loai/update")
    public String updateType(
            @RequestParam int id,
            @RequestParam String name,
            @RequestParam int status,
            RedirectAttributes flash) {

        try {

            service.updateType(
                    id,
                    name,
                    status
            );

            flash.addFlashAttribute(
                    "success",
                    "Đã cập nhật loại nguyên vật liệu."
            );

        } catch (IllegalArgumentException | DataAccessException exception) {

            String message =
                    exception instanceof DataAccessException
                            ? "Không thể cập nhật loại nguyên vật liệu."
                            : exception.getMessage();

            flash.addFlashAttribute(
                    "error",
                    message
            );
        }

        return "redirect:/nguyen-vat-lieu/loai";
    }

    // =========================
// TẠO MÃ NVL XEM TRƯỚC
// =========================

    @GetMapping("/code-preview")
    @ResponseBody
    public String previewMaterialCode(@RequestParam Integer typeId) {
        return service.generateMaterialCode(typeId);
    }

    // =========================
    // EXCEL: TẢI FILE MẪU CHUẨN
    // =========================
    @GetMapping("/excel/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] content = excelService.generateTemplate();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Mau_Nhap_Nguyen_Vat_Lieu.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =========================
    // EXCEL: XUẤT DANH MỤC NVL
    // =========================
    @GetMapping("/excel/export")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(defaultValue = "") String q) {
        try {
            var materials = service.list(q);
            byte[] content = excelService.exportMaterials(materials);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Danh_Muc_Nguyen_Vat_Lieu.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =========================
    // EXCEL: KIỂM TRA & XEM TRƯỚC
    // =========================
    @PostMapping("/excel/preview")
    @ResponseBody
    public ResponseEntity<ExcelImportResult<MaterialExcelDto>> previewExcel(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        try {
            var result = excelService.validateAndPreview(file.getInputStream(), file.getSize());
            if (!result.hasErrors() && !result.getValidRows().isEmpty()) {
                session.setAttribute("PENDING_MATERIAL_EXCEL_IMPORT", result.getValidRows());
            } else {
                session.removeAttribute("PENDING_MATERIAL_EXCEL_IMPORT");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ExcelImportResult<MaterialExcelDto> errorResult = new ExcelImportResult<>();
            errorResult.addError(0, "Tệp tải lên", "", e.getMessage());
            return ResponseEntity.ok(errorResult);
        }
    }

    // =========================
    // EXCEL: XÁC NHẬN NHẬP DỮ LIỆU
    // =========================
    @PostMapping("/excel/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmExcel(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<MaterialExcelDto> pending = (List<MaterialExcelDto>) session.getAttribute("PENDING_MATERIAL_EXCEL_IMPORT");
        if (pending == null || pending.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không có dữ liệu hợp lệ đang chờ lưu. Vui lòng tải lại tệp."
            ));
        }
        try {
            int count = excelService.confirmImport(pending);
            session.removeAttribute("PENDING_MATERIAL_EXCEL_IMPORT");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "count", count,
                    "message", "Đã nạp thành công " + count + " linh kiện vào hệ thống."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi lưu dữ liệu: " + e.getMessage()
            ));
        }
    }

    // =========================================================================
    // EXCEL LOẠI NVL: TẢI FILE MẪU CHUẨN
    // =========================================================================
    @GetMapping("/loai/excel/template")
    public ResponseEntity<byte[]> downloadTypeTemplate() {
        try {
            byte[] content = typeExcelService.generateTemplate();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Mau_Nhap_Loai_Nguyen_Vat_Lieu.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =========================================================================
    // EXCEL LOẠI NVL: XUẤT DANH SÁCH LOẠI NVL
    // =========================================================================
    @GetMapping("/loai/excel/export")
    public ResponseEntity<byte[]> exportTypeExcel() {
        try {
            byte[] content = typeExcelService.exportCurrentTypes();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Danh_Sach_Loai_Nguyen_Vat_Lieu.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =========================================================================
    // EXCEL LOẠI NVL: KIỂM TRA & XEM TRƯỚC
    // =========================================================================
    @PostMapping("/loai/excel/preview")
    @ResponseBody
    public ResponseEntity<ExcelImportResult<MaterialTypeExcelDto>> previewTypeExcel(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        try {
            var result = typeExcelService.validateAndPreview(file.getInputStream(), file.getSize());
            if (!result.hasErrors() && !result.getValidRows().isEmpty()) {
                session.setAttribute("PENDING_MATERIAL_TYPE_EXCEL_IMPORT", result.getValidRows());
            } else {
                session.removeAttribute("PENDING_MATERIAL_TYPE_EXCEL_IMPORT");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ExcelImportResult<MaterialTypeExcelDto> errorResult = new ExcelImportResult<>();
            errorResult.addError(0, "Tệp tải lên", "", e.getMessage());
            return ResponseEntity.ok(errorResult);
        }
    }

    // =========================================================================
    // EXCEL LOẠI NVL: XÁC NHẬN NHẬP DỮ LIỆU
    // =========================================================================
    @PostMapping("/loai/excel/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmTypeExcel(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<MaterialTypeExcelDto> pending = (List<MaterialTypeExcelDto>) session.getAttribute("PENDING_MATERIAL_TYPE_EXCEL_IMPORT");
        if (pending == null || pending.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không có dữ liệu hợp lệ đang chờ lưu. Vui lòng tải lại tệp."
            ));
        }
        try {
            int count = typeExcelService.confirmImport(pending);
            session.removeAttribute("PENDING_MATERIAL_TYPE_EXCEL_IMPORT");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "count", count,
                    "message", "Đã nạp thành công " + count + " loại nguyên vật liệu vào hệ thống."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi lưu dữ liệu: " + e.getMessage()
            ));
        }
    }
}