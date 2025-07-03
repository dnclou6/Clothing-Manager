package com.example.AsmGD1.controller.admin.product;

import com.example.AsmGD1.entity.product.Size;
import com.example.AsmGD1.service.product.SizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/acvstore")
public class SizeController {
    @Autowired
    private SizeService sizeService;

    @GetMapping("/sizes")
    public String listSizes(@RequestParam(value = "search", required = false) String search, Model model) {
        if (search != null && !search.trim().isEmpty()) {
            model.addAttribute("sizes", sizeService.searchSizes(search));
        } else {
            model.addAttribute("sizes", sizeService.getAllSize());
        }
        return "WebQuanLy/product/sizes";
    }

    @PostMapping("/sizes/save")
    public String saveSize(@ModelAttribute Size size) {
        sizeService.save(size);
        return "redirect:/acvstore/sizes";
    }

    @GetMapping("/sizes/delete/{id}")
    public String deleteSize(@PathVariable Integer id) {
        sizeService.delete(id);
        return "redirect:/acvstore/sizes";
    }
}