package com.example.AsmGD1.controller.admin.product;

import com.example.AsmGD1.entity.product.Style;
import com.example.AsmGD1.service.product.StyleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/acvstore")
public class StyleController {
    @Autowired
    private StyleService styleService;

    @GetMapping("/styles")
    public String listStyles(@RequestParam(value = "search", required = false) String search, Model model) {
        if (search != null && !search.trim().isEmpty()) {
            model.addAttribute("styles", styleService.searchStyles(search));
        } else {
            model.addAttribute("styles", styleService.getAllStyle());
        }
        return "WebQuanLy/product/styles";
    }

    @PostMapping("/styles/save")
    public String saveStyle(@ModelAttribute Style style) {
        styleService.save(style);
        return "redirect:/acvstore/styles";
    }

    @GetMapping("/styles/delete/{id}")
    public String deleteStyle(@PathVariable Integer id) {
        styleService.delete(id);
        return "redirect:/acvstore/styles";
    }
}