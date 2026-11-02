package com.example.demo.Controller;

import com.example.demo.Service.NhanvienService;
import com.example.demo.excel.core.ExcelImportResult;
import com.example.demo.excel.dto.EmployeeExcelDto;
import com.example.demo.excel.service.EmployeeExcelService;
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
public class NhanvienController {

    @Autowired
    private NhanvienService nhanvienService;

    @Autowired
    private EmployeeExcelService employeeExcelService;

    @GetMapping("/nhanvien")
    public String listNhanvien(Model model) {
        model.addAttribute(
                "nhanvien",
                nhanvienService.getAllNhanvien()
        );

        return "nhanvien/index";
    }

    // ==========================================
    // EXCEL: TẢI FILE MẪU NHÂN VIÊN
    // ==========================================
    @GetMapping("/nhanvien/excel/template")
    public ResponseEntity<byte[]> downloadEmployeeTemplate() {
        try {
            byte[] excelBytes = employeeExcelService.generateTemplate();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Mau_Nhan_Vien_Laptop123.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==========================================
    // EXCEL: XUẤT TOÀN BỘ DANH SÁCH NHÂN VIÊN
    // ==========================================
    @GetMapping("/nhanvien/excel/export")
    public ResponseEntity<byte[]> exportEmployeeExcel() {
        try {
            byte[] excelBytes = employeeExcelService.exportEmployees();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Danh_Sach_Nhan_Vien_" + System.currentTimeMillis() + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==========================================
    // EXCEL: PREVIEW KIỂM TRA TỆP NHẬP LIỆU
    // ==========================================
    @PostMapping("/nhanvien/excel/preview")
    @ResponseBody
    public ResponseEntity<ExcelImportResult<EmployeeExcelDto>> previewEmployeeExcel(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        try {
            if (file == null || file.isEmpty()) {
                ExcelImportResult<EmployeeExcelDto> err = new ExcelImportResult<>();
                err.addError(0, "Tệp tải lên", "", "Vui lòng chọn tệp Excel định dạng .xlsx để nạp.");
                return ResponseEntity.ok(err);
            }
            ExcelImportResult<EmployeeExcelDto> result = employeeExcelService.validateAndPreview(file.getInputStream(), file.getSize());
            if (result.hasErrors() == false) {
                session.setAttribute("PENDING_EMPLOYEE_EXCEL_IMPORT", result.getValidRows());
            } else {
                session.removeAttribute("PENDING_EMPLOYEE_EXCEL_IMPORT");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ExcelImportResult<EmployeeExcelDto> errorResult = new ExcelImportResult<>();
            errorResult.addError(0, "Tệp tải lên", "", e.getMessage());
            return ResponseEntity.ok(errorResult);
        }
    }

    // ==========================================
    // EXCEL: XÁC NHẬN NẠP VÀO CSDL
    // ==========================================
    @PostMapping("/nhanvien/excel/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmEmployeeExcel(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<EmployeeExcelDto> pending = (List<EmployeeExcelDto>) session.getAttribute("PENDING_EMPLOYEE_EXCEL_IMPORT");
        if (pending == null || pending.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không có dữ liệu hợp lệ đang chờ lưu. Vui lòng tải lại tệp."
            ));
        }
        try {
            int count = employeeExcelService.confirmImport(pending);
            session.removeAttribute("PENDING_EMPLOYEE_EXCEL_IMPORT");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "count", count,
                    "message", "Đã nạp thành công " + count + " nhân viên vào hệ thống."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi lưu dữ liệu: " + e.getMessage()
            ));
        }
    }
}
