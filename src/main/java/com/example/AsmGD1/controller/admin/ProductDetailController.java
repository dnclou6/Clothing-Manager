package com.example.AsmGD1.controller.admin;

import com.example.AsmGD1.dto.ProductDetailBatchDto;
import com.example.AsmGD1.dto.ProductDetailUpdateDto;
import com.example.AsmGD1.entity.Product;
import com.example.AsmGD1.entity.ProductDetail;
import com.example.AsmGD1.entity.product.*;
import com.example.AsmGD1.service.CategoryService;
import com.example.AsmGD1.service.admin.ProductDetailService;
import com.example.AsmGD1.service.admin.ProductService;
import com.example.AsmGD1.service.product.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/acvstore/product-details")
public class ProductDetailController {

    private static final Logger logger = LoggerFactory.getLogger(ProductDetailController.class);

    @Autowired private ProductDetailService productDetailService;
    @Autowired private ProductService productService;
    @Autowired private ColorService colorService;
    @Autowired private SizeService sizeService;
    @Autowired private MaterialService materialService;
    @Autowired private OriginService originService;
    @Autowired private StyleService styleService;
    @Autowired private CategoryService categoryService;

    private static final String UPLOAD_DIR = "C:/DATN/uploads/";

    @GetMapping
    public String viewAll(Model model, @RequestParam(value = "productId", required = false) Integer productId) {
        try {
            logger.info("Loading product details page with productId: {}", productId);
            model.addAttribute("product", new Product());
            model.addAttribute("products", productService.findAll());
            model.addAttribute("colors", colorService.getAllColors());
            model.addAttribute("sizes", sizeService.getAllSize());
            model.addAttribute("materials", materialService.getAllMaterial());
            model.addAttribute("origins", originService.getAllOrigin());
            model.addAttribute("styles", styleService.getAllStyle());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("allProducts", productService.findAll());
            model.addAttribute("allColors", colorService.getAllColors());
            model.addAttribute("allSizes", sizeService.getAllSize());
            model.addAttribute("allMaterials", materialService.getAllMaterial());
            model.addAttribute("allOrigins", originService.getAllOrigin());
            model.addAttribute("allStyles", styleService.getAllStyle());

            if (productId != null) {
                Product selectedProduct = productService.findById(productId);
                if (selectedProduct != null) {
                    model.addAttribute("selectedProductId", productId);
                    model.addAttribute("productDetailList", productDetailService.findByProductId(productId));
                } else {
                    logger.warn("Product with ID {} not found", productId);
                    model.addAttribute("productDetailList", productDetailService.findAll());
                }
            } else {
                model.addAttribute("productDetailList", productDetailService.findAll());
            }
            return "/WebQuanLy/product-detail-batch-form";
        } catch (Exception e) {
            logger.error("Error loading product details page: ", e);
            model.addAttribute("error", "Lỗi khi tải trang: " + e.getMessage());
            return "/WebQuanLy/error";
        }
    }

    @PostMapping("/save-batch")
    public String saveProductDetailBatch(@ModelAttribute ProductDetailBatchDto batchDto,
                                         @RequestParam(value = "variations.imageFiles", required = false) List<MultipartFile> imageFiles,
                                         Model model) {
        try {
            logger.info("Saving batch product details for productId: {}", batchDto.getProductId());
            productDetailService.saveProductDetailVariationsDto(batchDto);
            return "redirect:/acvstore/product-details?productId=" + batchDto.getProductId();
        } catch (Exception e) {
            logger.error("Error saving batch product details: ", e);
            model.addAttribute("error", "Lỗi khi lưu chi tiết sản phẩm: " + e.getMessage());
            return "/WebQuanLy/product-detail-batch-form";
        }
    }

    @GetMapping("/edit/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductDetail(@PathVariable Integer id) {
        try {
            logger.info("Fetching product detail with ID: {}", id);
            ProductDetail productDetail = productDetailService.findById(id);
            if (productDetail == null) {
                logger.warn("Product detail with ID {} not found", id);
                return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy chi tiết sản phẩm"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", productDetail.getId());
            response.put("productId", productDetail.getProduct().getId());
            response.put("colorId", productDetail.getColor().getId());
            response.put("sizeId", productDetail.getSize().getId());
            response.put("originId", productDetail.getOrigin().getId());
            response.put("materialId", productDetail.getMaterial().getId());
            response.put("styleId", productDetail.getStyle().getId());
            response.put("price", productDetail.getPrice());
            response.put("stockQuantity", productDetail.getStockQuantity());
            response.put("images", productDetail.getImages().stream()
                    .map(img -> Map.of("id", img.getId(), "imageUrl", img.getImageUrl()))
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching product detail with ID {}: ", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi tải chi tiết sản phẩm: " + e.getMessage()));
        }
    }

    @PostMapping("/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProductDetail(@PathVariable Integer id,
                                                                   @ModelAttribute ProductDetailUpdateDto updateDto,
                                                                   @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {
        try {
            logger.info("Updating product detail with ID: {}", id);
            updateDto.setId(id);
            productDetailService.updateProductDetail(updateDto, imageFiles);
            return ResponseEntity.ok(Map.of("message", "Cập nhật chi tiết sản phẩm thành công"));
        } catch (Exception e) {
            logger.error("Error updating product detail with ID {}: ", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi cập nhật chi tiết sản phẩm: " + e.getMessage()));
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteProductDetail(@PathVariable Integer id, @RequestParam(value = "productId", required = false) Integer productId) {
        try {
            logger.info("Deleting product detail with ID: {}", id);
            productDetailService.deleteById(id);
            String redirectUrl = "/acvstore/product-details";
            if (productId != null) {
                redirectUrl += "?productId=" + productId;
            }
            return "redirect:" + redirectUrl + "&success=Xóa thành công";
        } catch (Exception e) {
            logger.error("Error deleting product detail with ID {}: ", id, e);
            String redirectUrl = "/acvstore/product-details?error=" + e.getMessage();
            if (productId != null) {
                redirectUrl += "&productId=" + productId;
            }
            return "redirect:" + redirectUrl;
        }
    }

    @PostMapping("/batch-delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> batchDeleteProductDetails(@RequestBody Map<String, List<Integer>> payload) {
        try {
            List<Integer> ids = payload.get("ids");
            logger.info("Batch deleting product details with IDs: {}", ids);
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không có ID nào được cung cấp"));
            }
            productDetailService.batchDelete(ids);
            return ResponseEntity.ok(Map.of("message", "Xóa hàng loạt chi tiết sản phẩm thành công"));
        } catch (Exception e) {
            logger.error("Error batch deleting product details: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi xóa hàng loạt: " + e.getMessage()));
        }
    }

    @PostMapping("/batch-update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> batchUpdateProductDetails(@ModelAttribute ProductDetailUpdateDto updateDto,
                                                                         @RequestParam("ids") String ids) {
        try {
            logger.info("Batch updating product details with IDs: {}", ids);
            List<Integer> idList = List.of(ids.split(",")).stream()
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            if (idList.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không có ID nào được cung cấp"));
            }
            productDetailService.batchUpdate(updateDto, idList);
            return ResponseEntity.ok(Map.of("message", "Cập nhật hàng loạt chi tiết sản phẩm thành công"));
        } catch (Exception e) {
            logger.error("Error batch updating product details: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi cập nhật hàng loạt: " + e.getMessage()));
        }
    }

    @PostMapping("/delete-image/{imageId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteImage(@PathVariable Integer imageId) {
        try {
            logger.info("Deleting image with ID: {}", imageId);
            productDetailService.deleteImage(imageId);
            return ResponseEntity.ok(Map.of("message", "Xóa ảnh thành công"));
        } catch (Exception e) {
            logger.error("Error deleting image with ID {}: ", imageId, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi xóa ảnh: " + e.getMessage()));
        }
    }

    @PostMapping("/quick-add-color")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> quickAddColor(@RequestBody Map<String, String> payload) {
        try {
            String name = payload.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tên màu sắc không được để trống"));
            }
            logger.info("Quick adding color: {}", name);
            Color color = new Color();
            color.setName(name);
            Color savedColor = colorService.saveColor(color);
            return ResponseEntity.ok(Map.of("id", savedColor.getId(), "name", savedColor.getName()));
        } catch (Exception e) {
            logger.error("Error quick adding color: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi thêm màu sắc: " + e.getMessage()));
        }
    }

    @PostMapping("/quick-add-size")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> quickAddSize(@RequestBody Map<String, String> payload) {
        try {
            String name = payload.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tên kích cỡ không được để trống"));
            }
            logger.info("Quick adding size: {}", name);
            Size size = new Size();
            size.setName(name);
            Size savedSize = sizeService.save(size);
            return ResponseEntity.ok(Map.of("id", savedSize.getId(), "name", savedSize.getName()));
        } catch (Exception e) {
            logger.error("Error quick adding size: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi thêm kích cỡ: " + e.getMessage()));
        }
    }

    @PostMapping("/quick-add-material")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> quickAddMaterial(@RequestBody Map<String, String> payload) {
        try {
            String name = payload.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tên chất liệu không được để trống"));
            }
            logger.info("Quick adding material: {}", name);
            Material material = new Material();
            material.setName(name);
            Material savedMaterial = materialService.save(material);
            return ResponseEntity.ok(Map.of("id", savedMaterial.getId(), "name", savedMaterial.getName()));
        } catch (Exception e) {
            logger.error("Error quick adding material: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi thêm chất liệu: " + e.getMessage()));
        }
    }

    @PostMapping("/quick-add-origin")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> quickAddOrigin(@RequestBody Map<String, String> payload) {
        try {
            String name = payload.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tên thương hiệu không được để trống"));
            }
            logger.info("Quick adding origin: {}", name);
            Origin origin = new Origin();
            origin.setName(name);
            Origin savedOrigin = originService.save(origin);
            return ResponseEntity.ok(Map.of("id", savedOrigin.getId(), "name", savedOrigin.getName()));
        } catch (Exception e) {
            logger.error("Error quick adding origin: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi thêm thương hiệu: " + e.getMessage()));
        }
    }

    @PostMapping("/quick-add-style")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> quickAddStyle(@RequestBody Map<String, String> payload) {
        try {
            String name = payload.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tên kiểu dáng không được để trống"));
            }
            logger.info("Quick adding style: {}", name);
            Style style = new Style();
            style.setName(name);
            Style savedStyle = styleService.save(style);
            return ResponseEntity.ok(Map.of("id", savedStyle.getId(), "name", savedStyle.getName()));
        } catch (Exception e) {
            logger.error("Error quick adding style: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi thêm kiểu dáng: " + e.getMessage()));
        }
    }

    @ModelAttribute("allProducts")
    public List<Product> getAllProducts() {
        return productService.findAll();
    }

    @ModelAttribute("allColors")
    public List<Color> getAllColors() {
        return colorService.getAllColors();
    }

    @ModelAttribute("allSizes")
    public List<Size> getAllSizes() {
        return sizeService.getAllSize();
    }

    @ModelAttribute("allMaterials")
    public List<Material> getAllMaterials() {
        return materialService.getAllMaterial();
    }

    @ModelAttribute("allOrigins")
    public List<Origin> getAllOrigins() {
        return originService.getAllOrigin();
    }

    @ModelAttribute("allStyles")
    public List<Style> getAllStyles() {
        return styleService.getAllStyle();
    }
}