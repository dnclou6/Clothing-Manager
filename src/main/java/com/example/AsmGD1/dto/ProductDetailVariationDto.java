package com.example.AsmGD1.dto; // Thay đổi package nếu cần

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductDetailVariationDto {
    private Integer colorId;
    private Integer sizeId;
    private BigDecimal price;
    private Integer stockQuantity;
    // Tên trường này khớp với tên input trong HTML sau khi sửa
    private List<MultipartFile> imageFiles;

    public ProductDetailVariationDto() {
    }

    public ProductDetailVariationDto(Integer colorId, Integer sizeId, BigDecimal price, Integer stockQuantity, List<MultipartFile> imageFiles) {
        this.colorId = colorId;
        this.sizeId = sizeId;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageFiles = imageFiles;
    }

    public Integer getColorId() {
        return colorId;
    }

    public void setColorId(Integer colorId) {
        this.colorId = colorId;
    }

    public Integer getSizeId() {
        return sizeId;
    }

    public void setSizeId(Integer sizeId) {
        this.sizeId = sizeId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public List<MultipartFile> getImageFiles() {
        return imageFiles;
    }

    public void setImageFiles(List<MultipartFile> imageFiles) {
        this.imageFiles = imageFiles;
    }
}