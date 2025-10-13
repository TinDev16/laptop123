package com.example.demo.Controller;

import com.example.demo.Entity.Thuonghieu;
import com.example.demo.Service.ThuonghieuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/edit-thuonghieu/{id}")
    public String editThuonghieu(@PathVariable int id, Model model) {
        model.addAttribute("thuonghieu",thuonghieuService.getThuonghieuById(id));
        return "thuonghieu/edit";
    }


    @PostMapping("/update-thuonghieu")
    public String updateThuonghieu(@ModelAttribute Thuonghieu thuonghieu) {
        thuonghieuService.save(thuonghieu);
        return "redirect:/thuonghieu";
    }

    @GetMapping("/delete-thuonghieu/{id}")
    public String deleteThuonghieu(@PathVariable int id, Model model) {

        Thuonghieu thuonghieu = thuonghieuService.getThuonghieuById(id);
        model.addAttribute("thuonghieu",thuonghieu);
        return "redirect:/delete";
    }

    @PostMapping("/delete-thuonghieu")
    public String deleteSanpham(@RequestParam("id") int id,
                                @RequestParam("GHICHU") String GHICHU) {
        thuonghieuService.deleteThuonghieu(id, GHICHU);
        return "redirect:/thuonghieu";
    }
}
