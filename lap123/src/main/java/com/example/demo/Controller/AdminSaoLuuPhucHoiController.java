package com.example.demo.Controller;

import com.example.demo.Service.DatabaseBackupService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Controller
public class AdminSaoLuuPhucHoiController {

    private final DatabaseBackupService backupService;
    private final JdbcTemplate jdbcTemplate;

    public AdminSaoLuuPhucHoiController(
            DatabaseBackupService backupService,
            JdbcTemplate jdbcTemplate
    ) {
        this.backupService = backupService;
        this.jdbcTemplate = jdbcTemplate;
    }

    // Hiển thị trang sao lưu
    @GetMapping("/admin/sao-luu-phuc-hoi")
    public String hienThiTrangSaoLuu(Model model) {

        model.addAttribute("lichSuSaoLuu",
                jdbcTemplate.queryForList("""
                        SELECT *
                        FROM lich_su_sao_luu
                        ORDER BY thoi_gian_sao_luu DESC
                        """));

        model.addAttribute("danhSachCoThePhucHoi",
                jdbcTemplate.queryForList("""
                SELECT
                    id,
                    ten_ban_sao_luu,
                    thoi_gian_sao_luu,
                    dung_luong
                FROM lich_su_sao_luu
                WHERE trang_thai = 'THANH_CONG'
                ORDER BY thoi_gian_sao_luu DESC
                """));

        return "admin/sao-luu-phuc-hoi";
    }

    // Xử lý khi nhấn nút xác nhận sao lưu
    @PostMapping("/admin/sao-luu-phuc-hoi/tao")
    public String taoBanSaoLuu(
            @RequestParam("tenBanSaoLuu") String tenBanSaoLuu,
            RedirectAttributes redirectAttributes
    ) {

        try {

            String tenDaDat = tenBanSaoLuu.trim();

            if (tenDaDat.isEmpty()) {
                throw new IllegalArgumentException(
                        "Vui lòng nhập tên bản sao lưu."
                );
            }

            backupService.taoBanSaoLuu(
                    tenDaDat,
                    "THU_CONG"
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Sao lưu database thành công: " + tenDaDat
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Sao lưu thất bại: " + e.getMessage()
            );
        }

        return "redirect:/admin/sao-luu-phuc-hoi";
    }

    @GetMapping("/admin/sao-luu-phuc-hoi/xem/{id}")
    public ResponseEntity<Resource> xemFileSaoLuu(@PathVariable Long id) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT duong_dan FROM lich_su_sao_luu WHERE id = ?",
                    id
            );

            if (rows.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String duongDan = (String) rows.get(0).get("duong_dan");

            Path filePath = Path.of(duongDan).toAbsolutePath().normalize();

            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/plain;charset=UTF-8"))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + filePath.getFileName() + "\""
                    )
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ======================================================
// XỬ LÝ PHỤC HỒI DATABASE
// ======================================================

    @PostMapping("/admin/sao-luu-phuc-hoi/phuc-hoi")
    public String phucHoiDatabase(
            @RequestParam("idBanSaoLuu") Long idBanSaoLuu,
            RedirectAttributes redirectAttributes
    ) {

        try {

            backupService.phucHoiBanSaoLuu(idBanSaoLuu);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Phục hồi database thành công! "
                            + "Dữ liệu hiện tại đã được thay thế bằng dữ liệu từ bản sao lưu."
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Phục hồi database thất bại: " + e.getMessage()
            );
        }

        return "redirect:/admin/sao-luu-phuc-hoi";
    }
}