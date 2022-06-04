package org.ibctf.controller;

import net.bytebuddy.utility.RandomString;
import org.ibctf.model.Partner;
import org.ibctf.service.AuthenticationService;
import org.ibctf.util.WebConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.security.auth.message.AuthException;
import javax.servlet.http.HttpServletResponse;
import java.security.PublicKey;
import java.util.Base64;

@Controller
public class PartnerController {

    private final AuthenticationService authenticationService;

    @Autowired
    public PartnerController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        return plainView("login", model);
    }

    @PostMapping("/login")
    public String login(@ModelAttribute Partner partner, Model model, HttpServletResponse response) {
        try {
            String token = authenticationService.login(partner);
            authenticationService.plantAuthCookie(response, token);
        } catch(Exception e) {
            model.addAttribute("success", "Login failed with " + e.getMessage());
            return plainView("/login", model);
        }
        return "redirect:/";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        return plainView("register", model);
    }

    @PostMapping("/register")
    public String register(@ModelAttribute Partner partner, Model model) {
        try {
            authenticationService.register(partner);
            model.addAttribute("success", "Registration success");
        } catch(Exception e) {
            model.addAttribute("success", "Registration failed with " + e.getMessage());
            return plainView("register", model);
        }
        return plainView("login", model);
    }

    @GetMapping("/keys")
    public String keyRegistration(Model model) {
        model.addAttribute("key", null);
        try {
            Partner partner = authenticationService.currentUser();
            PublicKey key = authenticationService.loadKeyFile(partner.getUsername());
            model.addAttribute("key", Base64.getEncoder().encodeToString(key.getEncoded()));
        } catch (Exception ignored) {
        }
        return "keys";
    }

    @GetMapping("/otp")
    public String otpForm(Model model) {
        model.addAttribute("challenge", RandomString.make(10));
        return plainView("otp", model);
    }

    @PostMapping("/otp")
    public String otp(
            @ModelAttribute Partner partner,
            @RequestParam String challenge,
            Model model,
            HttpServletResponse response) {
        try {
            if (challenge == null || challenge.isEmpty()) {
                throw new AuthException("no challenge");
            }
            byte[] chResponse = Base64.getDecoder().decode(partner.getPassword());
            if (!authenticationService.verifyRsa(challenge.getBytes(), chResponse, partner.getUsername())) {
                throw new AuthException("validation");
            }
            String token = authenticationService.jwt(partner.getUsername(), WebConst.AUTHENTICATION_LEVEL_HIGH);
            authenticationService.plantAuthCookie(response, token);
        } catch (Exception e) {
            model.addAttribute("success", "Challenge failed with " + e.getMessage());
            return plainView("/login", model);
        }
        return "redirect:/";
    }

    private String plainView(String path, Model model) {
        model.addAttribute("partner", new Partner());
        return path;
    }
}
