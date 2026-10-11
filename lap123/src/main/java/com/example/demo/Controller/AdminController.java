
package com.example.demo.Controller;

import com.example.demo.Entity.Nhanvien;
import com.example.demo.Entity.Taikhoan;
import com.example.demo.Entity.Vaitro;
import com.example.demo.Repository.NhanvienRepository;
import com.example.demo.Repository.TaikhoanRepository;
import com.example.demo.Repository.VaitroRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final NhanvienRepository nhanvienRepository;
    private final TaikhoanRepository taikhoanRepository;
    private final VaitroRepository vaitroRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(
            NhanvienRepository nhanvienRepository,
            TaikhoanRepository taikhoanRepository,
            VaitroRepository vaitroRepository,
            PasswordEncoder passwordEncoder) {

        this.nhanvienRepository = nhanvienRepository;
        this.taikhoanRepository = taikhoanRepository;
        this.vaitroRepository = vaitroRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping({"", "/"})
    public String dashboard() {
        return "admin/dashboard";
    }

    // =========================================================
    // QUẢN LÝ NHÂN VIÊN
    // =========================================================

    @GetMapping("/nhanvien")
    public String danhSachNhanVien(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String role,
            @RequestParam(defaultValue = "0") int pageWorking,
            @RequestParam(defaultValue = "0") int pageRetired,
            Model model) {

        String keyword = q == null
                ? ""
                : q.trim().toLowerCase(Locale.ROOT);

        String roleFilter = role == null ? "" : role.trim();

        // Lấy danh sách nhân viên, loại trừ nhân viên ADMIN.
        List<Nhanvien> all = nhanvienRepository.findAllExceptAdmin();

        // Danh sách nhân viên đang làm:
        // lọc theo trạng thái, từ khóa và vai trò.
        List<Nhanvien> working = all.stream()
                .filter(n -> n.getTRANGTHAI() == 0)
                .filter(n -> matchEmployee(n, keyword))
                .filter(n -> matchEmployeeRole(n, roleFilter))
                .sorted(Comparator.comparing(
                        n -> safe(n.getHOTEN()),
                        String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        // Danh sách nhân viên đã nghỉ:
        // áp dụng cùng điều kiện tìm kiếm và lọc vai trò.
        List<Nhanvien> retired = all.stream()
                .filter(n -> n.getTRANGTHAI() == 1)
                .filter(n -> matchEmployee(n, keyword))
                .filter(n -> matchEmployeeRole(n, roleFilter))
                .sorted(Comparator.comparing(
                        n -> safe(n.getHOTEN()),
                        String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        // Phân trang độc lập cho hai danh sách.
        int size = 8;

        int totalWorkingPages = totalPages(working.size(), size);
        int totalRetiredPages = totalPages(retired.size(), size);

        pageWorking = safePage(pageWorking, totalWorkingPages);
        pageRetired = safePage(pageRetired, totalRetiredPages);

        model.addAttribute(
                "nhanVienDangLam",
                getPage(working, pageWorking, size));

        model.addAttribute(
                "nhanVienDaNghi",
                getPage(retired, pageRetired, size));

        model.addAttribute("tongDangLam", working.size());
        model.addAttribute("tongDaNghi", retired.size());

        model.addAttribute("pageWorking", pageWorking);
        model.addAttribute("pageRetired", pageRetired);

        model.addAttribute("totalWorkingPages", totalWorkingPages);
        model.addAttribute("totalRetiredPages", totalRetiredPages);

        // Giữ giá trị bộ lọc và từ khóa trên giao diện.
        model.addAttribute("q", q);
        model.addAttribute("role", roleFilter);

        // Danh sách vai trò để hiển thị trong ô lọc.
        model.addAttribute(
                "danhSachVaiTro",
                vaitroRepository.findAll().stream()
                        .filter(v -> !"ADMIN".equalsIgnoreCase(v.getMaVaitro()))
                        .collect(Collectors.toList()));

        return "admin/nhanvien";
    }

    /**
     * Kiểm tra nhân viên có thuộc vai trò được chọn không.
     *
     * role rỗng: hiển thị mọi vai trò.
     * role có giá trị: chỉ hiển thị nhân viên có tài khoản
     * thuộc vai trò đó.
     */
    private boolean matchEmployeeRole(Nhanvien n, String role) {
        if (role == null || role.isBlank()) {
            return true;
        }

        if (n.getTaikhoan() == null
                || n.getTaikhoan().getVaitro() == null) {
            return false;
        }

        Vaitro vaitro = n.getTaikhoan().getVaitro();

        // MaVaitro là mã vai trò, ví dụ ADMIN hoặc ROLE_QUAN_LY_KHO.
        return role.equals(vaitro.getMaVaitro());
    }

    @GetMapping("/nhanvien/form")
    public String formNhanVien(
            @RequestParam(required = false) Integer id,
            Model model,
            RedirectAttributes redirect) {

        Nhanvien nhanvien;

        if (id == null) {
            nhanvien = new Nhanvien();
            nhanvien.setTRANGTHAI(0);
        } else {
            nhanvien = nhanvienRepository.findById(id).orElse(null);

            if (nhanvien == null) {
                redirect.addFlashAttribute(
                        "error", "Không tìm thấy nhân viên.");
                return "redirect:/admin/nhanvien";
            }

            if (isAdminEmployee(nhanvien)) {
                redirect.addFlashAttribute(
                        "error",
                        "Không thể sửa hồ sơ nhân viên quản trị ADMIN tại đây.");
                return "redirect:/admin/nhanvien";
            }
        }

        model.addAttribute("nhanvien", nhanvien);
        model.addAttribute("isEdit", id != null);

        return "admin/form-nhanvien";
    }

    @PostMapping("/nhanvien/save")
    public String saveNhanVien(
            @ModelAttribute("nhanvien") Nhanvien form,
            RedirectAttributes redirect) {

        try {
            Nhanvien target;

            if (form.getMANV() > 0) {
                target = nhanvienRepository
                        .findById(form.getMANV())
                        .orElse(null);

                if (target == null || isAdminEmployee(target)) {
                    redirect.addFlashAttribute(
                            "error",
                            "Không tìm thấy nhân viên hoặc không được phép sửa.");
                    return "redirect:/admin/nhanvien";
                }

                // Giữ trạng thái hiện tại khi chỉnh sửa hồ sơ.
                target.setHOTEN(form.getHOTEN());
                target.setGIOITINH(form.getGIOITINH());
                target.setNGAYSINH(form.getNGAYSINH());
                target.setSDT(form.getSDT());
                target.setEMAIL(form.getEMAIL());
                target.setNGAYTHAMGIA(form.getNGAYTHAMGIA());

                // Không ghi đè GHICHU ở đây để giữ lý do nghỉ việc
                // đã được lưu khi chuyển nhân viên sang trạng thái nghỉ.
            } else {
                target = new Nhanvien();

                target.setHOTEN(form.getHOTEN());
                target.setGIOITINH(form.getGIOITINH());
                target.setNGAYSINH(form.getNGAYSINH());
                target.setSDT(form.getSDT());
                target.setEMAIL(form.getEMAIL());
                target.setNGAYTHAMGIA(form.getNGAYTHAMGIA());
                target.setTRANGTHAI(0);
                target.setGHICHU(null);
            }

            if (target.getHOTEN() == null
                    || target.getHOTEN().isBlank()) {

                redirect.addFlashAttribute(
                        "error", "Họ tên không được để trống.");

                return "redirect:/admin/nhanvien/form"
                        + (form.getMANV() > 0
                        ? "?id=" + form.getMANV()
                        : "");
            }

            nhanvienRepository.save(target);



            redirect.addFlashAttribute(
                    "success", "Lưu hồ sơ nhân viên thành công.");

        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute(
                    "error",
                    "Số điện thoại hoặc email nhân viên đã được sử dụng.");
        } catch (Exception ex) {
            redirect.addFlashAttribute(
                    "error",
                    "Không thể lưu hồ sơ. Hãy kiểm tra dữ liệu nhập.");
        }

        return "redirect:/admin/nhanvien";
    }

    @PostMapping("/nhanvien/{id}/status")
    public String doiTrangThaiNhanVien(
            @PathVariable int id,
            @RequestParam int trangthai,
            @RequestParam(required = false) String lyDo,
            RedirectAttributes redirectAttributes) {

        Nhanvien nhanvien = nhanvienRepository
                .findById(id)
                .orElse(null);

        if (nhanvien == null) {
            redirectAttributes.addFlashAttribute(
                    "error", "Không tìm thấy nhân viên.");
            return "redirect:/admin/nhanvien";
        }

        // Không cho thay đổi trạng thái nhân viên ADMIN tại đây.
        if (isAdminEmployee(nhanvien)) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Không thể thay đổi trạng thái nhân viên ADMIN tại đây.");
            return "redirect:/admin/nhanvien";
        }

        if (trangthai == 1) {
            // Chuyển sang nghỉ việc: bắt buộc nhập lý do.
            if (lyDo == null || lyDo.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute(
                        "error", "Vui lòng nhập lý do nghỉ việc.");
                return "redirect:/admin/nhanvien";
            }

            nhanvien.setTRANGTHAI(1);
            nhanvien.setGHICHU(lyDo.trim());

            // Nghỉ việc thì tự động khóa tài khoản liên kết.
            Taikhoan taiKhoan = nhanvien.getTaikhoan();

            if (taiKhoan != null) {
                taiKhoan.setLocked(true);
                taikhoanRepository.save(taiKhoan);
            }

        } else if (trangthai == 0) {
            // Khôi phục nhân viên, nhưng không tự động mở khóa tài khoản.
            nhanvien.setTRANGTHAI(0);
            nhanvien.setGHICHU(null);

        } else {
            redirectAttributes.addFlashAttribute(
                    "error", "Trạng thái không hợp lệ.");
            return "redirect:/admin/nhanvien";
        }

        nhanvienRepository.save(nhanvien);

        redirectAttributes.addFlashAttribute(
                "success",
                trangthai == 1
                        ? "Đã cho nhân viên nghỉ việc và tự động khóa tài khoản (nếu có)."
                        : "Đã khôi phục nhân viên. Tài khoản vẫn giữ nguyên trạng thái khóa.");

        return "redirect:/admin/nhanvien";
    }

    // =========================================================
    // QUẢN LÝ TÀI KHOẢN
    // =========================================================

    @GetMapping("/taikhoan")
    public String danhSachTaiKhoan(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String role,
            @RequestParam(defaultValue = "0") int pageActive,
            @RequestParam(defaultValue = "0") int pageLocked,
            Model model) {

        String keyword = q.trim().toLowerCase(Locale.ROOT);

        List<Taikhoan> all = taikhoanRepository.findAll();

        List<Taikhoan> filtered = all.stream()
                .filter(tk -> matchAccount(tk, keyword))
                .filter(tk -> role.isBlank()
                        || (tk.getVaitro() != null
                        && role.equals(tk.getVaitro().getMaVaitro())))
                .sorted(Comparator.comparing(
                        tk -> tk.getEmail() == null ? "" : tk.getEmail(),
                        String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        List<Taikhoan> active = filtered.stream()
                .filter(tk -> !tk.isLocked())
                .collect(Collectors.toList());

        List<Taikhoan> locked = filtered.stream()
                .filter(Taikhoan::isLocked)
                .collect(Collectors.toList());

        int size = 8;

        int totalActivePages = totalPages(active.size(), size);
        int totalLockedPages = totalPages(locked.size(), size);

        pageActive = safePage(pageActive, totalActivePages);
        pageLocked = safePage(pageLocked, totalLockedPages);

        model.addAttribute(
                "taiKhoanDangHoatDong",
                getPage(active, pageActive, size));

        model.addAttribute(
                "taiKhoanDaKhoa",
                getPage(locked, pageLocked, size));

        model.addAttribute("tongDangHoatDong", active.size());
        model.addAttribute("tongDaKhoa", locked.size());

        model.addAttribute("pageActive", pageActive);
        model.addAttribute("pageLocked", pageLocked);

        model.addAttribute("totalActivePages", totalActivePages);
        model.addAttribute("totalLockedPages", totalLockedPages);

        model.addAttribute("q", q);
        model.addAttribute("role", role);

        model.addAttribute(
                "danhSachVaiTro",
                vaitroRepository.findAll());

        return "admin/taikhoan";
    }

    @GetMapping("/taikhoan/form")
    public String formTaiKhoan(
            @RequestParam(required = false) Integer id,
            Model model,
            RedirectAttributes redirect) {

        Taikhoan tk = id == null
                ? new Taikhoan()
                : taikhoanRepository.findById(id).orElse(null);

        if (id != null && tk == null) {
            redirect.addFlashAttribute(
                    "error", "Không tìm thấy tài khoản.");
            return "redirect:/admin/taikhoan";
        }

        model.addAttribute("taikhoan", tk);
        model.addAttribute("isEdit", id != null);

        model.addAttribute(
                "danhSachVaiTro",
                vaitroRepository.findAll());

        List<Nhanvien> nhanVienCoTheGan =
                nhanvienRepository.findAllExceptAdmin()
                        .stream()
                        .filter(n -> n.getTaikhoan() == null
                                || (tk != null
                                && tk.getNhanvien() != null
                                && n.getMANV()
                                == tk.getNhanvien().getMANV()))
                        .sorted(Comparator.comparing(
                                n -> safe(n.getHOTEN()),
                                String.CASE_INSENSITIVE_ORDER))
                        .collect(Collectors.toList());

        model.addAttribute(
                "danhSachNhanVien",
                nhanVienCoTheGan);

        return "admin/form-taikhoan";
    }

    @PostMapping("/taikhoan/save")
    public String saveTaiKhoan(
            @RequestParam(required = false) Integer id,
            @RequestParam String email,
            @RequestParam(required = false, defaultValue = "") String password,
            @RequestParam int manv,
            @RequestParam("vaitroId") int vaitroId,
            RedirectAttributes redirect) {

        try {
            String emailValue = email == null ? "" : email.trim();

            if (emailValue.isBlank()) {
                redirect.addFlashAttribute(
                        "error",
                        "Email tài khoản không được để trống.");
                return "redirect:/admin/taikhoan";
            }

            Taikhoan duplicate =
                    taikhoanRepository.findByEmail(emailValue);

            if (duplicate != null
                    && (id == null
                    || duplicate.getTaikhoan_id() != id)) {

                redirect.addFlashAttribute(
                        "error", "Email tài khoản đã tồn tại.");
                return "redirect:/admin/taikhoan";
            }

            Taikhoan tk;

            if (id == null) {
                if (password == null || password.isBlank()) {
                    redirect.addFlashAttribute(
                            "error",
                            "Mật khẩu không được để trống.");
                    return "redirect:/admin/taikhoan";
                }

                tk = new Taikhoan();
                tk.setLocked(false);
                tk.setTrangthai(1);

            } else {
                tk = taikhoanRepository.findById(id).orElse(null);

                if (tk == null) {
                    redirect.addFlashAttribute(
                            "error", "Không tìm thấy tài khoản.");
                    return "redirect:/admin/taikhoan";
                }
            }

            Nhanvien nhanvien =
                    nhanvienRepository.findById(manv).orElse(null);

            Vaitro vaitro =
                    vaitroRepository.findById(vaitroId).orElse(null);

            if (nhanvien == null || vaitro == null) {
                redirect.addFlashAttribute(
                        "error",
                        "Nhân viên hoặc vai trò được chọn không tồn tại.");
                return "redirect:/admin/taikhoan";
            }

            if (isAdminEmployee(nhanvien)) {
                redirect.addFlashAttribute(
                        "error",
                        "Không thể gán tài khoản qua màn hình này cho nhân viên ADMIN.");
                return "redirect:/admin/taikhoan";
            }

            if (nhanvien.getTaikhoan() != null
                    && (id == null
                    || nhanvien.getTaikhoan().getTaikhoan_id() != id)) {

                redirect.addFlashAttribute(
                        "error",
                        "Nhân viên này đã được liên kết với tài khoản khác.");
                return "redirect:/admin/taikhoan";
            }

            tk.setEmail(emailValue);
            tk.setNhanvien(nhanvien);
            tk.setVaitro(vaitro);

            // Chỉ đổi mật khẩu nếu người dùng nhập mật khẩu mới.
            if (password != null && !password.isBlank()) {
                tk.setPassword(passwordEncoder.encode(password));
            }

            taikhoanRepository.save(tk);

            redirect.addFlashAttribute(
                    "success", "Lưu tài khoản thành công.");

        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute(
                    "error",
                    "Dữ liệu bị trùng hoặc vi phạm liên kết trong database.");
        } catch (Exception ex) {
            redirect.addFlashAttribute(
                    "error",
                    "Không thể lưu tài khoản. Hãy kiểm tra dữ liệu nhập.");
        }

        return "redirect:/admin/taikhoan";
    }

    @PostMapping("/taikhoan/{id}/lock")
    public String doiTrangThaiKhoa(
            @PathVariable int id,
            @RequestParam boolean locked,
            RedirectAttributes redirect) {

        Taikhoan tk = taikhoanRepository
                .findById(id)
                .orElse(null);

        if (tk == null) {
            redirect.addFlashAttribute(
                    "error", "Không tìm thấy tài khoản.");
            return "redirect:/admin/taikhoan";
        }

        // Không cho khóa tài khoản ADMIN qua màn hình này.
        if (tk.getVaitro() != null
                && "ADMIN".equalsIgnoreCase(tk.getVaitro().getMaVaitro())
                && locked) {

            redirect.addFlashAttribute(
                    "error",
                    "Không thể khóa tài khoản ADMIN qua màn hình này.");
            return "redirect:/admin/taikhoan";
        }

        // Chỉ được mở khóa nếu nhân viên liên kết vẫn đang làm.
        if (!locked && tk.getNhanvien() != null) {
            Nhanvien nhanvien = tk.getNhanvien();

            if (nhanvien.getTRANGTHAI() != 0) {
                redirect.addFlashAttribute(
                        "error",
                        "Không thể mở khóa tài khoản vì nhân viên đã nghỉ việc. "
                                + "Hãy khôi phục nhân viên trước.");
                return "redirect:/admin/taikhoan";
            }
        }

        // Chỉ thay đổi trạng thái tài khoản, không thay đổi trạng thái nhân viên.
        tk.setLocked(locked);
        taikhoanRepository.save(tk);

        redirect.addFlashAttribute(
                "success",
                locked
                        ? "Đã khóa tài khoản. Trạng thái nhân viên không thay đổi."
                        : "Đã mở khóa tài khoản thành công.");

        return "redirect:/admin/taikhoan";
    }

    // =========================================================
    // HÀM HỖ TRỢ
    // =========================================================

    private boolean isAdminEmployee(Nhanvien n) {
        return n != null
                && n.getTaikhoan() != null
                && n.getTaikhoan().getVaitro() != null
                && "ADMIN".equals(
                n.getTaikhoan().getVaitro().getMaVaitro());
    }

    private boolean matchEmployee(Nhanvien n, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }

        return safe(n.getHOTEN())
                .toLowerCase(Locale.ROOT)
                .contains(keyword)
                || safe(n.getSDT())
                .toLowerCase(Locale.ROOT)
                .contains(keyword)
                || safe(n.getEMAIL())
                .toLowerCase(Locale.ROOT)
                .contains(keyword)
                || String.valueOf(n.getMANV()).contains(keyword);
    }

    private boolean matchAccount(Taikhoan tk, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }

        String hoten = tk.getNhanvien() == null
                ? ""
                : safe(tk.getNhanvien().getHOTEN());

        String manv = tk.getNhanvien() == null
                ? ""
                : String.valueOf(tk.getNhanvien().getMANV());

        String roleName = tk.getVaitro() == null
                ? ""
                : safe(tk.getVaitro().getMaVaitro());

        return safe(tk.getEmail())
                .toLowerCase(Locale.ROOT)
                .contains(keyword)
                || hoten.toLowerCase(Locale.ROOT).contains(keyword)
                || manv.contains(keyword)
                || roleName.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private int totalPages(int total, int size) {
        return Math.max(
                1,
                (int) Math.ceil(total / (double) size));
    }

    private int safePage(int page, int totalPages) {
        return Math.max(
                0,
                Math.min(page, totalPages - 1));
    }

    private <T> List<T> getPage(
            List<T> list,
            int page,
            int size) {

        int start = page * size;
        int end = Math.min(start + size, list.size());

        if (start >= list.size()) {
            return List.of();
        }

        return list.subList(start, end);
    }

    private boolean emailNhanVienDaTonTai(String email, Integer maNvBoQua) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String emailCanTim = email.trim();

        return nhanvienRepository.findAll().stream()
                .filter(nv -> maNvBoQua == null || nv.getMANV() != maNvBoQua)
                .anyMatch(nv -> nv.getEMAIL() != null
                        && emailCanTim.equalsIgnoreCase(nv.getEMAIL().trim()));
    }
}
