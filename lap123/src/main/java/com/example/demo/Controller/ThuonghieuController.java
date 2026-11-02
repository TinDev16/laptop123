package com.example.demo.Controller;

import com.example.demo.Entity.Thuonghieu;
import com.example.demo.Service.ThuonghieuService;
import com.example.demo.excel.core.ExcelImportResult;
import com.example.demo.excel.dto.BrandExcelDto;
import com.example.demo.excel.service.BrandExcelService;
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
public class ThuonghieuController {

    @Autowired
    ThuonghieuService thuonghieuService;

    @Autowired
    BrandExcelService brandExcelService;

    @GetMapping("/thuonghieu")
    public String thuonghieu(Model model) {
        model.addAttribute("thuonghieu", thuonghieuService.getAllThuonghieu());
        return "thuonghieu/index";
    }

    @GetMapping("/thuonghieu/create")
    public String thuonghieuCreate(Model model) {
        model.addAttribute("thuonghieu", new Thuonghieu());
        return "thuonghieu/add";
    }

    @PostMapping("/thuonghieu")
    public String saveThuonghieu(@ModelAttribute Thuonghieu thuonghieu) {
        thuonghieuService.save(thuonghieu);
        return "redirect:/thuonghieu";
    }

    @GetMapping("/edit-thuonghieu/{id}")
    public String editThuonghieu(@PathVariable int id, Model model) {
        model.addAttribute("thuonghieu",
                thuonghieuService.getThuonghieuById(id));
        return "thuonghieu/edit";
    }

    @PostMapping("/update-thuonghieu")
    public String updateThuonghieu(@ModelAttribute Thuonghieu thuonghieu) {
        thuonghieuService.save(thuonghieu);
        return "redirect:/thuonghieu";
    }

    // Ngừng sử dụng thương hiệu và lưu lý do.
    @PostMapping("/delete-thuonghieu")
    public String deleteThuonghieu(
            @RequestParam("id") int id,
            @RequestParam("GHICHU") String GHICHU) {

        thuonghieuService.deleteThuonghieu(id, GHICHU);
        return "redirect:/thuonghieu";
    }

    // Khôi phục thương hiệu.
    @PostMapping("/thuonghieu/khoi-phuc")
    public String restoreThuonghieu(@RequestParam("id") int id) {
        thuonghieuService.restoreThuonghieu(id);
        return "redirect:/thuonghieu";
    }

    // ==========================================
    // EXCEL: TẢI FILE MẪU THƯƠNG HIỆU
    // ==========================================
    @GetMapping("/thuonghieu/excel/template")
    public ResponseEntity<byte[]> downloadBrandTemplate() {
        try {
            byte[] excelBytes = brandExcelService.generateTemplate();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Mau_Thuong_Hieu_Laptop123.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==========================================
    // EXCEL: XUẤT TOÀN BỘ DANH SÁCH THƯƠNG HIỆU
    // ==========================================
    @GetMapping("/thuonghieu/excel/export")
    public ResponseEntity<byte[]> exportBrandExcel() {
        try {
            byte[] excelBytes = brandExcelService.exportBrands();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Danh_Sach_Thuong_Hieu_" + System.currentTimeMillis() + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==========================================
    // EXCEL: PREVIEW KIỂM TRA TỆP NHẬP LIỆU
    // ==========================================
    @PostMapping("/thuonghieu/excel/preview")
    @ResponseBody
    public ResponseEntity<ExcelImportResult<BrandExcelDto>> previewBrandExcel(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        try {
            if (file == null || file.isEmpty()) {
                ExcelImportResult<BrandExcelDto> err = new ExcelImportResult<>();
                err.addError(0, "Tệp tải lên", "", "Vui lòng chọn tệp Excel định dạng .xlsx để nạp.");
                return ResponseEntity.ok(err);
            }
            ExcelImportResult<BrandExcelDto> result = brandExcelService.validateAndPreview(file.getInputStream(), file.getSize());
            if (result.hasErrors() == false) {
                session.setAttribute("PENDING_BRAND_EXCEL_IMPORT", result.getValidRows());
            } else {
                session.removeAttribute("PENDING_BRAND_EXCEL_IMPORT");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ExcelImportResult<BrandExcelDto> errorResult = new ExcelImportResult<>();
            errorResult.addError(0, "Tệp tải lên", "", e.getMessage());
            return ResponseEntity.ok(errorResult);
        }
    }

    // ==========================================
    // EXCEL: XÁC NHẬN NẠP VÀO CSDL
    // ==========================================
    @PostMapping("/thuonghieu/excel/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmBrandExcel(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<BrandExcelDto> pending = (List<BrandExcelDto>) session.getAttribute("PENDING_BRAND_EXCEL_IMPORT");
        if (pending == null || pending.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không có dữ liệu hợp lệ đang chờ lưu. Vui lòng tải lại tệp."
            ));
        }
        try {
            int count = brandExcelService.confirmImport(pending);
            session.removeAttribute("PENDING_BRAND_EXCEL_IMPORT");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "count", count,
                    "message", "Đã nạp thành công " + count + " thương hiệu vào hệ thống."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi lưu dữ liệu: " + e.getMessage()
            ));
        }
    }
}
