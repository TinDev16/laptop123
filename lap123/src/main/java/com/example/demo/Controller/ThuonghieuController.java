package com.example.demo.Controller;

import com.example.demo.Entity.Thuonghieu;
import com.example.demo.Service.ThuonghieuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ThuonghieuController {

    ThuonghieuService thuonghieuService;

    public ThuonghieuController(ThuonghieuService thuonghieuService) {
        super();
        this.thuonghieuService = thuonghieuService;
    }

    @GetMapping("/thuonghieu")
    public String thuonghieu(Model model) {
        model.addAttribute("thuonghieu",thuonghieuService.getAllThuonghieu());
        return "thuonghieu/index";
    }

    @GetMapping("/thuonghieu/create")
    public String thuonghieuCreate(Model model) {
        Thuonghieu thuonghieu = new Thuonghieu();
        model.addAttribute("thuonghieu",thuonghieu);
        return "thuonghieu/add";
    }

    @PostMapping("/thuonghieu")
    public String saveThuonghieu(@ModelAttribute Thuonghieu thuonghieu) {
        thuonghieuService.save(thuonghieu);
        return "redirect:/thuonghieu";
    }

    @GetMapping("/delete-thuonghieu/{id}")
    public String deleteThuonghieu(@PathVariable int id) {
        thuonghieuService.deleteThuonghieu(id);
        return "redirect:/thuonghieu";
    }
}
