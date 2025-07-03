package com.example.AsmGD1.service;

import com.example.AsmGD1.entity.Order;
import com.example.AsmGD1.entity.OrderDetail;
import com.example.AsmGD1.entity.ProductDetail;
import com.example.AsmGD1.repository.OrderDetailRepository;
import com.example.AsmGD1.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    public Order findByCode(String code) {
        return orderRepository.findByCode(code); // Assuming you have an OrderRepository
    }
    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<Order> findByStatus(String status) {
        return orderRepository.findByStatusIgnoreCase(status);
    }

    public Order findById(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
    }

    public void deleteOrder(Integer id) {
        orderRepository.deleteById(id);
    }

    public BigDecimal calculateTotalPriceByOrderId(Integer orderId) {
        List<OrderDetail> details = orderDetailRepository.findByOrderId(orderId);
        BigDecimal total = BigDecimal.ZERO;
        for (OrderDetail d : details) {
            total = total.add(d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())));
        }
        return total;
    }

    public void updateOrderTotal(Integer orderId) {
        BigDecimal total = calculateTotalPriceByOrderId(orderId);
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setTotalPrice(total);
            orderRepository.save(order);
        }
    }

    // ✅ FIX: Convert String to LocalDate before querying
    public List<Order> filterOrders(String status, String startDateStr, String endDateStr,
                                    String deliveryMethod, String paymentMethod) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        return orderRepository.findWithFilters(status, startDate.toString(), endDate.toString(), deliveryMethod, paymentMethod);
    }

    public List<Order> searchOrders(String keyword, String status, String startDateStr, String endDateStr,
                                    String deliveryMethod, String paymentMethod) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        return orderRepository.findWithKeywordAndFilters(keyword, status, startDate.toString(), endDate.toString(), deliveryMethod, paymentMethod);
    }

    public static String normalizeUnicode(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFC);
        return normalized.trim();
    }

    public long countAllOrders() {
        return orderRepository.count();
    }

    public long countByStatus(String status) {
        return orderRepository.countByStatus(status);
    }

    public List<Map<String, Object>> getRevenueByDay() {
        return orderRepository.getRevenueByDay();
    }

    public Page<Order> searchOrdersWithPagination(String keyword, String status, String startDate,
                                                  String endDate, String deliveryMethod, String paymentMethod,
                                                  Pageable pageable) {
        return orderRepository.findWithFiltersPaging(keyword, status, startDate, endDate, deliveryMethod, paymentMethod, pageable);
    }

    public List<Order> getAllCompletedOrders() {
        return orderRepository.findByStatus("Completed");
    }

    public OrderDetail createCartItem(ProductDetail pd, int quantity) {
        OrderDetail od = new OrderDetail();
        od.setProductDetail(pd);
        od.setQuantity(quantity);
        od.setPrice(pd.getPrice());
        od.setProductName(pd.getProduct().getName()); // Optional
        return od;
    }

    public String formatCurrency(BigDecimal amount) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + " VNĐ";
    }
}
