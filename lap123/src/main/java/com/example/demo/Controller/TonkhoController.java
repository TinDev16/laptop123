package com.example.demo.Controller;

import com.example.demo.Service.TonkhoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TonkhoController {

    @Autowired
    TonkhoService tonkhoService;

    @GetMapping("/tonkho")
    public String Tonkho(Model model) {
        model.addAttribute("tonkho", tonkhoService.getAllTonkho());
        return "tonkho/index";
    }
}
