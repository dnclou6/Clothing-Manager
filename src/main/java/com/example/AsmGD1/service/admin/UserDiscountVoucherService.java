package com.example.AsmGD1.service.admin;

import com.example.AsmGD1.entity.DiscountVoucher;
import com.example.AsmGD1.entity.User;
import com.example.AsmGD1.entity.UserDiscountVoucher;
import com.example.AsmGD1.repository.UserRepository;
import com.example.AsmGD1.repository.admin.UserDiscountVoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDiscountVoucherService {

    @Autowired private UserRepository userRepository;
    @Autowired private DiscountVoucherService discountVoucherService;
    @Autowired private UserDiscountVoucherRepository userDiscountVoucherRepository;

    public List<User> getAllCustomers() {
        return userRepository.findAll()
                .stream()
                .filter(u -> "CUSTOMER".equals(u.getRole()) && !Boolean.TRUE.equals(u.getIsDeleted()))
                .collect(Collectors.toList());
    }

    public List<User> getUsersByIds(List<Long> ids) {
        List<Integer> intIds = ids.stream().map(Long::intValue).collect(Collectors.toList());
        return userRepository.findAllById(intIds);
    }

    public void assignVoucherToUser(User user, DiscountVoucher voucher) {
        Long userId = user.getId().longValue();
        Long voucherId = voucher.getId();

        if (!userDiscountVoucherRepository.existsByUserIdAndVoucherId(userId, voucherId)) {
            UserDiscountVoucher entity = new UserDiscountVoucher(user, voucher);
            userDiscountVoucherRepository.save(entity);
        }
    }
}
