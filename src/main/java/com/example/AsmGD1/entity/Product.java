package com.example.AsmGD1.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 2, max = 255, message = "Tên sản phẩm phải từ 2 đến 255 ký tự")
    private String name;

    @Size(max = 1000, message = "Mô tả sản phẩm không được vượt quá 1000 ký tự")
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

    @Column(name = "image_url")
    private String imageUrl;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date createdAt = new Date();

    @Transient
    private int totalStockQuantity;

    @Transient
    private BigDecimal minPrice;

    @Transient
    private BigDecimal maxPrice;

    @Transient
    private String minPriceFormatted;

    @Transient
    private String maxPriceFormatted;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductDetail> productDetails;

    public Product() {}

    public Product(Integer id, String name, String description, Category category, String imageUrl, Date createdAt, int totalStockQuantity, BigDecimal minPrice, BigDecimal maxPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.totalStockQuantity = totalStockQuantity;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getTotalStockQuantity() {
        return totalStockQuantity;
    }

    public void setTotalStockQuantity(int totalStockQuantity) {
        this.totalStockQuantity = totalStockQuantity;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getMinPriceFormatted() {
        return minPriceFormatted;
    }

    public void setMinPriceFormatted(String minPriceFormatted) {
        this.minPriceFormatted = minPriceFormatted;
    }

    public String getMaxPriceFormatted() {
        return maxPriceFormatted;
    }

    public void setMaxPriceFormatted(String maxPriceFormatted) {
        this.maxPriceFormatted = maxPriceFormatted;
    }
}