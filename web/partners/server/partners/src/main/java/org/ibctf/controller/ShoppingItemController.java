package org.ibctf.controller;

import org.ibctf.model.Partner;
import org.ibctf.model.ShoppingItem;
import org.ibctf.repository.ShoppingItemRepository;
import org.ibctf.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class ShoppingItemController {

    private final ShoppingItemRepository shoppingItemRepository;
    private final AuthenticationService authenticationService;

    @Autowired
    public ShoppingItemController(
            ShoppingItemRepository shoppingItemRepository,
            AuthenticationService authenticationService) {
        this.shoppingItemRepository = shoppingItemRepository;
        this.authenticationService = authenticationService;
    }

    @GetMapping
    public String index(Model model) throws Exception {
        Partner partner = authenticationService.currentUser();
        Iterable<ShoppingItem> items = shoppingItemRepository.findByPartner(partner);
        model.addAttribute("shoppingItems", items);
        return "index";
    }

    @GetMapping("/item")
    public String itemForm(@RequestParam(required = false) Long id, Model model) throws Exception {
        if (id == null) {
            model.addAttribute("shoppingItem", new ShoppingItem());
        } else {
            Partner partner = authenticationService.currentUser();
            Optional<ShoppingItem> item = shoppingItemRepository.findByIdAndPartner(id, partner);
            model.addAttribute("shoppingItem", item);
            model.addAttribute("id", id);
        }
        return "item";
    }

    @PostMapping("/item")
    public String createItem(
            @RequestParam(required = false) Long id,
            @ModelAttribute ShoppingItem shoppingItem,
            Model model) throws Exception {
        Partner partner = authenticationService.currentUser();
        shoppingItem.setPartner(partner);
        if (id != null) {
            shoppingItem.setId(id);
            Optional<ShoppingItem> item = shoppingItemRepository.findByIdAndPartner(id, partner);
            if (item.isEmpty()) {
                model.addAttribute("success", "Update failed, unknown shopping item");
                model.addAttribute("id", id);
                return "item";
            }
        }
        shoppingItemRepository.save(shoppingItem);
        return "redirect:/";
    }
}
