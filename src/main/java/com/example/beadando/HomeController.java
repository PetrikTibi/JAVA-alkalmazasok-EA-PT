package com.example.beadando;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        System.out.println("--- A HOMECONTROLLER MEGHÍVVA! ---"); // Ezt írtuk hozzá
        return "index";
    }
}