package com.example.AsmGD1.repository;

import com.example.AsmGD1.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    @Query("SELECT od FROM OrderDetail od JOIN FETCH od.productDetail pd WHERE od.order.id = :orderId")
    List<OrderDetail> findByOrderId(@Param("orderId") Integer orderId);

    @Query(value = """
        SELECT pd.id AS product_detail_id,
               p.name AS product_name,
               c.name AS color,
               s.name AS size,
               pd.price,
               SUM(od.quantity) AS total_sold,
               (SELECT TOP 1 pi.image_url 
                FROM Product_Images pi 
                WHERE pi.product_detail_id = pd.id 
                ORDER BY pi.id ASC) AS image_url
        FROM Order_Details od
        JOIN Product_Details pd ON od.product_detail_id = pd.id
        JOIN Products p ON pd.product_id = p.id
        LEFT JOIN Colors c ON pd.color_id = c.id
        LEFT JOIN Sizes s ON pd.size_id = s.id
        JOIN Orders o ON od.order_id = o.id
        WHERE o.status = 'Completed' 
          AND CAST(o.created_at AS DATE) BETWEEN :fromDate AND :toDate
        GROUP BY pd.id, p.name, c.name, s.name, pd.price
        ORDER BY total_sold DESC
        OFFSET 0 ROWS FETCH NEXT 5 ROWS ONLY
    """, nativeQuery = true)
    List<Object[]> findTopSellingProductDetailsRaw(@Param("fromDate") LocalDate fromDate,
                                                   @Param("toDate") LocalDate toDate);

    @Query(value = """
        SELECT pd.id AS product_detail_id,
               p.name AS product_name,
               c.name AS color,
               s.name AS size,
               pd.price,
               pd.stock_quantity,
               (SELECT TOP 1 pi.image_url 
                FROM Product_Images pi 
                WHERE pi.product_detail_id = pd.id 
                ORDER BY pi.id ASC) AS image_url
        FROM Product_Details pd
        JOIN Products p ON pd.product_id = p.id
        LEFT JOIN Colors c ON pd.color_id = c.id
        LEFT JOIN Sizes s ON pd.size_id = s.id
        WHERE pd.stock_quantity <= 10
        ORDER BY pd.stock_quantity ASC
        OFFSET 0 ROWS FETCH NEXT 5 ROWS ONLY
    """, nativeQuery = true)
    List<Object[]> findLowStockProductDetailsRaw();
}