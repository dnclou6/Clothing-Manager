package com.example.AsmGD1.service;

import com.example.AsmGD1.entity.Product;
import com.example.AsmGD1.entity.ProductDetail;
import com.example.AsmGD1.repository.OrderDetailRepository;
import com.example.AsmGD1.repository.admin.DiscountCampaignProductDetailRepository;
import com.example.AsmGD1.repository.admin.ProductDetailRepository;
import com.example.AsmGD1.repository.admin.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerHomepageService {
    private final ProductRepository productRepo;
    private final ProductDetailRepository productDetailRepo;
    private final OrderDetailRepository orderDetailRepo;
    private final DiscountCampaignProductDetailRepository discountCampaignDetailRepo;

    public List<Product> getNewestProducts() {
        return productRepo.findTop10ByOrderByCreatedAtDesc();
    }

    public List<ProductDetail> getFlashSaleProducts() {
        return productDetailRepo.findFlashSaleProductDetails();
    }

    public List<Product> getBestSellingProducts() {
        return productRepo.findBestSellingProducts();
    }
}

