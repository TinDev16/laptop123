package com.example.demo.Controller;

import com.example.demo.Entity.Thuonghieu;
import com.example.demo.Service.ThuonghieuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ThuonghieuController {

    @Autowired
    ThuonghieuService thuonghieuService;

    @GetMapping("/thuonghieu")
    public String thuonghieu(Model model) {
        model.addAttribute("thuonghieu", thuonghieuService.getAllThuonghieu());
        return "thuonghieu/index";
    }

    @GetMapping("/thuonghieu/create")
    public String thuonghieuCreate(Model model) {
        model.addAttribute("thuonghieu", new Thuonghieu());
        return "thuonghieu/add";
    }

    @PostMapping("/thuonghieu")
    public String saveThuonghieu(@ModelAttribute Thuonghieu thuonghieu) {
        thuonghieuService.save(thuonghieu);
        return "redirect:/thuonghieu";
    }

    @GetMapping("/edit-thuonghieu/{id}")
    public String editThuonghieu(@PathVariable int id, Model model) {
        model.addAttribute("thuonghieu",
                thuonghieuService.getThuonghieuById(id));
        return "thuonghieu/edit";
    }

    @PostMapping("/update-thuonghieu")
    public String updateThuonghieu(@ModelAttribute Thuonghieu thuonghieu) {
        thuonghieuService.save(thuonghieu);
        return "redirect:/thuonghieu";
    }

    // Ngừng sử dụng thương hiệu và lưu lý do.
    @PostMapping("/delete-thuonghieu")
    public String deleteThuonghieu(
            @RequestParam("id") int id,
            @RequestParam("GHICHU") String GHICHU) {

        thuonghieuService.deleteThuonghieu(id, GHICHU);
        return "redirect:/thuonghieu";
    }

    // Khôi phục thương hiệu.
    @PostMapping("/thuonghieu/khoi-phuc")
    public String restoreThuonghieu(@RequestParam("id") int id) {
        thuonghieuService.restoreThuonghieu(id);
        return "redirect:/thuonghieu";
    }
}