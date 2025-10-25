package com.example.demo.Controller;

import com.example.demo.Entity.*;
import com.example.demo.Repository.CT_PhieuxuatRepository;
import com.example.demo.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PhieuxuatController {

    @Autowired
    CT_PhieuxuatRepository ctPhieuxuatRepository;

    @Autowired
    CT_PhieuxuatService ct_phieuxuatService;

    @Autowired
    PhieuxuatService phieuxuatService;

    @Autowired
    KhachhangService khachhangService;

    @Autowired
    NhanvienService nhanvienService;

    @Autowired
    SanphamService sanphamService;

    @GetMapping("/phieuxuat")
    public String phieuxuat(Model model) {
        model.addAttribute("phieuxuat", phieuxuatService.getAllPhieuxuat());
        return "phieuxuat/index";
    }

    @GetMapping("/phieuxuat/create")
    public String phieuxuatCreate(Model model) {
        model.addAttribute("phieuxuat", new Phieuxuat());
        model.addAttribute("khachhang", khachhangService.getAllKhachhang());
        model.addAttribute("nhanvien", nhanvienService.getAllNhanvien());
        model.addAttribute("sanpham", sanphamService.getAllSanpham());

        // Tạo map {MASP -> giá nhập gần nhất}
        Map<Integer, BigDecimal> lastPrices = new HashMap<>();
        for (Sanpham sp : sanphamService.getAllSanpham()) {
            BigDecimal lastPrice = ctPhieuxuatRepository.findLastGiaxuatBySanphamId(sp.getMASP());
            if (lastPrice != null) {
                lastPrices.put(sp.getMASP(), lastPrice);
            }
        }
        model.addAttribute("lastPrices", lastPrices);

        return "phieuxuat/add";
    }

    @PostMapping("/phieuxuat")
    @Transactional
    public String savePhieuXuat(
            @ModelAttribute Phieuxuat phieuxuat,
            @RequestParam("chiTietList") String chiTietListJson,
            @RequestParam("tongTien") BigDecimal tongTien
    ) {
        try {
            phieuxuatService.savePhieuXuat(phieuxuat, chiTietListJson, tongTien);
            return "redirect:/phieuxuat";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/edit-phieuxuat/{id}")
    public String editPhieuxuat(@PathVariable int id, Model model) {
        Phieuxuat phieuxuat = phieuxuatService.getPhieuxuatById(id);
        if (phieuxuat == null) {
            return "redirect:/phieuxuat"; // không tìm thấy thì quay lại danh sách
        }
        model.addAttribute("phieuxuat", phieuxuat);
        model.addAttribute("chiTietList", phieuxuat.getChitietPhieuxuat());
        model.addAttribute("nhanvienList", nhanvienService.getAllNhanvien());
        model.addAttribute("khachhangList", khachhangService.getAllKhachhang());
        return "phieuxuat/edit";
    }

    @PostMapping("/update-phieuxuat")
    @Transactional
    public String updatePhieuxuat(@ModelAttribute Phieuxuat phieuxuat,
                                  @RequestParam("ct_id") List<Integer> ctIds,
                                  @RequestParam("soluong") List<Integer> soLuongs,
                                  @RequestParam("giaxuat") List<String> giaXuatsStr,
                                  RedirectAttributes redirectAttrs,
                                  Model model) {

        Phieuxuat existing = phieuxuatService.getPhieuxuatById(phieuxuat.getMAPHIEUXUAT());
        if (existing == null) {
            model.addAttribute("title", "Phiếu Xuất Không Tồn Tại");
            model.addAttribute("message", "Phiếu xuất bạn đang thao tác không tồn tại trong hệ thống.");
            model.addAttribute("backUrl", "/phieuxuat");
            return "error-page";
        }

        // Nạp lại nhân viên và khách hàng bằng ID để tránh null
        if (phieuxuat.getNhanvien() != null && phieuxuat.getNhanvien().getMANV() > 0) {
            existing.setNhanvien(nhanvienService.getNhanvienById(phieuxuat.getNhanvien().getMANV()));
        }

        if (phieuxuat.getKhachhang() != null && phieuxuat.getKhachhang().getMAKH() > 0) {
            existing.setKhachhang(khachhangService.getKhachhangById(phieuxuat.getKhachhang().getMAKH()));
        }

        Nhanvien nv = existing.getNhanvien();
        if (nv == null) {
            model.addAttribute("title", "Lỗi Khi Xác Nhận Phiếu Xuất");
            model.addAttribute("message", "Không thể xác nhận phiếu xuất vì nhân viên không tồn tại hoặc bị xóa khỏi hệ thống.");
            model.addAttribute("backUrl", "/phieuxuat");
            return "error-page";
        }
        if (nv.getTRANGTHAI() == 1) {
            model.addAttribute("title", "Lỗi Khi Xác Nhận Phiếu Xuất");
            model.addAttribute("message", "Không thể xác nhận phiếu xuất vì nhân viên đã nghỉ việc: " + nv.getHOTEN());
            model.addAttribute("backUrl", "/phieuxuat");
            return "error-page";
        }

        Khachhang kh = existing.getKhachhang();
        if (kh == null) {
            model.addAttribute("title", "Lỗi Khi Xác Nhận Phiếu Xuất");
            model.addAttribute("message", "Không thể xác nhận phiếu xuất vì khách hàng không tồn tại hoặc bị xóa khỏi hệ thống.");
            model.addAttribute("backUrl", "/phieuxuat");
            return "error-page";
        }
        if (kh.getTRANGTHAI() == 1) {
            model.addAttribute("title", "Lỗi Khi Xác Nhận Phiếu Xuất");
            model.addAttribute("message", "Không thể xác nhận phiếu xuất vì khách hàng đã bị vô hiệu hóa: " + kh.getHOTEN());
            model.addAttribute("backUrl", "/phieuxuat");
            return "error-page";
        }

        // Kiểm tra sản phẩm bị xóa hoặc ngừng kinh doanh
        for (int i = 0; i < ctIds.size(); i++) {
            CT_Phieuxuat ct = ct_phieuxuatService.getById(ctIds.get(i));
            if (ct != null && ct.getSanpham().getTRANGTHAI() == 1) {
                model.addAttribute("title", "Lỗi Khi Xác Nhận Phiếu Xuất");
                model.addAttribute("message", "Không thể xác nhận vì sản phẩm đã bị xóa hoặc ngừng kinh doanh: "
                        + ct.getSanpham().getTENSP());
                model.addAttribute("backUrl", "/phieuxuat");
                return "error-page";
            }
        }

        // ✅ Cập nhật thông tin cơ bản
        existing.setNhanvien(phieuxuat.getNhanvien());
        existing.setKhachhang(phieuxuat.getKhachhang());
        existing.setTHOIGIAN(phieuxuat.getTHOIGIAN());

        // ✅ Khi nhấn lưu thì luôn chuyển trạng thái thành 1 (hoàn tất)
        existing.setTRANGTHAI(1);

        BigDecimal tongTien = BigDecimal.ZERO;

        for (int i = 0; i < ctIds.size(); i++) {
            int ctId = ctIds.get(i);
            int soLuongMoi = soLuongs.get(i);

            // Xử lý giá xuất
            String giaStr = giaXuatsStr.get(i);
            if (giaStr == null) giaStr = "0";
            giaStr = giaStr.replaceAll("[^0-9]", "");
            if (giaStr.isEmpty()) giaStr = "0";


            BigDecimal giaMoi;
            try {
                giaMoi = new BigDecimal(giaStr);
            } catch (NumberFormatException e) {
                giaMoi = BigDecimal.ZERO;
            }

            CT_Phieuxuat ct = ct_phieuxuatService.getById(ctId);
            if (ct != null) {
                Sanpham sp = ct.getSanpham();

                // Cập nhật số lượng tồn
                sp.setSOLUONGTON(sp.getSOLUONGTON() - soLuongMoi);

                ct.setSoluong(soLuongMoi);
                ct.setGiaxuat(giaMoi);

                tongTien = tongTien.add(giaMoi.multiply(BigDecimal.valueOf(soLuongMoi)));

                sanphamService.saveSanpham(sp);
                ct_phieuxuatService.save(ct);
            }
        }

        existing.setTONGTIEN(tongTien);
        phieuxuatService.update(existing);

        redirectAttrs.addFlashAttribute("success", "Xác nhận phiếu xuất thành công!");
        return "redirect:/phieuxuat";
    }

    @GetMapping("/delete-phieuxuat/{id}")
    public String deletePhieuxuat(@PathVariable("id") int id) {
        phieuxuatService.deletePhieuxuat(id);
        return "redirect:/phieuxuat";
    }

    @GetMapping("view-phieuxuat/{id}")
    public String viewDetail(@PathVariable int id, Model model) {
        Phieuxuat phieuxuat = phieuxuatService.getPhieuxuatById(id);
        if (phieuxuat == null) {
            return "redirect:/phieuxuat";
        }
        model.addAttribute("phieuxuat", phieuxuat);
        model.addAttribute("chiTietList", phieuxuat.getChitietPhieuxuat());
        return "phieuxuat/detail";
    }

}
