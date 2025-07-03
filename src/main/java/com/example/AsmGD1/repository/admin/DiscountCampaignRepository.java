package com.example.AsmGD1.repository.admin;

import com.example.AsmGD1.entity.DiscountCampaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface DiscountCampaignRepository extends JpaRepository<DiscountCampaign, Long>, JpaSpecificationExecutor<DiscountCampaign> {

    default Page<DiscountCampaign> filterCampaigns(String keyword, Date startDate, Date endDate,
                                                   String status, String discountLevel, Pageable pageable) {
        return findAll(DiscountCampaignSpecification.buildFilter(keyword, startDate, endDate, status, discountLevel), pageable);
    }
}
