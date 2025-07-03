    package com.example.AsmGD1.repository.admin;

    import com.example.AsmGD1.entity.User;
    import com.example.AsmGD1.entity.UserDiscountVoucher;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    import java.util.List;

    @Repository
    public interface UserDiscountVoucherRepository extends JpaRepository<UserDiscountVoucher, Long> {
        List<UserDiscountVoucher> findByUserId(Long userId);
        List<UserDiscountVoucher> findByVoucherId(Long voucherId);
        boolean existsByUserIdAndVoucherId(Long userId, Long voucherId);
    }