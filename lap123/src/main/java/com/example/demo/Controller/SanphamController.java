package com.example.demo.Controller;

import com.example.demo.Entity.Sanpham;
import com.example.demo.Entity.Thuonghieu;
import com.example.demo.Service.SanphamService;
import com.example.demo.Service.ThuonghieuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.Service.BomService;

import com.example.demo.excel.core.ExcelImportResult;
import com.example.demo.excel.dto.ProductExcelDto;
import com.example.demo.excel.service.ProductExcelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@Controller
public class SanphamController {

    @Autowired
    private SanphamService sanphamService;

    @Autowired
    private BomService bomService;

    @Autowired
    private ThuonghieuService thuonghieuservice;

    @Autowired
    private ProductExcelService productExcelService;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @GetMapping("/sanpham")
    public String sanpham(Model model) {
        model.addAttribute("sanpham", sanphamService.getAllSanpham());
        return "sanpham/index";
    }

    @GetMapping("/sanpham/create")
    public String newSanpham(Model model) {
        model.addAttribute("sanpham", new Sanpham());
        model.addAttribute("thuonghieuList", thuonghieuservice.getAllThuonghieu());
        return "sanpham/add";
    }

    @PostMapping("/sanpham")
    public String saveSanpham(@ModelAttribute("sanpham") Sanpham sanpham,
                              @RequestParam(value = "file", required = false) MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            sanpham.setHINHANHSP(storeImage(file));
        }
        sanphamService.saveSanpham(sanpham);
        return "redirect:/sanpham";
    }

    @GetMapping("/edit-sanpham/{id}")
    public String editSanpham(@PathVariable("id") int id, Model model) {
        Sanpham sanpham = sanphamService.getSanphamById(id);
        if (sanpham.getThuonghieu() == null) {
            sanpham.setThuonghieu(new Thuonghieu());
        }
        model.addAttribute("sanpham", sanpham);
        model.addAttribute("thuonghieuList", thuonghieuservice.getAllThuonghieu());
        return "sanpham/edit";
    }

    @PostMapping("/update-sanpham")
    public String updateSanpham(@ModelAttribute Sanpham sanpham,
                                @RequestParam(value = "file", required = false) MultipartFile file) {
        Sanpham existing = sanphamService.getSanphamById(sanpham.getMASP());
        if (file != null && !file.isEmpty()) {
            existing.setHINHANHSP(storeImage(file));
        }

        existing.setTENSP(sanpham.getTENSP());
        existing.setXUATXU(sanpham.getXUATXU());
        existing.setHEDIEUHANH(sanpham.getHEDIEUHANH());
        existing.setThuonghieu(sanpham.getThuonghieu());
        existing.setTRANGTHAI(sanpham.getTRANGTHAI());
        existing.setGHICHU(sanpham.getGHICHU());

        sanphamService.updateSanpham(existing);
        return "redirect:/sanpham";
    }

    @GetMapping("/view-sanpham/{id}")
    public String viewSanpham(
            @PathVariable("id") int id,
            @RequestParam(value = "from", required = false, defaultValue = "") String from,
            Model model) {

        model.addAttribute(
                "sanpham",
                sanphamService.getSanphamById(id)
        );

        model.addAttribute("from", from);

        return "sanpham/detail";
    }

    @GetMapping("/delete-sanpham/{id}")
    public String showDeleteForm(@PathVariable int id, Model model) {
        model.addAttribute("sanpham", sanphamService.getSanphamById(id));
        return "sanpham/delete";
    }

    @PostMapping("/delete-sanpham")
    public String deleteSanpham(@RequestParam("id") int id,
                                @RequestParam("GHICHU") String GHICHU) {
        sanphamService.deleteSanpham(id, GHICHU);
        return "redirect:/sanpham";
    }

    private String storeImage(MultipartFile file) {
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = System.currentTimeMillis() + "_" + safeName;
        Path root = Path.of(uploadDir).toAbsolutePath().normalize();
        Path target = root.resolve(fileName).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Tên tệp hình ảnh không hợp lệ.");
        }
        try {
            Files.createDirectories(root);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException exception) {
            throw new IllegalStateException("Không thể lưu hình ảnh sản phẩm.", exception);
        }
    }

    // =========================
    // EXCEL: TẢI FILE MẪU SẢN PHẨM LAPTOP
    // =========================
    @GetMapping("/sanpham/excel/template")
    public ResponseEntity<byte[]> downloadProductTemplate() {
        try {
            byte[] content = productExcelService.generateTemplate();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Mau_Nhap_San_Pham_Laptop.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =========================
    // EXCEL: XUẤT DANH MỤC SẢN PHẨM LAPTOP
    // =========================
    @GetMapping("/sanpham/excel/export")
    public ResponseEntity<byte[]> exportProductExcel(@RequestParam(value = "q", defaultValue = "") String q) {
        try {
            byte[] content = productExcelService.exportProducts(q);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Danh_Muc_San_Pham_Laptop.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =========================
    // EXCEL: KIỂM TRA & XEM TRƯỚC SẢN PHẨM
    // =========================
    @PostMapping("/sanpham/excel/preview")
    @ResponseBody
    public ResponseEntity<ExcelImportResult<ProductExcelDto>> previewProductExcel(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        try {
            var result = productExcelService.validateAndPreview(file.getInputStream(), file.getSize());
            if (!result.hasErrors() && !result.getValidRows().isEmpty()) {
                session.setAttribute("PENDING_PRODUCT_EXCEL_IMPORT", result.getValidRows());
            } else {
                session.removeAttribute("PENDING_PRODUCT_EXCEL_IMPORT");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ExcelImportResult<ProductExcelDto> errorResult = new ExcelImportResult<>();
            errorResult.addError(0, "Tệp tải lên", "", e.getMessage());
            return ResponseEntity.ok(errorResult);
        }
    }

    // =========================
    // EXCEL: XÁC NHẬN NHẬP DỮ LIỆU SẢN PHẨM
    // =========================
    @PostMapping("/sanpham/excel/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmProductExcel(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<ProductExcelDto> pending = (List<ProductExcelDto>) session.getAttribute("PENDING_PRODUCT_EXCEL_IMPORT");
        if (pending == null || pending.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không có dữ liệu hợp lệ đang chờ lưu. Vui lòng tải lại tệp."
            ));
        }
        try {
            int count = productExcelService.confirmImport(pending);
            session.removeAttribute("PENDING_PRODUCT_EXCEL_IMPORT");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "count", count,
                    "message", "Đã nạp thành công " + count + " dòng sản phẩm Laptop vào hệ thống."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi lưu dữ liệu: " + e.getMessage()
            ));
        }
    }
}
