package com.example.demo.Controller;
import com.example.demo.Entity.CT_Phieunhap;
import com.example.demo.Entity.Phieunhap;
import com.example.demo.Entity.Sanpham;
import com.example.demo.Repository.*;
import com.example.demo.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String updatePhieunhap(@ModelAttribute Phieunhap phieunhap,
                                  @RequestParam("ct_id") List<Integer> ctIds,
                                  @RequestParam("soluong") List<Integer> soLuongs,
                                  @RequestParam("gianhap") List<BigDecimal> giaNhaps) {

        Phieunhap existing = phieunhapService.getPhieunhapById(phieunhap.getMAPHIEUNHAP());
        if (existing == null) {
            return "redirect:/phieunhap";
        }

        existing.setNcc(phieunhap.getNcc());
        existing.setNhanvien(phieunhap.getNhanvien());
        existing.setTHOIGIAN(phieunhap.getTHOIGIAN());
        existing.setTRANGTHAI(phieunhap.getTRANGTHAI());

        BigDecimal tongTien = BigDecimal.ZERO;

        // Cập nhật chi tiết sản phẩm
        for (int i = 0; i < ctIds.size(); i++) {
            int ctId = ctIds.get(i);
            int soLuongMoi = soLuongs.get(i);
            BigDecimal giaMoi = giaNhaps.get(i);

            CT_Phieunhap ct = ctPhieunhapService.getById(ctId);
            if (ct != null) {
                Sanpham sp = ct.getSanpham();

                // cập nhật tồn kho
                int chenhLech = soLuongMoi - ct.getSoluong();
                sp.setSOLUONGTON(sp.getSOLUONGTON() + chenhLech);

                // cập nhật giá & số lượng
                ct.setSoluong(soLuongMoi);
                ct.setGianhap(giaMoi);

                tongTien = tongTien.add(giaMoi.multiply(BigDecimal.valueOf(soLuongMoi)));

                sanphamService.saveSanpham(sp);
                ctPhieunhapService.save(ct);
            }
        }

        existing.setTONGTIEN(tongTien);
        phieunhapService.update(existing);

        return "redirect:/phieunhap/detail/" + phieunhap.getMAPHIEUNHAP();
    }

    @PostMapping("/confirm/{id}")
    public String confirmPhieunhap(@PathVariable int id) {
        Phieunhap phieunhap = phieunhapService.getPhieunhapById(id);
        if (phieunhap == null) {
            return "redirect:/phieunhap";
        }

        // Cập nhật tồn kho khi xác nhận
        for (CT_Phieunhap ct : phieunhap.getChiTietPhieuNhap()) {
            Sanpham sp = ct.getSanpham();
            sp.setSOLUONGTON(sp.getSOLUONGTON() + ct.getSoluong());
            sanphamService.saveSanpham(sp);
        }

        phieunhap.setTRANGTHAI(1);
        phieunhapService.update(phieunhap);

        return "redirect:/phieunhap/detail/" + id;
    }

    @GetMapping("/delete-phieunhap/{id}")
    public String deletePhieunhap(@PathVariable("id") int id) {
        phieunhapService.deletePhieunhap(id);
        return "redirect:/phieunhap";
    }



}

