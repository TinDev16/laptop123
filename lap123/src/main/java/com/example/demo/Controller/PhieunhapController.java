package com.example.demo.Controller;
import com.example.demo.Entity.*;
import com.example.demo.Repository.*;
import com.example.demo.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
public class PhieunhapController {

    @Autowired
    private CT_PhieunhapRepository ctPhieunhapRepository;

    @Autowired
    private NCCService nccService;

    @Autowired
    private NhanvienService nhanvienService;

    @Autowired
    private CT_PhieunhapService ctPhieunhapService;

    @Autowired
    private SanphamService sanphamService;

    @Autowired
    private PhieunhapService phieunhapService;

    @GetMapping("/phieunhap")
    public String phieunhap(Model model) {
        model.addAttribute("phieunhap", phieunhapService.getAllPhieunhap());
        return "phieunhap/index";
    }

    @GetMapping("/phieunhap/create")
    public String phieunhapCreate(Model model) {
        model.addAttribute("phieunhap", new Phieunhap());
        model.addAttribute("nccList", nccService.getAllNCC());
        model.addAttribute("nhanvien", nhanvienService.getAllNhanvien());
        model.addAttribute("sanpham", sanphamService.getAllSanpham());
        return "phieunhap/add";
    }

    @PostMapping("/phieunhap")
    @Transactional
    public String savePhieuNhap(
            @ModelAttribute Phieunhap phieunhap,
            @RequestParam("chiTietList") String chiTietListJson,
            @RequestParam("tongTien") BigDecimal tongTien
    ) {
        try {
            phieunhapService.savePhieuNhap(phieunhap, chiTietListJson, tongTien);
            return "redirect:/phieunhap";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("view-phieunhap/{id}")
    public String viewDetail(@PathVariable int id, Model model) {
        Phieunhap phieunhap = phieunhapService.getPhieunhapById(id);
        if (phieunhap == null) {
            return "redirect:/phieunhap";
        }
        model.addAttribute("phieunhap", phieunhap);
        model.addAttribute("chiTietList", phieunhap.getChiTietPhieuNhap());
        return "phieunhap/detail";
    }

    @GetMapping("/edit-phieunhap/{id}")
    public String editPhieunhap(@PathVariable int id, Model model) {
        Phieunhap phieunhap = phieunhapService.getPhieunhapById(id);
        if (phieunhap == null) {
            return "redirect:/phieunhap"; // không tìm thấy thì quay lại danh sách
        }

        model.addAttribute("phieunhap", phieunhap);
        model.addAttribute("chiTietList", phieunhap.getChiTietPhieuNhap());
        model.addAttribute("nccList", nccService.getAllNCC());
        model.addAttribute("nhanvienList", nhanvienService.getAllNhanvien());
        return "phieunhap/edit";
    }

    @PostMapping("/update-phieunhap")
    @Transactional
    public String updatePhieunhap(@ModelAttribute Phieunhap phieunhap,
                                  @RequestParam("ct_id") List<Integer> ctIds,
                                  @RequestParam("soluong") List<Integer> soLuongs,
                                  @RequestParam("gianhap") List<String> giaNhapsStr,
                                  RedirectAttributes redirectAttrs, Model model) {

        Phieunhap existing = phieunhapService.getPhieunhapById(phieunhap.getMAPHIEUNHAP());
        if (existing == null) {
            model.addAttribute("title", "Phiếu Nhập Không Tồn Tại");
            model.addAttribute("message", "Phiếu nhập bạn đang thao tác không tồn tại trong hệ thống.");
            model.addAttribute("backUrl", "/phieunhap");
            return "error-page";
        }

        // 🔹 Kiểm tra nhà cung cấp
        if (phieunhap.getNcc() == null || phieunhap.getNcc().getTRANGTHAI() == 1) {
            model.addAttribute("title", "Lỗi Khi Xác Nhận Phiếu Nhập");
            model.addAttribute("message", "Không thể xác nhận phiếu nhập vì nhà cung cấp đã bị xóa hoặc ngừng hợp tác.");
            model.addAttribute("backUrl", "/phieunhap");
            return "error-page";
        }

        // 🔹 Kiểm tra nhân viên
        if (phieunhap.getNhanvien() == null || phieunhap.getNhanvien().getTRANGTHAI() == 1) {
            model.addAttribute("title", "Lỗi Khi Xác Nhận Phiếu Nhập");
            model.addAttribute("message", "Không thể xác nhận phiếu nhập vì nhân viên thực hiện đã bị khóa hoặc nghỉ việc.");
            model.addAttribute("backUrl", "/phieunhap");
            return "error-page";
        }

        // 🔹 Kiểm tra sản phẩm bị xóa hoặc ngừng kinh doanh
        for (int i = 0; i < ctIds.size(); i++) {
            CT_Phieunhap ct = ctPhieunhapService.getById(ctIds.get(i));
            if (ct != null && ct.getSanpham().getTRANGTHAI() == 1) {
                model.addAttribute("title", "Lỗi Khi Xác Nhận Phiếu Nhập");
                model.addAttribute("message", "Không thể xác nhận phiếu nhập vì sản phẩm đã bị xóa hoặc ngừng kinh doanh: "
                        + ct.getSanpham().getTENSP());
                model.addAttribute("backUrl", "/phieunhap");
                return "error-page";
            }
        }

        // ✅ Cập nhật thông tin cơ bản
        existing.setNcc(phieunhap.getNcc());
        existing.setNhanvien(phieunhap.getNhanvien());
        existing.setTHOIGIAN(phieunhap.getTHOIGIAN());
        existing.setTRANGTHAI(1); // Đánh dấu hoàn tất

        BigDecimal tongTien = BigDecimal.ZERO;

        // ✅ Cập nhật chi tiết phiếu nhập
        for (int i = 0; i < ctIds.size(); i++) {
            int ctId = ctIds.get(i);
            int soLuongMoi = soLuongs.get(i);

            String giaStr = giaNhapsStr.get(i);
            if (giaStr == null) giaStr = "0";
            giaStr = giaStr.replaceAll("[^0-9]", "");
            if (giaStr.isEmpty()) giaStr = "0";

            BigDecimal giaMoi;
            try {
                giaMoi = new BigDecimal(giaStr);
            } catch (NumberFormatException e) {
                giaMoi = BigDecimal.ZERO;
            }

            CT_Phieunhap ct = ctPhieunhapService.getById(ctId);
            if (ct != null) {
                Sanpham sp = ct.getSanpham();

                // 🧾 Cập nhật số lượng tồn
                sp.setSOLUONGTON(sp.getSOLUONGTON() + soLuongMoi);

                ct.setSoluong(soLuongMoi);
                ct.setGianhap(giaMoi);

                tongTien = tongTien.add(giaMoi.multiply(BigDecimal.valueOf(soLuongMoi)));

                sanphamService.saveSanpham(sp);
                ctPhieunhapService.save(ct);
            }
        }

        existing.setTONGTIEN(tongTien);
        phieunhapService.update(existing);

        redirectAttrs.addFlashAttribute("success", "✅ Xác nhận phiếu nhập thành công!");
        return "redirect:/phieunhap";
    }


    @GetMapping("/delete-phieunhap/{id}")
    public String deletePhieunhap(@PathVariable("id") int id) {
        phieunhapService.deletePhieunhap(id);
        return "redirect:/phieunhap";
    }

}

