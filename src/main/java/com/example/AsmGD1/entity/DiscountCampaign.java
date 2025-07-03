package com.example.AsmGD1.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Table(name = "discount_campaigns")
public class DiscountCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "discount_percent", nullable = false)
    private Double discountPercent;

    private Integer quantity;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt = new Date();

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Transient
    private String status;

    public String getStatus() {
        if (status == null) {
            Date now = new Date();
            if (startDate != null && endDate != null) {
                if (now.before(startDate)) status = "UPCOMING";
                else if (now.after(endDate)) status = "ENDED";
                else status = "ONGOING";
            } else {
                status = "";
            }
        }
        return status;
    }

    public DiscountCampaign() {
    }

    public DiscountCampaign(Long id, String code, String name, Double discountPercent, Integer quantity, Date startDate, Date endDate, Date createdAt, Boolean isDeleted, String status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.discountPercent = discountPercent;
        this.quantity = quantity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
        this.status = status;
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

    public Double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
