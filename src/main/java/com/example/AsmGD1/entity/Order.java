package com.example.AsmGD1.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String code; // Mã hóa đơn (HD001...)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;


    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "shipping_fee")
    private BigDecimal shippingFee;

    @Column(name = "delivery_method") // 'Tại quầy', 'Giao hàng'
    private String deliveryMethod;

    @Column(name = "payment_method") // 'Tiền mặt', 'Chuyển khoản'
    private String paymentMethod;

    @Column(nullable = false)
    private String status; // pending, delivering, completed, cancelled

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    @Transient // Chỉ để tính toán trong bộ nhớ, không lưu vào cơ sở dữ liệu
    private String formattedTotal;


    @Transient
    private String formattedShippingFee;

    // Custom getter cho hiển thị status tiếng Việt (nếu cần dùng trong view)
    @Transient
    public String getStatusDisplay() {
        return switch (status) {
            case "Pending" -> "Chờ xác nhận";
            case "Delivering" -> "Đang giao";
            case "Completed" -> "Hoàn thành";
            case "Cancelled" -> "Đã hủy";
            default -> "Không xác định";
        };
    }


    // Optional: format code nếu bạn không muốn nhập thủ công
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Order() {
    }

    public Order(Integer id, String code, User user, BigDecimal totalPrice, BigDecimal shippingFee, String deliveryMethod, String paymentMethod, String status, LocalDateTime createdAt, List<OrderDetail> orderDetails, String formattedTotal, String formattedShippingFee) {
        this.id = id;
        this.code = code;
        this.user = user;
        this.totalPrice = totalPrice;
        this.shippingFee = shippingFee;
        this.deliveryMethod = deliveryMethod;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.createdAt = createdAt;
        this.orderDetails = orderDetails;
        this.formattedTotal = formattedTotal;
        this.formattedShippingFee = formattedShippingFee;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public String getFormattedTotal() {
        return formattedTotal;
    }

    public void setFormattedTotal(String formattedTotal) {
        this.formattedTotal = formattedTotal;
    }


    public String getFormattedShippingFee() {
        if (shippingFee == null || shippingFee.compareTo(BigDecimal.ZERO) == 0) {
            return "Miễn phí";
        }
        return String.format("%,.0f đ", shippingFee);
    }

    public void setFormattedShippingFee(String formattedShippingFee) {
        this.formattedShippingFee = formattedShippingFee;
    }

}
