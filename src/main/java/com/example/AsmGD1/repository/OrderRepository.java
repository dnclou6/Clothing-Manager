package com.example.AsmGD1.repository;

import com.example.AsmGD1.entity.Order;
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
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Order findByCode(String code);
    long countByStatus(String status);

    List<Order> findByStatus(String status);

    // Phân trang + tìm kiếm
    @Query("SELECT o FROM Order o " +
            "WHERE (:keyword IS NULL OR o.user.fullName LIKE %:keyword%) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:deliveryMethod IS NULL OR o.deliveryMethod = :deliveryMethod) " +
            "AND (:paymentMethod IS NULL OR o.paymentMethod = :paymentMethod) " +
            "AND (:startDate IS NULL OR o.createdAt >= CAST(:startDate AS date)) " +
            "AND (:endDate IS NULL OR o.createdAt <= CAST(:endDate AS date))")
    Page<Order> findWithFiltersPaging(@Param("keyword") String keyword,
                                      @Param("status") String status,
                                      @Param("startDate") String startDate,
                                      @Param("endDate") String endDate,
                                      @Param("deliveryMethod") String deliveryMethod,
                                      @Param("paymentMethod") String paymentMethod,
                                      Pageable pageable);

    // Tìm kiếm không phân trang
    @Query("SELECT o FROM Order o " +
            "WHERE (:keyword IS NULL OR LOWER(o.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(o.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:deliveryMethod IS NULL OR o.deliveryMethod = :deliveryMethod) " +
            "AND (:paymentMethod IS NULL OR o.paymentMethod = :paymentMethod) " +
            "AND (:startDate IS NULL OR o.createdAt >= CAST(:startDate AS date)) " +
            "AND (:endDate IS NULL OR o.createdAt <= CAST(:endDate AS date))")
    List<Order> findWithKeywordAndFilters(@Param("keyword") String keyword,
                                          @Param("status") String status,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate,
                                          @Param("deliveryMethod") String deliveryMethod,
                                          @Param("paymentMethod") String paymentMethod);

    // Lọc cơ bản
    @Query("SELECT o FROM Order o " +
            "WHERE (:status IS NULL OR o.status = :status) " +
            "AND (:deliveryMethod IS NULL OR o.deliveryMethod = :deliveryMethod) " +
            "AND (:paymentMethod IS NULL OR o.paymentMethod = :paymentMethod) " +
            "AND (:startDate IS NULL OR o.createdAt >= CAST(:startDate AS date)) " +
            "AND (:endDate IS NULL OR o.createdAt <= CAST(:endDate AS date))")
    List<Order> findWithFilters(@Param("status") String status,
                                @Param("startDate") String startDate,
                                @Param("endDate") String endDate,
                                @Param("deliveryMethod") String deliveryMethod,
                                @Param("paymentMethod") String paymentMethod);

    // Trạng thái ignore case
    @Query("SELECT o FROM Order o WHERE UPPER(o.status) = UPPER(:status)")
    List<Order> findByStatusIgnoreCase(@Param("status") String status);

    // Doanh thu theo ngày
    @Query(value = "SELECT CONVERT(DATE, created_at) AS day, SUM(total_price) AS totalRevenue " +
            "FROM Orders WHERE status = 'Completed' " +
            "GROUP BY CONVERT(DATE, created_at) ORDER BY day DESC", nativeQuery = true)
    List<Map<String, Object>> getRevenueByDay();

    // Doanh thu theo tháng
    @Query(value = "SELECT YEAR(created_at) AS year, MONTH(created_at) AS month, " +
            "SUM(total_price) AS totalRevenue FROM Orders " +
            "WHERE status = 'Completed' " +
            "GROUP BY YEAR(created_at), MONTH(created_at) " +
            "ORDER BY year DESC, month DESC", nativeQuery = true)
    List<Map<String, Object>> getRevenueByMonth();

    // Doanh thu theo năm
    @Query(value = "SELECT YEAR(created_at) AS year, SUM(total_price) AS totalRevenue " +
            "FROM Orders WHERE status = 'Completed' " +
            "GROUP BY YEAR(created_at) ORDER BY year DESC", nativeQuery = true)
    List<Map<String, Object>> getRevenueByYear();

    // Tổng đơn hoàn thành
    @Query(value = "SELECT COUNT(id) AS totalOrders FROM Orders WHERE status = 'Completed'", nativeQuery = true)
    Long getCompletedOrdersCount();

    // Tổng doanh thu trong khoảng thời gian
    @Query(value = "SELECT SUM(total_price) FROM Orders " +
            "WHERE status = 'Completed' AND CAST(created_at AS DATE) BETWEEN :fromDate AND :toDate", nativeQuery = true)
    Double getRevenueBetween(@Param("fromDate") LocalDate fromDate,
                             @Param("toDate") LocalDate toDate);

    // Tổng sản phẩm bán ra trong khoảng
    @Query(value = "SELECT SUM(od.quantity) FROM Order_Details od " +
            "JOIN Orders o ON od.order_id = o.id " +
            "WHERE o.status = 'Completed' AND CAST(o.created_at AS DATE) BETWEEN :fromDate AND :toDate", nativeQuery = true)
    Integer getSoldQuantityBetween(@Param("fromDate") LocalDate fromDate,
                                   @Param("toDate") LocalDate toDate);

    // Trạng thái đơn hàng theo ngày
    @Query(value = "SELECT status, COUNT(*) FROM Orders " +
            "WHERE CAST(created_at AS DATE) BETWEEN :fromDate AND :toDate " +
            "GROUP BY status", nativeQuery = true)
    List<Object[]> getOrderStatusCountsBetween(@Param("fromDate") LocalDate fromDate,
                                               @Param("toDate") LocalDate toDate);

    // Đếm số đơn hoàn thành theo khoảng
    @Query(value = "SELECT COUNT(*) FROM Orders " +
            "WHERE status = 'Completed' AND CAST(created_at AS DATE) BETWEEN :fromDate AND :toDate", nativeQuery = true)
    Long countCompletedOrdersBetween(@Param("fromDate") LocalDate fromDate,
                                     @Param("toDate") LocalDate toDate);

    // Dữ liệu biểu đồ: ngày - số hóa đơn - số sản phẩm
    @Query(value = """
        SELECT CAST(o.created_at AS DATE) AS day,
               COUNT(DISTINCT o.id) AS order_count,
               SUM(od.quantity) AS product_count
        FROM Orders o
        JOIN Order_Details od ON o.id = od.order_id
        WHERE o.status = 'Completed'
          AND CAST(o.created_at AS DATE) BETWEEN :fromDate AND :toDate
        GROUP BY CAST(o.created_at AS DATE)
        ORDER BY day ASC
        """, nativeQuery = true)
    List<Map<String, Object>> getOrderProductStatsBetween(@Param("fromDate") LocalDate fromDate,
                                                          @Param("toDate") LocalDate toDate);

    @Query("SELECT o FROM Order o " +
            "WHERE (:keyword IS NULL OR LOWER(o.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(o.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:deliveryMethod IS NULL OR o.deliveryMethod = :deliveryMethod) " +
            "AND (:paymentMethod IS NULL OR o.paymentMethod = :paymentMethod) " +
            "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate)")
    List<Order> searchOrdersWithFilters(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("deliveryMethod") String deliveryMethod,
            @Param("paymentMethod") String paymentMethod
    );


}
