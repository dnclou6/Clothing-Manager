package com.example.AsmGD1.repository.admin;

import com.example.AsmGD1.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail, Integer> {

    // Tìm tất cả ProductDetail theo productId
    List<ProductDetail> findByProductId(Integer productId);

    // Tìm tất cả ProductDetail theo danh sách productIds
    List<ProductDetail> findByProductIdIn(List<Integer> productIds);

    // Tìm ProductDetail theo productId, colorId và sizeId
    @Query("SELECT pd FROM ProductDetail pd " +
            "WHERE pd.product.id = :productId " +
            "AND pd.color.id = :colorId " +
            "AND pd.size.id = :sizeId")
    ProductDetail findByProductIdAndColorIdAndSizeId(
            @Param("productId") Integer productId,
            @Param("colorId") Integer colorId,
            @Param("sizeId") Integer sizeId);

    // Tìm ProductDetail theo productId và stockQuantity lớn hơn 0
    @Query("SELECT pd FROM ProductDetail pd " +
            "WHERE pd.product.id = :productId " +
            "AND pd.stockQuantity > 0")
    List<ProductDetail> findByProductIdAndStockQuantityGreaterThanZero(
            @Param("productId") Integer productId);

    // Tìm tất cả ProductDetail có stockQuantity lớn hơn 0
    List<ProductDetail> findByStockQuantityGreaterThan(Integer stockQuantity);

    // Tìm ProductDetail theo tên sản phẩm (không phân biệt hoa thường)
    @Query("SELECT pd FROM ProductDetail pd " +
            "WHERE LOWER(pd.product.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<ProductDetail> findByProductNameContainingIgnoreCase(@Param("name") String name);

    // Tìm ProductDetail theo danh mục (categoryId)
    @Query("SELECT pd FROM ProductDetail pd " +
            "WHERE pd.product.category.id = :categoryId")
    List<ProductDetail> findByCategoryId(@Param("categoryId") Integer categoryId);


    @Query("SELECT pd FROM DiscountCampaignProductDetail dcpd JOIN dcpd.productDetail pd " +
            "WHERE dcpd.campaign.startDate <= CURRENT_TIMESTAMP AND dcpd.campaign.endDate >= CURRENT_TIMESTAMP")
    List<ProductDetail> findFlashSaleProductDetails();

}