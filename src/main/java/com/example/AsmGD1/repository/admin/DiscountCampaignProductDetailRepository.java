package com.example.AsmGD1.repository.admin;

import com.example.AsmGD1.entity.DiscountCampaignProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscountCampaignProductDetailRepository extends JpaRepository<DiscountCampaignProductDetail, Long> {
    List<DiscountCampaignProductDetail> findByCampaignId(Long campaignId);
    void deleteAllByCampaignId(Long campaignId);

}
