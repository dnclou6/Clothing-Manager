package com.example.AsmGD1.repository.admin;

import com.example.AsmGD1.entity.Product;
import com.example.AsmGD1.entity.ProductDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query(value = "SELECT p.id, p.name, SUM(pd.stock_quantity) AS stockQuantity " +
            "FROM products p " +
            "LEFT JOIN product_details pd ON p.id = pd.product_id " +
            "GROUP BY p.id, p.name " +
            "HAVING SUM(pd.stock_quantity) > 0 " +
            "ORDER BY SUM(pd.stock_quantity) DESC",
            nativeQuery = true)
    List<Map<String, Object>> getProductsInStock();

    List<Product> findByNameContainingIgnoreCase(String name);
    @Query(value = "SELECT TOP 5 pd.* FROM Product_Details pd JOIN Order_Details od ON pd.id = od.product_detail_id JOIN Orders o ON od.order_id = o.id WHERE CAST(o.created_at AS DATE) BETWEEN :fromDate AND :toDate GROUP BY pd.id, pd.product_id, pd.origin_id, pd.color_id, pd.size_id, pd.material_id, pd.style_id, pd.price, pd.stock_quantity, pd.created_at ORDER BY SUM(od.quantity) DESC", nativeQuery = true)
    List<ProductDetail> findTopSellingProductsBetween(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "SELECT TOP 5 * FROM Product_Details WHERE stock_quantity <= 10 ORDER BY stock_quantity ASC", nativeQuery = true)
    List<ProductDetail> findLowStockProducts();


    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByCategoryId(Integer categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchName, '%')) AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    Page<Product> findByNameContainingIgnoreCaseAndCategoryId(String searchName, Integer categoryId, Pageable pageable);


    List<Product> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT p FROM OrderDetail od " +
            "JOIN od.productDetail pd " +
            "JOIN pd.product p " +
            "GROUP BY p.id, p.name, p.description, p.imageUrl, p.category.id, p.createdAt " +
            "ORDER BY SUM(od.quantity) DESC")
    List<Product> findBestSellingProducts();


}