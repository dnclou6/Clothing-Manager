package com.example.AsmGD1.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "discount_voucher")
public class DiscountVoucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(name = "discount_value")
    private BigDecimal discountValue;

    @Column(name = "max_discount_value")
    private BigDecimal maxDiscountValue;

    @Column(name = "min_order_value")
    private BigDecimal minOrderValue;

    private int quantity;

    @Column(name = "usage_count")
    private int usageCount;

    @Column(name = "is_public")
    private boolean isPublic;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_deleted")
    private Boolean deleted = false; // ✅ Đổi tên field để Spring JPA hiểu đúng

    public DiscountVoucher() {
    }

    public DiscountVoucher(Long id, String code, String name, String type, BigDecimal discountValue,
                           BigDecimal maxDiscountValue, BigDecimal minOrderValue, int quantity,
                           int usageCount, boolean isPublic, LocalDateTime startDate,
                           LocalDateTime endDate, LocalDateTime createdAt, Boolean deleted) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.type = type;
        this.discountValue = discountValue;
        this.maxDiscountValue = maxDiscountValue;
        this.minOrderValue = minOrderValue;
        this.quantity = quantity;
        this.usageCount = usageCount;
        this.isPublic = isPublic;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.deleted = deleted;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getMaxDiscountValue() {
        return maxDiscountValue;
    }

    public void setMaxDiscountValue(BigDecimal maxDiscountValue) {
        this.maxDiscountValue = maxDiscountValue;
    }

    public BigDecimal getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(BigDecimal minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getDeleted() { // ✅ getter chuẩn tên field
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
    @Transient
    private String discountValueFormatted;

    @Transient
    private String maxDiscountValueFormatted;

    @Transient
    private String minOrderValueFormatted;

    public String getDiscountValueFormatted() {
        return discountValueFormatted;
    }

    public void setDiscountValueFormatted(String discountValueFormatted) {
        this.discountValueFormatted = discountValueFormatted;
    }

    public String getMaxDiscountValueFormatted() {
        return maxDiscountValueFormatted;
    }

    public void setMaxDiscountValueFormatted(String maxDiscountValueFormatted) {
        this.maxDiscountValueFormatted = maxDiscountValueFormatted;
    }

    public String getMinOrderValueFormatted() {
        return minOrderValueFormatted;
    }

    public void setMinOrderValueFormatted(String minOrderValueFormatted) {
        this.minOrderValueFormatted = minOrderValueFormatted;
    }

}
