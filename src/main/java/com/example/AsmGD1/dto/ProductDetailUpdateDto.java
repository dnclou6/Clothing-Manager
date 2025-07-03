package com.example.AsmGD1.dto;

import java.math.BigDecimal;

public class ProductDetailUpdateDto {
    private Integer id;
    private Integer productId;
    private Integer colorId;
    private Integer sizeId;
    private Integer originId;
    private Integer materialId;
    private Integer styleId;
    private BigDecimal price; // Sửa từ Double thành BigDecimal
    private Integer stockQuantity;

    // Getters và Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getColorId() { return colorId; }
    public void setColorId(Integer colorId) { this.colorId = colorId; }
    public Integer getSizeId() { return sizeId; }
    public void setSizeId(Integer sizeId) { this.sizeId = sizeId; }
    public Integer getOriginId() { return originId; }
    public void setOriginId(Integer originId) { this.originId = originId; }
    public Integer getMaterialId() { return materialId; }
    public void setMaterialId(Integer materialId) { this.materialId = materialId; }
    public Integer getStyleId() { return styleId; }
    public void setStyleId(Integer styleId) { this.styleId = styleId; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
}