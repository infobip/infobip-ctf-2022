package org.ibctf.controller;

import org.ibctf.model.Partner;
import org.ibctf.repository.PartnerRepository;
import org.ibctf.repository.ShoppingItemRepository;
import org.ibctf.service.AuthenticationService;
import org.ibctf.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TemplateController {

    private final AuthenticationService authenticationService;
    private final TemplateService templateService;

    @Autowired
    public TemplateController(
            AuthenticationService authenticationService,
            TemplateService templateService,
            PartnerRepository partnerRepository,
            ShoppingItemRepository shoppingItemRepository) {
        this.authenticationService = authenticationService;
        this.templateService = templateService;
    }

    @GetMapping("/template")
    public String templateForm(Model model) throws Exception {
        Partner partner = authenticationService.currentUser();
        String template = templateService.fetchUserTemplateOrDefault(partner);
        model.addAttribute("template", template);
        return "template";
    }

    @PostMapping("/template")
    public String templateSubmit(@RequestParam("template") String template) throws Exception {
        Partner partner = authenticationService.currentUser();
        templateService.submitUserTemplate(partner, template);
        return "redirect:/template";
    }

    @GetMapping("/template/reset")
    public String templateDelete() throws Exception {
        Partner partner = authenticationService.currentUser();
        templateService.removeUserTemplate(partner);
        return "redirect:/template";
    }

    @GetMapping("/process")
    public String processItem(@RequestParam("id") Long id, Model model) throws Exception {
        Partner partner = authenticationService.currentUser();
        String result = templateService.processItem(id, partner);

        if (result == null) {
            return "redirect:/";
        }

        model.addAttribute("stream", result);
        return "process";
    }
}
