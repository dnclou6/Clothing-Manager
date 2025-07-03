package com.example.AsmGD1.controller.admin;

import com.example.AsmGD1.entity.Product;
import com.example.AsmGD1.entity.ProductDetail;
import com.example.AsmGD1.service.QRCodeService;
import com.example.AsmGD1.service.admin.ProductDetailService;
import com.example.AsmGD1.service.admin.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/acvstore")
public class ProductQRCodeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductDetailService productDetailService;

    @Autowired
    private QRCodeService qrCodeService;

    private final String QR_UPLOAD_DIR;

    public ProductQRCodeController() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            QR_UPLOAD_DIR = "C:/DATN/uploads/qr/";
        } else {
            QR_UPLOAD_DIR = System.getProperty("user.home") + "/DATN/uploads/qr/";
        }

        // Tự động tạo thư mục QR nếu chưa tồn tại
        try {
            Path qrPath = Paths.get(QR_UPLOAD_DIR);
            if (!Files.exists(qrPath)) {
                Files.createDirectories(qrPath);
                System.out.println("Created QR directory: " + QR_UPLOAD_DIR);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create QR upload directory: " + QR_UPLOAD_DIR, e);
        }
    }

    @GetMapping("/product/qr-base64/{id}")
    @ResponseBody
    public String generateQRCodeBase64(@PathVariable("id") Integer id) throws Exception {
        Product product = productService.findById(id);
        if (product == null) {
            System.out.println("Product with ID " + id + " not found");
            return "Sản phẩm không tồn tại";
        }

        String base64Image = qrCodeService.generateQRCodeBase64(String.valueOf(product.getId()), 200, 200);
        return "<img src='data:image/png;base64," + base64Image + "' />";
    }

    @GetMapping("/product/qr-file/{id}")
    public ResponseEntity<Resource> serveQRCodeImage(@PathVariable("id") Integer id) throws Exception {
        ProductDetail detail = productDetailService.findById(id);
        if (detail == null) {
            System.out.println("ProductDetail with ID " + id + " not found");
            return ResponseEntity.notFound().build();
        }

        String filePath = QR_UPLOAD_DIR + "qr_" + id + ".png";
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                qrCodeService.generateQRCodeToFile(String.valueOf(id), 250, 250, filePath);
                System.out.println("Generated QR code for product detail ID: " + id);
            } catch (Exception e) {
                System.err.println("Failed to generate QR code: " + e.getMessage());
                throw e;
            }
        }

        Resource fileResource = new FileSystemResource(file);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(fileResource, headers, HttpStatus.OK);
    }

    @GetMapping("/product/qr-gallery")
    public String qrGallery(Model model) {
        model.addAttribute("products", productService.findAll());
        return "WebQuanLy/qr_gallery";
    }
}