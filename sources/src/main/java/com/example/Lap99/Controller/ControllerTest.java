package com.example.Lap99.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControllerTest {

    @GetMapping("/index")
    public String home() {
        return "index";
    }

    @GetMapping("/testAPI")
    public String testAPI() {
        return "testAPI";
    }
}
