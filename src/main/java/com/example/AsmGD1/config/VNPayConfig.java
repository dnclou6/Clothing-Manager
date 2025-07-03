package com.example.AsmGD1.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {
    public static final String VNP_TMNCODE = "V1D0GND4"; // Thay bằng vnp_TmnCode của bạn
    public static final String VNP_HASHSECRET = "OKEXPGAT7T48NZT7LR22OGKMLFYUYRKB"; // Thay bằng vnp_HashSecret của bạn
    public static final String VNP_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"; // URL thanh toán sandbox (dùng cho test)
    public static final String VNP_RETURN_URL = "http://localhost:8080/acvstore/banhang/vnpay-return"; // URL callback sau khi thanh toán
    public static final String VNP_API_URL = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction"; // API để kiểm tra trạng thái giao dịch (nếu cần)
}
