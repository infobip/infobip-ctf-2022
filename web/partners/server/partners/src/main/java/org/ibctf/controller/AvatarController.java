package org.ibctf.controller;

import org.ibctf.model.Partner;
import org.ibctf.service.AuthenticationService;
import org.ibctf.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AvatarController {

    private final AuthenticationService authenticationService;
    private final FileService fileService;

    @Autowired
    public AvatarController(AuthenticationService authenticationService, FileService fileService) {
        this.authenticationService = authenticationService;
        this.fileService = fileService;
    }

    @GetMapping("/avatar")
    public String profileImage(Model model) throws Exception {
        Partner partner = authenticationService.currentUser();
        model.addAttribute("avatar", partner.getAvatar());
        return "image";
    }

    @PostMapping("/avatar")
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {
        Partner partner = authenticationService.currentUser();
        fileService.fileToAvatar(file, partner);
        return "redirect:/avatar";
    }
}
