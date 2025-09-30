package com.example.demo.Controller;
import com.example.demo.Entity.Nhanvien;
import com.example.demo.Entity.Sanpham;
import com.example.demo.Service.NhanvienService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class NhanvienController {

    private NhanvienService nhanvienService;
    public NhanvienController(NhanvienService nhanvienService) {
        super();
        this.nhanvienService = nhanvienService;
    }

    @GetMapping("/nhanvien")
    public String listNhanvien(Model model) {
        model.addAttribute("nhanvien",nhanvienService.getAllNhanvien());
        return "nhanvien/index";
    }

    @GetMapping("/nhanvien/create")
    public String createNhanvien(Model model) {
        Nhanvien nhanvien = new Nhanvien();
        model.addAttribute("nhanvien",nhanvien);
        return "nhanvien/add";
    }

    @PostMapping("/nhanvien")
    public String saveNhanvien(@ModelAttribute("nhanvien") Nhanvien nhanvien) {
        nhanvienService.saveNhanvien(nhanvien);
        return "redirect:/nhanvien";
    }

    @GetMapping("/edit-nhanvien/{id}")
    public String editNhanvien(@PathVariable("id") int id, Model model) {
        model.addAttribute("nhanvien",nhanvienService.getNhanvienById(id));
        return "nhanvien/edit";
    }

    @PostMapping("/update-nhanvien")
    public String updateSanpham(@ModelAttribute Nhanvien nhanvien) {
        nhanvienService.updateNhanvien(nhanvien);
        return "redirect:/nhanvien";
    }

    @GetMapping("/delete-nhanvien/{id}")
    public String deleteNhanvien(@PathVariable("id") int id) {
        nhanvienService.deleteNhanvien(id);
        return "redirect:/nhanvien";
    }


}
