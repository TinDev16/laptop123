package com.example.demo.Controller;

import com.example.demo.Entity.Khachhang;
import com.example.demo.Service.KhachhangService;
import com.example.demo.excel.core.ExcelImportResult;
import com.example.demo.excel.dto.CustomerExcelDto;
import com.example.demo.excel.service.CustomerExcelService;
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
public class KhachhangController {

    @Autowired
    private KhachhangService khachhangService;

    @Autowired
    private CustomerExcelService customerExcelService;

    // Khách hàng đang hoạt động
    @GetMapping("/khachhang")
    public String khachhang(Model model) {
        model.addAttribute("khachhang", new Khachhang());
        model.addAttribute("danhSachKhachHang",
                khachhangService.getAllKhachhang());
        model.addAttribute("trangThaiDanhSach", 0);

        return "khachhang/index";
    }

    // Khách hàng không còn sử dụng
    @GetMapping("/khachhang/ngung-su-dung")
    public String khachhangNgungSuDung(Model model) {
        model.addAttribute("khachhang", new Khachhang());
        model.addAttribute("danhSachKhachHang",
                khachhangService.getAllKhachhang());
        model.addAttribute("trangThaiDanhSach", 1);

        return "khachhang/index";
    }

    @GetMapping("/khachhang/create")
    public String khachhangCreate(Model model) {
        model.addAttribute("khachhang", new Khachhang());
        return "khachhang/add";
    }

    @PostMapping("/khachhang")
    public String saveKhachhang(
            @ModelAttribute("khachhang") Khachhang khachhang) {

        khachhangService.saveKhachhang(khachhang);
        return "redirect:/khachhang";
    }

    @GetMapping("/edit-khachhang/{id}")
    public String editKhachhang(
            @PathVariable("id") int id, Model model) {

        model.addAttribute("khachhang",
                khachhangService.getKhachhangById(id));

        return "khachhang/edit";
    }

    @PostMapping("/update-khachhang")
    public String updateKhachhang(
            @ModelAttribute Khachhang khachhang) {

        khachhangService.updateKhachhang(khachhang);
        return "redirect:/khachhang";
    }

    // Ngừng sử dụng khách hàng
    @PostMapping("/delete-khachhang")
    public String deleteKhachhang(
            @RequestParam("id") int id,
            @RequestParam("GHICHU") String GHICHU) {

        System.out.println("MAKH nhận được: " + id);
        System.out.println("GHICHU nhận được: [" + GHICHU + "]");

        khachhangService.deleteKhachhang(id, GHICHU);

        return "redirect:/khachhang";
    }

    // Khôi phục khách hàng
    @PostMapping("/khachhang/khoi-phuc")
    public String khoiPhucKhachhang(
            @RequestParam("id") int id) {

        khachhangService.restoreKhachhang(id);

        return "redirect:/khachhang";
    }

    // =========================
    // EXCEL: TẢI FILE MẪU KHÁCH HÀNG
    // =========================
    @GetMapping("/khachhang/excel/template")
    public ResponseEntity<byte[]> downloadCustomerTemplate() {
        try {
            byte[] content = customerExcelService.generateTemplate();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Mau_Nhap_Khach_Hang.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =========================
    // EXCEL: XUẤT DANH MỤC KHÁCH HÀNG
    // =========================
    @GetMapping("/khachhang/excel/export")
    public ResponseEntity<byte[]> exportCustomerExcel() {
        try {
            byte[] content = customerExcelService.exportCustomers();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Danh_Muc_Khach_Hang.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =========================
    // EXCEL: KIỂM TRA & XEM TRƯỚC KHÁCH HÀNG
    // =========================
    @PostMapping("/khachhang/excel/preview")
    @ResponseBody
    public ResponseEntity<ExcelImportResult<CustomerExcelDto>> previewCustomerExcel(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        try {
            var result = customerExcelService.validateAndPreview(file.getInputStream(), file.getSize());
            if (!result.hasErrors() && !result.getValidRows().isEmpty()) {
                session.setAttribute("PENDING_CUSTOMER_EXCEL_IMPORT", result.getValidRows());
            } else {
                session.removeAttribute("PENDING_CUSTOMER_EXCEL_IMPORT");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ExcelImportResult<CustomerExcelDto> errorResult = new ExcelImportResult<>();
            errorResult.addError(0, "Tệp tải lên", "", e.getMessage());
            return ResponseEntity.ok(errorResult);
        }
    }

    // =========================
    // EXCEL: XÁC NHẬN NHẬP KHÁCH HÀNG
    // =========================
    @PostMapping("/khachhang/excel/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmCustomerExcel(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CustomerExcelDto> pending = (List<CustomerExcelDto>) session.getAttribute("PENDING_CUSTOMER_EXCEL_IMPORT");
        if (pending == null || pending.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không có dữ liệu hợp lệ đang chờ lưu. Vui lòng tải lại tệp."
            ));
        }
        try {
            int count = customerExcelService.confirmImport(pending);
            session.removeAttribute("PENDING_CUSTOMER_EXCEL_IMPORT");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "count", count,
                    "message", "Đã nạp thành công " + count + " khách hàng vào hệ thống."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi lưu dữ liệu: " + e.getMessage()
            ));
        }
    }
}