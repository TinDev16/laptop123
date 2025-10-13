package com.example.demo.Controller;
import com.example.demo.Entity.CT_Phieunhap;
import com.example.demo.Entity.Phieunhap;
import com.example.demo.Entity.Sanpham;
import com.example.demo.Repository.*;
import com.example.demo.Service.PhieunhapService;
import com.example.demo.dto.PhieunhapRequest;
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


    private final PhieunhapRepository phieunhapRepository;
    private NhanvienRepository nhanvienRepository;
    private NCCRepository NCCRepository;
    private PhieunhapService phieunhapService;
    private SanphamRepository sanphamRepository;

    public PhieunhapController(PhieunhapService phieunhapService, NhanvienRepository nhanvienRepository, NCCRepository NCCRepository, SanphamRepository sanphamRepository, PhieunhapRepository phieunhapRepository) {
        super();
        this.phieunhapService = phieunhapService;
        this.nhanvienRepository = nhanvienRepository;
        this.NCCRepository = NCCRepository;
        this.sanphamRepository = sanphamRepository;
        this.phieunhapRepository = phieunhapRepository;
    }

    @GetMapping("/phieunhap")
    public String phieunhap(Model model) {
        model.addAttribute("phieunhap", phieunhapService.getAllPhieunhap());
        return "phieunhap/index";
    }

    @GetMapping("/phieunhap/create")
    public String phieunhapCreate(Model model) {
        model.addAttribute("phieunhap", new Phieunhap());
        model.addAttribute("nccList", NCCRepository.findAll());
        model.addAttribute("nhanvien", nhanvienRepository.findAll());
        model.addAttribute("sanpham", sanphamRepository.findAll());
        return "phieunhap/add";
    }

    @PostMapping("/phieunhap")
    @Transactional
    public String savePhieuNhap(
            @ModelAttribute Phieunhap phieunhap,
            @RequestParam("chiTietList") String chiTietListJson,
            @RequestParam("tongTien") float tongTien
    ) {
        try {
            phieunhapService.savePhieuNhap(phieunhap, chiTietListJson, tongTien);
            return "redirect:/phieunhap";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }





}

