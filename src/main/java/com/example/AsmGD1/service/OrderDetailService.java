package com.example.AsmGD1.service;

import com.example.AsmGD1.entity.OrderDetail;
import com.example.AsmGD1.repository.OrderDetailRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderDetailService {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderService orderService;

    // Lấy danh sách chi tiết theo ID hóa đơn
    public List<OrderDetail> findByOrderId(Integer orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }

    // Lấy chi tiết đơn hàng theo ID chi tiết
    public OrderDetail findById(Integer id) {
        return orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết đơn hàng"));
    }

    // Thêm mới hoặc cập nhật chi tiết đơn hàng, tự động cập nhật tổng tiền đơn hàng
    @Transactional
    public void save(OrderDetail detail) {
        orderDetailRepository.save(detail);
        orderService.updateOrderTotal(detail.getOrder().getId());
    }

    // Xóa một chi tiết đơn hàng, cập nhật lại tổng tiền đơn hàng
    @Transactional
    public void delete(Integer id) {
        OrderDetail detail = findById(id);
        Integer orderId = detail.getOrder().getId();
        orderDetailRepository.delete(detail);
        orderService.updateOrderTotal(orderId);
    }

    // Xóa tất cả chi tiết đơn hàng theo orderId
    @Transactional
    public void deleteAllByOrderId(Integer orderId) {
        List<OrderDetail> details = findByOrderId(orderId);
        orderDetailRepository.deleteAll(details);
        orderService.updateOrderTotal(orderId);
    }
}
