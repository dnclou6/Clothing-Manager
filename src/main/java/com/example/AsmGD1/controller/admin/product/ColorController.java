package com.example.AsmGD1.controller.admin.product;

import com.example.AsmGD1.entity.product.Color;
import com.example.AsmGD1.service.product.ColorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/acvstore")
public class ColorController {
    @Autowired
    private ColorService colorService;

    @GetMapping("/colors")
    public String listColors(@RequestParam(value = "search", required = false) String search, Model model) {
        if (search != null && !search.trim().isEmpty()) {
            model.addAttribute("colors", colorService.searchColors(search));
        } else {
            model.addAttribute("colors", colorService.getAllColors());
        }
        return "WebQuanLy/product/colors";
    }

    @PostMapping("/colors/save")
    public String saveColor(@ModelAttribute Color color) {
        colorService.saveColor(color);
        return "redirect:/acvstore/colors";
    }

    @GetMapping("/colors/delete/{id}")
    public String deleteColor(@PathVariable Integer id) {
        colorService.deleteColor(id);
        return "redirect:/acvstore/colors";
    }
}