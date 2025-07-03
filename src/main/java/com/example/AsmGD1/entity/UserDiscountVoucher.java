package com.example.AsmGD1.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "UserDiscountVoucher")
public class UserDiscountVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private DiscountVoucher voucher;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public UserDiscountVoucher() {}

    public UserDiscountVoucher(User user, DiscountVoucher voucher) {
        this.user = user;
        this.voucher = voucher;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DiscountVoucher getVoucher() {
        return voucher;
    }

    public void setVoucher(DiscountVoucher voucher) {
        this.voucher = voucher;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}