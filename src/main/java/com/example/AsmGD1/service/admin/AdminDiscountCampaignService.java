package com.example.AsmGD1.service.admin;

import com.example.AsmGD1.entity.DiscountCampaign;
import com.example.AsmGD1.entity.DiscountCampaignProductDetail;
import com.example.AsmGD1.entity.Product;
import com.example.AsmGD1.entity.ProductDetail;
import com.example.AsmGD1.repository.admin.DiscountCampaignProductDetailRepository;
import com.example.AsmGD1.repository.admin.DiscountCampaignRepository;
import com.example.AsmGD1.repository.admin.ProductDetailRepository;
import com.example.AsmGD1.repository.admin.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDiscountCampaignService {

    @Autowired
    private DiscountCampaignRepository campaignRepository;
    @Autowired
    private ProductDetailRepository productDetailRepository;
    @Autowired
    private DiscountCampaignProductDetailRepository campaignDetailRepository;
    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public DiscountCampaign createDiscountCampaign(DiscountCampaign campaign, List<Integer> productDetailIds) {
        // Gán thời gian tạo
        campaign.setCreatedAt(new Date());
        campaign.setDeleted(false);

        // Lưu campaign trước
        DiscountCampaign savedCampaign = campaignRepository.save(campaign);

        // Lưu các bản ghi chi tiết gắn với campaign
        List<DiscountCampaignProductDetail> detailList = new ArrayList<>();
        for (Integer pdId : productDetailIds) {
            ProductDetail productDetail = productDetailRepository.findById(pdId).orElse(null);
            if (productDetail != null) {
                DiscountCampaignProductDetail detail = new DiscountCampaignProductDetail();
                detail.setCampaign(savedCampaign);
                detail.setProductDetail(productDetail);
                detailList.add(detail);
            }
        }
        campaignDetailRepository.saveAll(detailList);
        return savedCampaign;
    }

    public List<ProductDetail> findProductDetailsByProductId(Integer productId) {
        return productDetailRepository.findByProductId(productId);
    }

    public List<DiscountCampaign> getAllCampaigns() {
        return campaignRepository.findAll();
    }
    public List<ProductDetail> findByProductIds(List<Integer> ids) {
        return productDetailRepository.findByProductIdIn(ids);
    }

    public Page<Product> getPaginatedProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    public DiscountCampaign findById(Long id) {
        return campaignRepository.findById(id).orElse(null);
    }
    @Transactional
    public void updateDiscountCampaign(DiscountCampaign campaign, List<Integer> productDetailIds) {
        DiscountCampaign existing = campaignRepository.findById(campaign.getId()).orElseThrow();
        existing.setName(campaign.getName());
        existing.setCode(campaign.getCode());
        existing.setStartDate(campaign.getStartDate());
        existing.setEndDate(campaign.getEndDate());
        existing.setQuantity(campaign.getQuantity());
        existing.setDiscountPercent(campaign.getDiscountPercent());

        campaignRepository.save(existing);

        // Xóa chi tiết cũ
        campaignDetailRepository.deleteAllByCampaignId(existing.getId());

        // Lưu lại chi tiết mới
        List<DiscountCampaignProductDetail> newDetails = productDetailIds.stream().map(pid -> {
            ProductDetail detail = productDetailRepository.findById(pid).orElseThrow();
            return new DiscountCampaignProductDetail(null, existing, detail);
        }).toList();
        campaignDetailRepository.saveAll(newDetails);
    }
    public List<ProductDetail> findSelectedDetailsByCampaignId(Long campaignId) {
        return campaignDetailRepository
                .findByCampaignId(campaignId)
                .stream()
                .map(DiscountCampaignProductDetail::getProductDetail)
                .collect(Collectors.toList());
    }
}
