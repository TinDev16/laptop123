package com.example.demo.Controller;
import com.example.demo.Service.NhanvienService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/nhanvien")
public class NhanvienController {

    private NhanvienService nhanvienService;
    public NhanvienController(NhanvienService nhanvienService) {
        super();
        this.nhanvienService = nhanvienService;
    }

    @GetMapping("/nhanvien")
    public String listNhanvien(Model model) {
        model.addAttribute("nhanvien",nhanvienService.getAllNhanvien());
        return "nhanvien";
    }


}
