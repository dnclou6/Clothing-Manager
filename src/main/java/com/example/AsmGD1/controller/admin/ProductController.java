package com.example.AsmGD1.controller.admin;

import com.example.AsmGD1.entity.Category;
import com.example.AsmGD1.entity.Product;
import com.example.AsmGD1.service.CategoryService;
import com.example.AsmGD1.service.admin.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/acvstore/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    private final String UPLOAD_DIR;

    public ProductController() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            UPLOAD_DIR = "C:/DATN/uploads/";
        } else {
            UPLOAD_DIR = System.getProperty("user.home") + "/DATN/uploads/";
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created directory: " + UPLOAD_DIR);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + UPLOAD_DIR, e);
        }
    }

    @GetMapping
    public String viewProductPage(
            Model model,
            @RequestParam(value = "searchName", required = false) String searchName,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "page", defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 10); // 10 phần tử mỗi trang
        Page<Product> productPage;

        if (searchName != null && !searchName.isEmpty()) {
            productPage = productService.findByNameContaining(searchName, categoryId, pageable);
        } else if (categoryId != null) {
            productPage = productService.findByCategoryId(categoryId, pageable);
        } else {
            productPage = productService.findAllPaginated(pageable);
        }

        model.addAttribute("productList", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("searchName", searchName);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "/WebQuanLy/product-list-form";
    }

    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable("id") Integer id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("productList", productService.findAll());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "/WebQuanLy/product-list-form";
    }

    @PostMapping("/save")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> saveProduct(
            @ModelAttribute Product product,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(value = "category.id", required = false) Integer categoryId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Product existingProduct = productService.findById(product.getId());
            if (existingProduct == null) {
                response.put("success", false);
                response.put("message", "Sản phẩm không tồn tại!");
                return ResponseEntity.badRequest().body(response);
            }

            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());

            if (categoryId != null) {
                Category category = categoryService.getCategoryById(categoryId);
                if (category != null) {
                    existingProduct.setCategory(category);
                } else {
                    existingProduct.setCategory(null);
                }
            }

            if (!imageFile.isEmpty()) {
                if (existingProduct.getImageUrl() != null) {
                    String oldImagePath = existingProduct.getImageUrl().split("\\?")[0].replace("/Uploads/", "");
                    File oldFile = new File(UPLOAD_DIR + oldImagePath);
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }
                String originalFileName = imageFile.getOriginalFilename();
                String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                String newFileName = UUID.randomUUID().toString() + fileExtension;
                String filePath = UPLOAD_DIR + newFileName;
                Path path = Paths.get(filePath);
                Files.copy(imageFile.getInputStream(), path);
                String imageUrl = "/Uploads/" + newFileName + "?v=" + System.currentTimeMillis();
                existingProduct.setImageUrl(imageUrl);
            }

            productService.save(existingProduct);
            response.put("success", true);
            response.put("message", "Lưu sản phẩm thành công!");
            response.put("product", existingProduct);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi lưu sản phẩm: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Integer id) {
        Product product = productService.findById(id);
        if (product != null && product.getImageUrl() != null) {
            String imagePath = product.getImageUrl().split("\\?")[0].replace("/Uploads/", "");
            File file = new File(UPLOAD_DIR + imagePath);
            if (file.exists()) {
                file.delete();
            }
        }
        productService.deleteById(id);
        return "redirect:/acvstore/products";
    }

    @GetMapping("/get/{id}")
    @ResponseBody
    public ResponseEntity<Product> getProduct(@PathVariable("id") Integer id) {
        Product product = productService.findById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/quick-add-product")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> quickAddProduct(
            @ModelAttribute Product product,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "category.id", required = false) Integer categoryId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Gán danh mục nếu categoryId có giá trị
            if (categoryId != null) {
                Category category = categoryService.getCategoryById(categoryId);
                if (category != null) {
                    product.setCategory(category);
                } else {
                    product.setCategory(null); // Nếu không tìm thấy category, đặt null
                }
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                String originalFileName = imageFile.getOriginalFilename();
                String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                String newFileName = UUID.randomUUID().toString() + fileExtension;
                String filePath = UPLOAD_DIR + newFileName;

                Path path = Paths.get(filePath);
                Files.copy(imageFile.getInputStream(), path);

                File savedFile = new File(filePath);
                if (!savedFile.exists()) {
                    throw new IOException("Không thể lưu file ảnh: " + filePath);
                }

                String imageUrl = "/Uploads/" + newFileName + "?v=" + System.currentTimeMillis();
                product.setImageUrl(imageUrl);
            }

            productService.save(product);
            Product savedProduct = productService.findById(product.getId());
            response.put("success", true);
            response.put("message", "Thêm sản phẩm thành công!");
            response.put("product", savedProduct);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Lỗi khi thêm sản phẩm: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}