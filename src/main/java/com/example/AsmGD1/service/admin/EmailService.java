package com.example.AsmGD1.service.admin;

import com.example.AsmGD1.entity.DiscountVoucher;
import com.example.AsmGD1.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVoucherToMany(List<User> userDiscountVouchers, DiscountVoucher voucher) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Lấy danh sách email người nhận
        String[] recipients = userDiscountVouchers.stream().map(User::getEmail).distinct().toArray(String[]::new);

        helper.setTo(recipients);
        helper.setSubject("🎁 Ưu đãi đặc biệt dành cho bạn");

        String discountText = voucher.getType().equals("CASH")
                ? String.format("%,.0f VNĐ", voucher.getDiscountValue())
                : voucher.getDiscountValue() + "%";

        String content = String.format("""
                <h3>Xin chào quý khách,</h3>
                <p>Chúng tôi gửi đến bạn mã giảm giá <strong>%s</strong>!</p>
                <p>Chương trình: <b>%s</b></p>
                <p>Giảm: <b>%s</b></p>
                <p>Thời gian áp dụng: <b>%s</b> đến <b>%s</b></p>
                <br><p>💚 Cảm ơn bạn đã đồng hành!</p>
            """,
                voucher.getCode(),
                voucher.getName(),
                discountText,
                voucher.getStartDate(),
                voucher.getEndDate()
        );

        helper.setText(content, true);
        mailSender.send(message);
    }
}