
package com.example.demo.Controller;

import com.example.demo.Entity.NCC;
import com.example.demo.Service.NCCService;
import com.example.demo.excel.core.ExcelImportResult;
import com.example.demo.excel.dto.SupplierExcelDto;
import com.example.demo.excel.service.SupplierExcelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
public class NCCController {

    @Autowired
    NCCService nccService;

    @Autowired
    SupplierExcelService supplierExcelService;

    // Danh sách nhà cung cấp
    @GetMapping("/ncc")
    public String nhacungcap(Model model) {
        model.addAttribute("ncc", nccService.getAllNCC());
        return "ncc/index";
    }

    // Trang thêm nhà cung cấp
    @GetMapping("/ncc/create")
    public String createNhacungcap(Model model) {
        NCC ncc = new NCC();
        model.addAttribute("ncc", ncc);
        return "ncc/add";
    }

    // Lưu nhà cung cấp mới
    @PostMapping("/ncc")
    public String saveNhacungcap(@ModelAttribute("ncc") NCC ncc) {
        nccService.saveNCC(ncc);
        return "redirect:/ncc";
    }

    // Trang chỉnh sửa nhà cung cấp
    @GetMapping("/edit-ncc/{id}")
    public String editNhacungcap(
            Model model,
            @PathVariable int id) {

        model.addAttribute("ncc", nccService.getNCCById(id));
        return "ncc/edit";
    }

    // Cập nhật nhà cung cấp
    @PostMapping("/update-ncc")
    public String updateNhacungcap(@ModelAttribute("ncc") NCC ncc) {
        nccService.updateNCC(ncc);
        return "redirect:/ncc";
    }

    // Ngừng hợp tác và lưu lý do
    @PostMapping("/delete-ncc")
    public String deleteNhacungcap(
            @RequestParam("id") int id,
            @RequestParam("GHICHU") String GHICHU) {

        nccService.deleteNCC(id, GHICHU);
        return "redirect:/ncc";
    }

    // Khôi phục nhà cung cấp
    @PostMapping("/ncc/khoi-phuc")
    public String restoreNhacungcap(@RequestParam("id") int id) {
        nccService.restoreNCC(id);
        return "redirect:/ncc";
    }

    // =========================
    // EXCEL: TẢI FILE MẪU NHÀ CUNG CẤP
    // =========================
    @GetMapping("/ncc/excel/template")
    public ResponseEntity<byte[]> downloadNccTemplate() {
        try {
            byte[] content = supplierExcelService.generateTemplate();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Mau_Nhap_Nha_Cung_Cap.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =========================
    // EXCEL: XUẤT DANH MỤC NHÀ CUNG CẤP
    // =========================
    @GetMapping("/ncc/excel/export")
    public ResponseEntity<byte[]> exportNccExcel() {
        try {
            byte[] content = supplierExcelService.exportSuppliers();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Danh_Muc_Nha_Cung_Cap.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =========================
    // EXCEL: KIỂM TRA & XEM TRƯỚC NHÀ CUNG CẤP
    // =========================
    @PostMapping("/ncc/excel/preview")
    @ResponseBody
    public ResponseEntity<ExcelImportResult<SupplierExcelDto>> previewNccExcel(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        try {
            var result = supplierExcelService.validateAndPreview(file.getInputStream(), file.getSize());
            if (!result.hasErrors() && !result.getValidRows().isEmpty()) {
                session.setAttribute("PENDING_NCC_EXCEL_IMPORT", result.getValidRows());
            } else {
                session.removeAttribute("PENDING_NCC_EXCEL_IMPORT");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ExcelImportResult<SupplierExcelDto> errorResult = new ExcelImportResult<>();
            errorResult.addError(0, "Tệp tải lên", "", e.getMessage());
            return ResponseEntity.ok(errorResult);
        }
    }

    // =========================
    // EXCEL: XÁC NHẬN NHẬP NHÀ CUNG CẤP
    // =========================
    @PostMapping("/ncc/excel/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmNccExcel(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<SupplierExcelDto> pending = (List<SupplierExcelDto>) session.getAttribute("PENDING_NCC_EXCEL_IMPORT");
        if (pending == null || pending.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không có dữ liệu hợp lệ đang chờ lưu. Vui lòng tải lại tệp."
            ));
        }
        try {
            int count = supplierExcelService.confirmImport(pending);
            session.removeAttribute("PENDING_NCC_EXCEL_IMPORT");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "count", count,
                    "message", "Đã nạp thành công " + count + " nhà cung cấp vào hệ thống."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi lưu dữ liệu: " + e.getMessage()
            ));
        }
    }
}
