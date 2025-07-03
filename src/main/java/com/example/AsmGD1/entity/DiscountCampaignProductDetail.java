package com.example.AsmGD1.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "discount_campaign_product_details")
public class DiscountCampaignProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "campaign_id")
    private DiscountCampaign campaign;

    @ManyToOne
    @JoinColumn(name = "product_detail_id")
    private ProductDetail productDetail;

    public DiscountCampaignProductDetail() {
    }

    public DiscountCampaignProductDetail(Long id, DiscountCampaign campaign, ProductDetail productDetail) {
        this.id = id;
        this.campaign = campaign;
        this.productDetail = productDetail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DiscountCampaign getCampaign() {
        return campaign;
    }

    public void setCampaign(DiscountCampaign campaign) {
        this.campaign = campaign;
    }

    public ProductDetail getProductDetail() {
        return productDetail;
    }

    public void setProductDetail(ProductDetail productDetail) {
        this.productDetail = productDetail;
    }
}
