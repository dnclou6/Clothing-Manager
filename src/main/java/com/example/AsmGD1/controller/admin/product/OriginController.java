package com.example.AsmGD1.controller.admin.product;

import com.example.AsmGD1.entity.product.Origin;
import com.example.AsmGD1.service.product.OriginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/acvstore")
public class OriginController {
    @Autowired
    private OriginService originService;

    @GetMapping("/origins")
    public String listOrigins(@RequestParam(value = "search", required = false) String search, Model model) {
        if (search != null && !search.trim().isEmpty()) {
            model.addAttribute("origins", originService.searchOrigins(search));
        } else {
            model.addAttribute("origins", originService.getAllOrigin());
        }
        return "WebQuanLy/product/origins";
    }

    @PostMapping("/origins/save")
    public String saveOrigin(@ModelAttribute Origin origin) {
        originService.save(origin);
        return "redirect:/acvstore/origins";
    }

    @GetMapping("/origins/delete/{id}")
    public String deleteOrigin(@PathVariable Integer id) {
        originService.delete(id);
        return "redirect:/acvstore/origins";
    }
}