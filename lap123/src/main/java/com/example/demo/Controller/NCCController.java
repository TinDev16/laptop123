package com.example.demo.Controller;

import com.example.demo.Entity.NCC;
import com.example.demo.Service.NCCService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class NCCController {

    NCCService nccService;

    public NCCController(NCCService nccService) {
        this.nccService = nccService;
    }

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

    @GetMapping("/delete-ncc/{id}")
    public String deleteNhacungcap(@PathVariable("id") int id) {
        nccService.deleteNCC(id);
        return "redirect:/ncc";
    }



}
