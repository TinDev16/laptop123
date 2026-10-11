
package com.example.demo.Controller;

import com.example.demo.Service.BomService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/bom")
public class AdminBomController {

    private final BomService bomService;

    public AdminBomController(BomService bomService) {
        this.bomService = bomService;
    }

    @GetMapping("/{id}")
    public String chiTietBom(
            @PathVariable long id,
            Model model) {

        try {
            model.addAttribute("bom", bomService.get(id));
            model.addAttribute("details", bomService.details(id));
            return "admin/bom-detail";

        } catch (EmptyResultDataAccessException e) {
            return "redirect:/admin/sanpham?bomNotFound";
        }
    }
}
