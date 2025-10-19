package com.example.demo.Controller;

import com.example.demo.Entity.NCC;
import com.example.demo.Service.NCCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

    @GetMapping("/delete-ncc/{id}")
    public String deleteNhacungcap(@PathVariable("id") int id, Model model) {
        NCC ncc = nccService.getNCCById(id);
        model.addAttribute("ncc", ncc);
        return "redirect:/ncc";
    }

    @PostMapping("/delete-ncc")
    public String deleteNhacungcap(@RequestParam("id") int id,
                                   @RequestParam("GHICHU") String GHICHU) {
        nccService.deleteNCC(id, GHICHU);
        return "redirect:/ncc";
    }

}
