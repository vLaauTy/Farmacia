package Farmacia.Farmacia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// El POST de /login lo procesa Spring Security (ver UserConfig)
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
