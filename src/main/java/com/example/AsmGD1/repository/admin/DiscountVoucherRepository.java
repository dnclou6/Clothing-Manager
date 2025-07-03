    package com.example.AsmGD1.repository.admin;

    import com.example.AsmGD1.entity.DiscountVoucher;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    import java.util.List;
    import java.util.Optional;

    @Repository
    public interface DiscountVoucherRepository extends JpaRepository<DiscountVoucher, Long> {
        Optional<DiscountVoucher> findByCode(String code);
        List<DiscountVoucher> findAllByDeletedFalse();
        boolean existsByCode(String code);

    }
