package com.example.demo.Controller;
import com.example.demo.Entity.Phieunhap;
import com.example.demo.Entity.Sanpham;
import com.example.demo.Repository.NCCRepository;
import com.example.demo.Repository.NhanvienRepository;
import com.example.demo.Repository.PhieunhapRepository;
import com.example.demo.Repository.SanphamRepository;
import com.example.demo.Service.PhieunhapService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class PhieunhapController {

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
        model.addAttribute("phieunhap", new Phieunhap());
        return "phieunhap/index";
    }

    @GetMapping("/phieunhap/create")
    public String showCreateForm(Model model) {
        model.addAttribute("nhanvien", nhanvienRepository.findAll());
        model.addAttribute("nccList", NCCRepository.findAll());
        model.addAttribute("sanpham", sanphamRepository.findAll());
        model.addAttribute("phieunhap", new Phieunhap());
        return "phieunhap/add";
    }

//    @PostMapping("/create")
//    public String createPhieuNhap(@ModelAttribute Phieunhap phieunhap,
//                                  @RequestParam("MASP") List<Integer> maspList,
//                                  @RequestParam("soluong") List<Integer> soluongList,
//                                  @RequestParam("gianhap") List<Double> gianhapList) {
//        double tongTien = 0;
//        for (int i = 0; i < maspList.size(); i++) {
//            CT_Phieunhap ct = new CT_Phieunhap();
//            ct.setPhieunhap(phieunhap);
//            ct.setSanpham(new Sanpham(maspList.get(i))); // giả sử có constructor
//            ct.setSOLUONG(soluongList.get(i));
//            ct.setGIANHAP(gianhapList.get(i));
//            ct.setTRANGTHAI("OK");
//            tongTien += soluongList.get(i) * gianhapList.get(i);
//            phieunhap.getChitiets().add(ct);
//        }
//        phieunhap.setTHOIGIAN(LocalDateTime.now());
//        phieunhap.setTONGTIEN(tongTien);
//        phieunhapRepository.save(phieunhap);
//        return "redirect:/phieunhap";
//    }


    @GetMapping("/delete-phieunhap/{id}")
    public String deletePhieunhap(@PathVariable("id") int id) {
        phieunhapService.deletePhieunhap(id);
        return "redirect:/phieunhap";
    }


}
