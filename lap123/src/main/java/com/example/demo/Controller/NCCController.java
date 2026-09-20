package com.example.demo.Controller;

import com.example.demo.Entity.NCC;
import com.example.demo.Service.NCCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class NCCController {

    @Autowired
    NCCService nccService;

    @GetMapping("/ncc")
    public String nhacungcap(Model model) {
        model.addAttribute("ncc", nccService.getAllNCC());
        return "ncc/index";
    }

    @GetMapping("/ncc/create")
    public String createNhacungcap(Model model) {
        NCC ncc = new NCC();
        model.addAttribute("ncc", ncc);
        return "ncc/add";
    }

    @PostMapping("/ncc")
    public String saveNhacungcap(@ModelAttribute("ncc") NCC ncc) {
        nccService.saveNCC(ncc);
        return "redirect:/ncc";
    }

    @GetMapping("/edit-ncc/{id}")
    public String editNhacungcap(Model model, @PathVariable int id) {
        model.addAttribute("ncc",nccService.getNCCById(id));
        return "ncc/edit";
    }

    @PostMapping("/update-ncc")
    public String updateNhacungcap(@ModelAttribute("ncc") NCC ncc) {
        nccService.updateNCC(ncc);
        return "redirect:/ncc";
    }

    @PostMapping("/delete-ncc")
    public String deleteNhacungcap(@RequestParam("id") int id,
                                   @RequestParam("GHICHU") String GHICHU) {
        nccService.deleteNCC(id, GHICHU);
        return "redirect:/ncc";
    }

    @PostMapping("/permanently-delete-ncc")
    public String permanentlyDelete(@RequestParam("id") int id, RedirectAttributes redirectAttributes) {
        try {
            nccService.permanentlyDeleteNCC(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa bản ghi không có dữ liệu liên quan.");
        } catch (IllegalArgumentException | IllegalStateException | DataIntegrityViolationException ex) {
            String message = ex instanceof DataIntegrityViolationException
                    ? "Không thể xóa vì bản ghi đang được sử dụng trong dữ liệu nghiệp vụ."
                    : ex.getMessage();
            redirectAttributes.addFlashAttribute("error", message);
        }
        return "redirect:/ncc";
    }
}
