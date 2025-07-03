package com.example.AsmGD1.dto; // Thay đổi package nếu cần

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductDetailBatchDto {
    private Integer productId;
    private Integer originId;
    private Integer materialId;
    private Integer styleId;
    // Tên trường 'variations' khớp với tiền tố trong HTML name
    private List<ProductDetailVariationDto> variations;

    public ProductDetailBatchDto() {
    }

    public ProductDetailBatchDto(Integer productId, Integer originId, Integer materialId, Integer styleId, List<ProductDetailVariationDto> variations) {
        this.productId = productId;
        this.originId = originId;
        this.materialId = materialId;
        this.styleId = styleId;
        this.variations = variations;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getOriginId() {
        return originId;
    }

    public void setOriginId(Integer originId) {
        this.originId = originId;
    }

    public Integer getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Integer materialId) {
        this.materialId = materialId;
    }

    public Integer getStyleId() {
        return styleId;
    }

    public void setStyleId(Integer styleId) {
        this.styleId = styleId;
    }

    public List<ProductDetailVariationDto> getVariations() {
        return variations;
    }

    public void setVariations(List<ProductDetailVariationDto> variations) {
        this.variations = variations;
    }
}