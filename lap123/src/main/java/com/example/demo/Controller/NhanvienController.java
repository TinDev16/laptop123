package com.example.demo.Controller;
import com.example.demo.Entity.Nhanvien;
import com.example.demo.Service.NhanvienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class NhanvienController {

    @Autowired
    private NhanvienService nhanvienService;

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

    @PostMapping("/delete-nhanvien")
    public String deleteSanpham(@RequestParam("id") int id,
                                @RequestParam("GHICHU") String GHICHU) {
        nhanvienService.deleteNhanvien(id, GHICHU);
        return "redirect:/nhanvien";
    }

    @PostMapping("/permanently-delete-nhanvien")
    public String permanentlyDelete(@RequestParam("id") int id, RedirectAttributes redirectAttributes) {
        try {
            nhanvienService.permanentlyDeleteNhanvien(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa bản ghi không có dữ liệu liên quan.");
        } catch (IllegalArgumentException | IllegalStateException | DataIntegrityViolationException ex) {
            String message = ex instanceof DataIntegrityViolationException
                    ? "Không thể xóa vì bản ghi đang được sử dụng trong dữ liệu nghiệp vụ."
                    : ex.getMessage();
            redirectAttributes.addFlashAttribute("error", message);
        }
        return "redirect:/nhanvien";
    }
}
