package com.example.AsmGD1.repository.product;

import com.example.AsmGD1.entity.product.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProductDetailId(Integer productDetailId);
}

