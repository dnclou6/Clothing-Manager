//package com.example.AsmGD1.controller;
//
//import com.example.AsmGD1.entity.Category;
//import com.example.AsmGD1.service.CategoryService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/categories")
//public class CategoryController {
//    @Autowired
//    private CategoryService categoryService;
//
//    @GetMapping("/categories")
//    public String listCategories(Model model) {
//        List<Category> categories = categoryService.getAllCategories();
//        model.addAttribute("categories", categories);
//        return "categories";
//    }
//
//    @PostMapping("/save")
//    public String saveCategory(@ModelAttribute Category category) {
//        categoryService.saveCategory(category);
//        return "redirect:/categories";
//    }
//
//    @GetMapping("/delete/{id}")
//    public String deleteCategory(@PathVariable Integer id) {
//        categoryService.deleteCategory(id);
//        return "redirect:/categories";
//    }
//}
