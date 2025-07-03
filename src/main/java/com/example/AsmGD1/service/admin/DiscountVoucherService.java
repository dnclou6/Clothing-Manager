package com.example.AsmGD1.service.admin;

import com.example.AsmGD1.entity.DiscountVoucher;
import com.example.AsmGD1.repository.admin.DiscountVoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiscountVoucherService {
    @Autowired
    private DiscountVoucherRepository repository;

    public List<DiscountVoucher> getAllVouchers() {
        return repository.findAllByDeletedFalse(); // ✅ đúng với field mới là "deleted"
    }


    public DiscountVoucher getById(Long id) {
        return repository.findById(id).orElseThrow();
    }

    public DiscountVoucher save(DiscountVoucher voucher) {
        return repository.save(voucher);
    }

    public void delete(Long id) {
        DiscountVoucher voucher = getById(id);
        voucher.setDeleted(true); // đúng với tên method mới trong entity
        repository.save(voucher);
    }

    public String getStatus(DiscountVoucher v) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(v.getStartDate())) return "Sắp diễn ra";
        else if (now.isAfter(v.getEndDate())) return "Đã kết thúc";
        else return "Đang diễn ra";
    }

    // ✅ DÒNG NÀY BẠN CẦN THÊM
    public boolean existsByCode(String code) {
        return repository.existsByCode(code);
    }
}