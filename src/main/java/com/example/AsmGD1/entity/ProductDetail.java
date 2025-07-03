package com.example.AsmGD1.entity;

import com.example.AsmGD1.entity.product.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Product_Details")
public class ProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "productDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @ManyToOne @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne @JoinColumn(name = "origin_id")
    private Origin origin;

    @ManyToOne @JoinColumn(name = "color_id")
    private Color color;

    @ManyToOne @JoinColumn(name = "size_id")
    private Size size;

    @ManyToOne @JoinColumn(name = "material_id")
    private Material material;

    @ManyToOne @JoinColumn(name = "style_id")
    private Style style;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "productDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails;


    public String getStockStatus() {
        if (stockQuantity > 10) return "Còn hàng";
        else if (stockQuantity > 0) return "Sắp hết hàng";
        else return "Hết hàng";
    }

    // Getter & Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public List<ProductImage> getImages() { return images; }
    public void setImages(List<ProductImage> images) { this.images = images; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Origin getOrigin() { return origin; }
    public void setOrigin(Origin origin) { this.origin = origin; }

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    public Size getSize() { return size; }
    public void setSize(Size size) { this.size = size; }

    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }

    public Style getStyle() { return style; }
    public void setStyle(Style style) { this.style = style; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
