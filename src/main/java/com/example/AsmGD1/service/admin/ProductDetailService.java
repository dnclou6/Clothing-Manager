package com.example.AsmGD1.service.admin;

import com.example.AsmGD1.dto.ProductDetailBatchDto;
import com.example.AsmGD1.dto.ProductDetailUpdateDto;
import com.example.AsmGD1.dto.ProductDetailVariationDto;
import com.example.AsmGD1.entity.Product;
import com.example.AsmGD1.entity.ProductDetail;
import com.example.AsmGD1.entity.product.*;
import com.example.AsmGD1.repository.admin.ProductDetailRepository;
import com.example.AsmGD1.repository.admin.ProductRepository;
import com.example.AsmGD1.repository.product.*;
import com.example.AsmGD1.util.QRCodeUtil;
import com.google.zxing.WriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductDetailService {

    private static final Logger logger = LoggerFactory.getLogger(ProductDetailService.class);

    @Autowired private ProductDetailRepository productDetailRepo;
    @Autowired private ProductImageRepository productImageRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private ColorRepository colorRepo;
    @Autowired private SizeRepository sizeRepo;
    @Autowired private OriginRepository originRepo;
    @Autowired private MaterialRepository materialRepo;
    @Autowired private StyleRepository styleRepo;

    private final String UPLOAD_DIR;

    public ProductDetailService() {
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

    public List<ProductDetail> findAll() {
        return productDetailRepo.findAll();
    }

    public List<ProductDetail> findByProductId(Integer productId) {
        return productDetailRepo.findByProductId(productId);
    }

    public ProductDetail findById(Integer id) {
        return productDetailRepo.findById(id).orElse(null);
    }

    @Transactional
    public void deleteById(Integer id) {
        logger.info("Bắt đầu xóa ProductDetail với ID: {}", id);
        ProductDetail pd = findById(id);
        if (pd != null) {
            logger.info("Tìm thấy ProductDetail: {}", pd.getId());
            for (ProductImage image : pd.getImages()) {
                try {
                    Path imagePath = Paths.get(UPLOAD_DIR + image.getImageUrl());
                    logger.info("Xóa tệp ảnh: {}", imagePath);
                    Files.deleteIfExists(imagePath);
                    productImageRepo.delete(image);
                } catch (IOException e) {
                    logger.error("Lỗi khi xóa tệp ảnh: {}", image.getImageUrl(), e);
                    throw new RuntimeException("Không thể xóa tệp ảnh: " + image.getImageUrl(), e);
                }
            }
            logger.info("Xóa ProductDetail: {}", pd.getId());
            productDetailRepo.delete(pd);
        } else {
            logger.warn("Không tìm thấy ProductDetail với ID: {}", id);
            throw new RuntimeException("Không tìm thấy ProductDetail với ID: " + id);
        }
    }

    @Transactional
    public void batchDelete(List<Integer> ids) {
        logger.info("Bắt đầu xóa hàng loạt ProductDetails với IDs: {}", ids);
        for (Integer id : ids) {
            try {
                deleteById(id);
            } catch (Exception e) {
                logger.error("Lỗi khi xóa ProductDetail ID {}: {}", id, e.getMessage());
                throw new RuntimeException("Lỗi khi xóa ProductDetail ID " + id + ": " + e.getMessage());
            }
        }
        logger.info("Xóa hàng loạt thành công cho {} ProductDetails", ids.size());
    }

    @Transactional
    public void batchUpdate(ProductDetailUpdateDto updateDto, List<Integer> ids) {
        logger.info("Bắt đầu cập nhật hàng loạt ProductDetails với IDs: {}", ids);
        for (Integer id : ids) {
            ProductDetail existingDetail = findById(id);
            if (existingDetail != null) {
                if (updateDto.getOriginId() != null) {
                    existingDetail.setOrigin(originRepo.findById(updateDto.getOriginId())
                            .orElseThrow(() -> new RuntimeException("Origin not found")));
                }
                if (updateDto.getMaterialId() != null) {
                    existingDetail.setMaterial(materialRepo.findById(updateDto.getMaterialId())
                            .orElseThrow(() -> new RuntimeException("Material not found")));
                }
                if (updateDto.getStyleId() != null) {
                    existingDetail.setStyle(styleRepo.findById(updateDto.getStyleId())
                            .orElseThrow(() -> new RuntimeException("Style not found")));
                }
                if (updateDto.getPrice() != null) {
                    existingDetail.setPrice(updateDto.getPrice());
                }
                if (updateDto.getStockQuantity() != null) {
                    existingDetail.setStockQuantity(updateDto.getStockQuantity());
                }
                productDetailRepo.save(existingDetail);
            } else {
                logger.warn("Không tìm thấy ProductDetail với ID: {}", id);
            }
        }
        logger.info("Cập nhật hàng loạt thành công cho {} ProductDetails", ids.size());
    }

    @Transactional
    public void updateProductDetail(ProductDetailUpdateDto updateDto, MultipartFile[] newImageFiles) {
        ProductDetail existingDetail = findById(updateDto.getId());
        if (existingDetail != null) {
            existingDetail.setProduct(productRepo.findById(updateDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found")));
            existingDetail.setColor(colorRepo.findById(updateDto.getColorId())
                    .orElseThrow(() -> new RuntimeException("Color not found")));
            existingDetail.setSize(sizeRepo.findById(updateDto.getSizeId())
                    .orElseThrow(() -> new RuntimeException("Size not found")));
            existingDetail.setOrigin(originRepo.findById(updateDto.getOriginId())
                    .orElseThrow(() -> new RuntimeException("Origin not found")));
            existingDetail.setMaterial(materialRepo.findById(updateDto.getMaterialId())
                    .orElseThrow(() -> new RuntimeException("Material not found")));
            existingDetail.setStyle(styleRepo.findById(updateDto.getStyleId())
                    .orElseThrow(() -> new RuntimeException("Style not found")));
            existingDetail.setPrice(updateDto.getPrice());
            existingDetail.setStockQuantity(updateDto.getStockQuantity());

            productDetailRepo.save(existingDetail);

            if (newImageFiles != null && newImageFiles.length > 0) {
                saveImagesForProductDetail(existingDetail, newImageFiles);
            }
        } else {
            throw new RuntimeException("ProductDetail not found with ID: " + updateDto.getId());
        }
    }

    @Transactional
    public void saveProductDetailWithImages(ProductDetail pd, MultipartFile[] imageFiles) {
        ProductDetail savedDetail = productDetailRepo.save(pd);

        try {
            QRCodeUtil.generateQRCodeForProduct(savedDetail.getId());
        } catch (IOException | WriterException e) {
            System.err.println("❌ Không thể tạo QR Code cho sản phẩm ID: " + savedDetail.getId());
            e.printStackTrace();
        }

        saveImagesForProductDetail(savedDetail, imageFiles);
    }

    @Transactional
    public void deleteImage(Integer imageId) {
        ProductImage image = productImageRepo.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        try {
            Path imagePath = Paths.get(UPLOAD_DIR + image.getImageUrl());
            Files.deleteIfExists(imagePath);
            productImageRepo.delete(image);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete image file: " + e.getMessage());
        }
    }

    @Transactional
    public void saveProductDetailVariationsDto(ProductDetailBatchDto batchDto) {
        Product product = productRepo.findById(batchDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found ID: " + batchDto.getProductId()));
        Origin origin = originRepo.findById(batchDto.getOriginId())
                .orElseThrow(() -> new RuntimeException("Origin not found ID: " + batchDto.getOriginId()));
        Material material = materialRepo.findById(batchDto.getMaterialId())
                .orElseThrow(() -> new RuntimeException("Material not found ID: " + batchDto.getMaterialId()));
        Style style = styleRepo.findById(batchDto.getStyleId())
                .orElseThrow(() -> new RuntimeException("Style not found ID: " + batchDto.getStyleId()));

        if (batchDto.getVariations() == null || batchDto.getVariations().isEmpty()) {
            System.out.println("Không có biến thể nào được cung cấp để lưu.");
            return;
        }

        Map<Integer, List<ProductDetailVariationDto>> variationsByColor = batchDto.getVariations().stream()
                .filter(v -> v.getColorId() != null && v.getSizeId() != null && v.getPrice() != null && v.getStockQuantity() != null)
                .collect(Collectors.groupingBy(ProductDetailVariationDto::getColorId));

        for (Map.Entry<Integer, List<ProductDetailVariationDto>> entry : variationsByColor.entrySet()) {
            Integer colorId = entry.getKey();
            List<ProductDetailVariationDto> variations = entry.getValue();

            List<MultipartFile> imageFiles = variations.stream()
                    .filter(v -> v.getImageFiles() != null && !v.getImageFiles().isEmpty())
                    .findFirst()
                    .map(ProductDetailVariationDto::getImageFiles)
                    .orElse(null);

            for (ProductDetailVariationDto variationDto : variations) {
                try {
                    Color color = colorRepo.findById(variationDto.getColorId())
                            .orElseThrow(() -> new RuntimeException("Color not found ID: " + variationDto.getColorId()));
                    Size size = sizeRepo.findById(variationDto.getSizeId())
                            .orElseThrow(() -> new RuntimeException("Size not found ID: " + variationDto.getSizeId()));

                    ProductDetail pd = new ProductDetail();
                    pd.setProduct(product);
                    pd.setOrigin(origin);
                    pd.setMaterial(material);
                    pd.setStyle(style);
                    pd.setColor(color);
                    pd.setSize(size);
                    pd.setPrice(variationDto.getPrice());
                    pd.setStockQuantity(variationDto.getStockQuantity());
                    pd.setCreatedAt(LocalDateTime.now());

                    ProductDetail savedDetail = productDetailRepo.save(pd);

                    try {
                        QRCodeUtil.generateQRCodeForProduct(savedDetail.getId());
                    } catch (IOException | WriterException e) {
                        System.err.println("❌ Không thể tạo QR Code cho biến thể sản phẩm ID: " + savedDetail.getId());
                        e.printStackTrace();
                    }

                    if (imageFiles != null && !imageFiles.isEmpty()) {
                        List<MultipartFile> validFiles = imageFiles.stream()
                                .filter(mf -> mf != null && !mf.isEmpty() && mf.getOriginalFilename() != null && !mf.getOriginalFilename().isEmpty())
                                .collect(Collectors.toList());

                        if (!validFiles.isEmpty()) {
                            saveImagesForProductDetail(savedDetail, validFiles.toArray(new MultipartFile[0]));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi xử lý biến thể: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveImagesForProductDetail(ProductDetail productDetail, MultipartFile[] imageFiles) {
        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty() && file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty()) {
                    try {
                        Path uploadPath = Paths.get(UPLOAD_DIR);
                        if (!Files.exists(uploadPath)) {
                            Files.createDirectories(uploadPath);
                        }
                        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
                        Path filePath = uploadPath.resolve(filename);
                        Files.write(filePath, file.getBytes());

                        ProductImage img = new ProductImage();
                        img.setProductDetail(productDetail);
                        img.setImageUrl(filename);
                        productImageRepo.save(img);
                    } catch (IOException e) {
                        System.err.println("Không thể lưu tệp ảnh: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}