package com.company.aegis.demo;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoController {

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser != null) {
            model.addAttribute("userName", oidcUser.getName());
            model.addAttribute("attributes", oidcUser.getAttributes());
        }
        return "index";
    }
}
