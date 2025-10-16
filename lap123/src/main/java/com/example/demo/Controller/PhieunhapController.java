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
                                  RedirectAttributes redirectAttrs) {

        Phieunhap existing = phieunhapService.getPhieunhapById(phieunhap.getMAPHIEUNHAP());
        if (existing == null) {
            redirectAttrs.addFlashAttribute("error", "Phiếu nhập không tồn tại!");
            return "redirect:/phieunhap";
        }

        // 🧠 Kiểm tra nếu phiếu nhập chứa sản phẩm bị xóa (trạng thái = 0)
        for (int i = 0; i < ctIds.size(); i++) {
            CT_Phieunhap ct = ctPhieunhapService.getById(ctIds.get(i));
            if (ct != null && ct.getSanpham().getTRANGTHAI() == 1) {
                redirectAttrs.addFlashAttribute("error",
                        "Không thể xác nhận phiếu nhập vì có sản phẩm đã bị xóa hoặc ngừng kinh doanh: "
                                + ct.getSanpham().getTENSP());
                return "redirect:/phieunhap/detail/" + phieunhap.getMAPHIEUNHAP();
            }
        }

        // ✅ Cập nhật thông tin cơ bản
        existing.setNcc(phieunhap.getNcc());
        existing.setNhanvien(phieunhap.getNhanvien());
        existing.setTHOIGIAN(phieunhap.getTHOIGIAN());

        // ✅ Khi nhấn lưu thì luôn chuyển trạng thái thành 1 (hoàn tất)
        existing.setTRANGTHAI(1);

        BigDecimal tongTien = BigDecimal.ZERO;

        for (int i = 0; i < ctIds.size(); i++) {
            int ctId = ctIds.get(i);
            int soLuongMoi = soLuongs.get(i);

            // 🧩 Xử lý giá nhập
            String giaStr = giaNhapsStr.get(i);
            if (giaStr == null) giaStr = "0";
            giaStr = giaStr.replace(".", "").trim();

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

        redirectAttrs.addFlashAttribute("success", "Xác nhận phiếu nhập thành công!");
        return "redirect:/phieunhap";
    }

    @GetMapping("/delete-phieunhap/{id}")
    public String deletePhieunhap(@PathVariable("id") int id) {
        phieunhapService.deletePhieunhap(id);
        return "redirect:/phieunhap";
    }

}

