package com.example.AsmGD1.service.admin;

import com.example.AsmGD1.entity.DiscountCampaign;
import com.example.AsmGD1.repository.admin.DiscountCampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class DiscountCampaignService {
    @Autowired
    private DiscountCampaignRepository discountCampaignRepository;

    public Page<DiscountCampaign> getFilteredCampaigns(String keyword, Date startDate, Date endDate,
                                                       String status, String discountLevel, Pageable pageable) {
        System.out.println("🔥 GỌI filterCampaigns TRONG Service với filter:");
        System.out.println("Keyword: " + keyword);
        System.out.println("Start date: " + startDate);
        System.out.println("End date: " + endDate);
        System.out.println("Status: " + status);
        System.out.println("Discount level: " + discountLevel);

        return discountCampaignRepository.filterCampaigns(keyword, startDate, endDate, status, discountLevel, pageable);
    }

    public DiscountCampaign getById(Long id) {
        return discountCampaignRepository.findById(id).orElseThrow(() -> new RuntimeException("Campaign not found"));
    }
}
