package com.example.AsmGD1.controller.admin.product;

import com.example.AsmGD1.entity.product.Material;
import com.example.AsmGD1.service.product.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/acvstore")
public class MaterialController {
    @Autowired
    private MaterialService materialService;

    @GetMapping("/materials")
    public String listMaterials(@RequestParam(value = "search", required = false) String search, Model model) {
        if (search != null && !search.trim().isEmpty()) {
            model.addAttribute("materials", materialService.searchMaterials(search));
        } else {
            model.addAttribute("materials", materialService.getAllMaterial());
        }
        return "WebQuanLy/product/materials";
    }

    @PostMapping("/materials/save")
    public String saveMaterial(@ModelAttribute Material material) {
        materialService.save(material);
        return "redirect:/acvstore/materials";
    }

    @GetMapping("/materials/delete/{id}")
    public String deleteMaterial(@PathVariable Integer id) {
        materialService.delete(id);
        return "redirect:/acvstore/materials";
    }
}