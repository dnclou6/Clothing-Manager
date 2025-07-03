package com.example.AsmGD1.controller.admin;

import com.example.AsmGD1.entity.Product;
import com.example.AsmGD1.service.admin.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/acvstore/api/products")
public class ProductApiController {

    @Autowired
    private ProductService productService;

    @GetMapping("/all")
    public List<Product> getAllProducts() {
        return productService.findAll();
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam("keyword") String keyword) {
        return productService.findByNameContaining(keyword);
    }
}