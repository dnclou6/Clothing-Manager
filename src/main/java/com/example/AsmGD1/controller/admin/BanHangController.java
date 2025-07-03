package com.example.AsmGD1.controller.admin;

import com.example.AsmGD1.config.VNPayConfig;
import com.example.AsmGD1.dto.HeldOrderDetailDTO;
import com.example.AsmGD1.entity.*;
import com.example.AsmGD1.service.OrderService;
import com.example.AsmGD1.service.UserService;
import com.example.AsmGD1.service.admin.DiscountCampaignService;
import com.example.AsmGD1.service.admin.DiscountVoucherService;
import com.example.AsmGD1.service.admin.ProductService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;

import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/acvstore/banhang")
public class BanHangController {

    private static final Logger log = LoggerFactory.getLogger(BanHangController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private DiscountVoucherService discountVoucherService;

    @Autowired
    private DiscountCampaignService discountCampaignService;


    @GetMapping
    public String showBanHangPage(Model model, HttpSession session) {
        log.info("Accessing BanHang page");

        List<ProductDetail> productDetails = productService.getAllProductDetails();
        log.info("Số lượng chi tiết sản phẩm lấy được: {}", productDetails != null ? productDetails.size() : 0);

        List<Product> products = new ArrayList<>();
        if (productDetails == null || productDetails.isEmpty()) {
            log.warn("Product detail list is null or empty");
        } else {
            Map<Product, List<ProductDetail>> productMap = productDetails.stream()
                    .collect(Collectors.groupingBy(
                            ProductDetail::getProduct,
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            products = productMap.entrySet().stream()
                    .map(entry -> {
                        Product product = entry.getKey();
                        List<ProductDetail> details = entry.getValue();
                        Hibernate.initialize(product);
                        details.forEach(detail -> {
                            Hibernate.initialize(detail.getColor());
                            Hibernate.initialize(detail.getSize());
                            Hibernate.initialize(detail.getImages());
                        });
                        int totalStockQuantity = details.stream()
                                .mapToInt(ProductDetail::getStockQuantity)
                                .sum();
                        product.setTotalStockQuantity(totalStockQuantity);

                        // Tính khoảng giá
                        BigDecimal minPrice = details.stream()
                                .map(ProductDetail::getPrice)
                                .filter(Objects::nonNull)
                                .min(BigDecimal::compareTo)
                                .orElse(BigDecimal.ZERO);
                        BigDecimal maxPrice = details.stream()
                                .map(ProductDetail::getPrice)
                                .filter(Objects::nonNull)
                                .max(BigDecimal::compareTo)
                                .orElse(BigDecimal.ZERO);
                        product.setMinPrice(minPrice);
                        product.setMaxPrice(maxPrice);

                        // Thêm định dạng giá
                        product.setMinPriceFormatted(orderService.formatCurrency(minPrice));
                        product.setMaxPriceFormatted(orderService.formatCurrency(maxPrice));

                        return product;
                    })
                    .sorted(Comparator.comparing(Product::getId))
                    .collect(Collectors.toList());
        }
        model.addAttribute("products", products);

        List<User> customers = userService.getAllCustomers();
        if (customers == null) {
            log.warn("Customer list is null");
            customers = new ArrayList<>();
        }
        model.addAttribute("customers", customers);

        List<DiscountVoucher> allVouchers = discountVoucherService.getAllVouchers();
        if (allVouchers == null) {
            log.warn("Discount voucher list is null");
            allVouchers = new ArrayList<>();
        }

        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> discountVouchers = new ArrayList<>();

        for (DiscountVoucher voucher : allVouchers) {
            if (now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
                log.debug("Skipping expired voucher: voucherId={}, startDate={}, endDate={}",
                        voucher.getId(), voucher.getStartDate(), voucher.getEndDate());
                continue;
            }

            if (voucher.getUsageCount() >= voucher.getQuantity()) {
                log.debug("Skipping fully used voucher: voucherId={}, usageCount={}, quantity={}",
                        voucher.getId(), voucher.getUsageCount(), voucher.getQuantity());
                continue;
            }

            Map<String, Object> voucherData = new HashMap<>();
            voucherData.put("id", voucher.getId());
            voucherData.put("name", voucher.getName());
            voucherData.put("minOrderValue", voucher.getMinOrderValue());
            voucherData.put("minOrderValueFormatted", orderService.formatCurrency(voucher.getMinOrderValue()));
            discountVouchers.add(voucherData);
        }
        model.addAttribute("discountVouchers", discountVouchers);

        Order cart = (Order) session.getAttribute("cart");
        if (cart == null) {
            log.info("Cart is null, initializing new cart");
            cart = new Order();
            cart.setOrderDetails(new ArrayList<>());
            session.setAttribute("cart", cart);
        } else {
            Hibernate.initialize(cart.getOrderDetails());
            cart.getOrderDetails().forEach(detail -> {
                Hibernate.initialize(detail.getProductDetail());
                Hibernate.initialize(detail.getProductDetail().getColor());
                Hibernate.initialize(detail.getProductDetail().getSize());
            });
        }

        updateCartTotals(model, session, cart, BigDecimal.ZERO);
        return "WebQuanLy/banhang";
    }

    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam Integer productDetailId,
            @RequestParam Integer quantity,
            @RequestParam BigDecimal shippingFee,
            HttpSession session) {
        log.info("Adding product to cart: productDetailId={}, quantity={}", productDetailId, quantity);

        if (productDetailId == null || quantity == null || quantity <= 0) {
            log.error("Invalid productDetailId or quantity: productDetailId={}, quantity={}", productDetailId, quantity);
            return ResponseEntity.badRequest().body(Map.of("error", "Dữ liệu không hợp lệ."));
        }

        Order cart = (Order) session.getAttribute("cart");
        if (cart == null) {
            log.info("Cart is null, initializing new cart");
            cart = new Order();
            cart.setOrderDetails(new ArrayList<>());
            session.setAttribute("cart", cart);
        }

        ProductDetail productDetail = productService.getProductDetailById(productDetailId);
        if (productDetail == null) {
            log.error("Product not found: productDetailId={}", productDetailId);
            return ResponseEntity.badRequest().body(Map.of("error", "Sản phẩm không tồn tại."));
        }
        Hibernate.initialize(productDetail.getProduct());
        Hibernate.initialize(productDetail.getColor());
        Hibernate.initialize(productDetail.getSize());
        Hibernate.initialize(productDetail.getImages());

        if (productDetail.getStockQuantity() < quantity) {
            log.error("Insufficient stock: productDetailId={}, stockQuantity={}", productDetailId, productDetail.getStockQuantity());
            return ResponseEntity.badRequest().body(Map.of("error", "Không đủ hàng trong kho."));
        }

        Optional<OrderDetail> existingItem = cart.getOrderDetails().stream()
                .filter(item -> item.getProductDetail().getId().equals(productDetailId))
                .findFirst();

        if (existingItem.isPresent()) {
            OrderDetail item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            if (productDetail.getStockQuantity() < newQuantity) {
                log.error("Insufficient stock for updated quantity: productDetailId={}, newQuantity={}, stockQuantity={}",
                        productDetailId, newQuantity, productDetail.getStockQuantity());
                return ResponseEntity.badRequest().body(Map.of("error", "Số lượng trong kho không đủ."));
            }
            item.setQuantity(newQuantity);
            log.info("Updated quantity for existing item: productDetailId={}, newQuantity={}", productDetailId, newQuantity);
        } else {
            OrderDetail newItem = new OrderDetail();
            newItem.setProductDetail(productDetail);
            newItem.setProductName(productDetail.getProduct().getName());
            newItem.setPrice(productDetail.getPrice());
            newItem.setQuantity(quantity);
            newItem.setOrder(cart);
            cart.getOrderDetails().add(newItem);
            log.info("Added new item to cart: productDetailId={}, quantity={}", productDetailId, quantity);
        }

        session.setAttribute("cart", cart);
        Map<String, Object> response = updateCartTotals(null, session, cart, shippingFee);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cart/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCart(
            @RequestParam Integer productDetailId,
            @RequestParam Integer quantity,
            HttpSession session) {
        log.info("Updating cart: productDetailId={}, quantity={}", productDetailId, quantity);

        if (productDetailId == null || quantity == null) {
            log.error("Invalid productDetailId or quantity: productDetailId={}, quantity={}", productDetailId, quantity);
            return ResponseEntity.badRequest().body(Map.of("error", "Dữ liệu không hợp lệ."));
        }

        Order cart = (Order) session.getAttribute("cart");
        if (cart == null || cart.getOrderDetails().isEmpty()) {
            log.warn("Cart is empty or null");
            return ResponseEntity.badRequest().body(Map.of("error", "Giỏ hàng trống."));
        }

        ProductDetail productDetail = productService.getProductDetailById(productDetailId);
        if (productDetail == null || productDetail.getStockQuantity() < quantity) {
            log.error("Product not found or insufficient stock: productDetailId={}, stockQuantity={}",
                    productDetailId, productDetail != null ? productDetail.getStockQuantity() : "N/A");
            return ResponseEntity.badRequest().body(Map.of("error", "Số lượng trong kho không đủ."));
        }

        cart.getOrderDetails().stream()
                .filter(item -> item.getProductDetail().getId().equals(productDetailId))
                .findFirst()
                .ifPresent(item -> {
                    if (quantity <= 0) {
                        cart.getOrderDetails().remove(item);
                        log.info("Removed item from cart: productDetailId={}", productDetailId);
                    } else {
                        item.setQuantity(quantity);
                        log.info("Updated quantity: productDetailId={}, newQuantity={}", productDetailId, quantity);
                    }
                });

        session.setAttribute("cart", cart);
        Map<String, Object> response = updateCartTotals(null, session, cart, BigDecimal.ZERO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cart/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @RequestParam Integer productDetailId,
            HttpSession session) {
        log.info("Removing product from cart: productDetailId={}", productDetailId);

        if (productDetailId == null) {
            log.error("Invalid productDetailId: {}", productDetailId);
            return ResponseEntity.badRequest().body(Map.of("error", "Dữ liệu không hợp lệ."));
        }

        Order cart = (Order) session.getAttribute("cart");
        if (cart != null) {
            cart.getOrderDetails().removeIf(item -> item.getProductDetail().getId().equals(productDetailId));
            session.setAttribute("cart", cart);
            log.info("Removed product from cart: productDetailId={}", productDetailId);
        } else {
            log.warn("Cart is null");
        }

        Map<String, Object> response = updateCartTotals(null, session, cart, BigDecimal.ZERO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cart/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearCart(HttpSession session) {
        log.info("Clearing cart");
        session.removeAttribute("cart");
        session.removeAttribute("discount");
        session.removeAttribute("voucherId");
        Map<String, Object> response = updateCartTotals(null, session, null, BigDecimal.ZERO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cart/apply-voucher")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> applyVoucher(
            @RequestParam Long voucherId,
            HttpSession session) {
        log.info("Applying voucher: voucherId={}", voucherId);

        if (voucherId == null || voucherId == 0) {
            // Nếu voucherId là 0 hoặc null, xóa mã giảm giá
            session.removeAttribute("discount");
            session.removeAttribute("voucherId");
            Order cart = (Order) session.getAttribute("cart");
            if (cart == null || cart.getOrderDetails().isEmpty()) {
                log.warn("Cart is empty or null");
                return ResponseEntity.badRequest().body(Map.of("error", "Giỏ hàng trống."));
            }
            Map<String, Object> response = updateCartTotals(null, session, cart, BigDecimal.ZERO);
            log.info("Removed voucher from session");
            return ResponseEntity.ok(response);
        }

        Order cart = (Order) session.getAttribute("cart");
        if (cart == null || cart.getOrderDetails().isEmpty()) {
            log.warn("Cart is empty or null");
            return ResponseEntity.badRequest().body(Map.of("error", "Giỏ hàng trống."));
        }

        BigDecimal subTotal = cart.getOrderDetails().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DiscountVoucher voucher = discountVoucherService.getById(voucherId);
        if (voucher == null) {
            log.error("Voucher not found: voucherId={}", voucherId);
            return ResponseEntity.badRequest().body(Map.of("error", "Mã giảm giá không hợp lệ."));
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
            log.warn("Voucher is not valid: voucherId={}, startDate={}, endDate={}", voucherId, voucher.getStartDate(), voucher.getEndDate());
            return ResponseEntity.badRequest().body(Map.of("error", "Mã giảm giá chưa có hiệu lực hoặc đã hết hạn."));
        }

        if (subTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            session.removeAttribute("discount");
            session.removeAttribute("voucherId");
            Map<String, Object> response = updateCartTotals(null, session, cart, BigDecimal.ZERO);
            response.put("error", "Đơn hàng không đủ giá trị tối thiểu để áp dụng mã.");
            log.info("Voucher not applied: subTotal={} is less than minOrderValue={}", subTotal, voucher.getMinOrderValue());
            return ResponseEntity.ok(response);
        }

        if (voucher.getUsageCount() >= voucher.getQuantity()) {
            log.warn("Voucher usage limit reached: voucherId={}, usageCount={}, quantity={}",
                    voucherId, voucher.getUsageCount(), voucher.getQuantity());
            return ResponseEntity.badRequest().body(Map.of("error", "Mã giảm giá đã được sử dụng hết."));
        }

        BigDecimal discountValue;
        if ("PERCENT".equalsIgnoreCase(voucher.getType())) {
            discountValue = subTotal.multiply(voucher.getDiscountValue());
            if (voucher.getMaxDiscountValue() != null && discountValue.compareTo(voucher.getMaxDiscountValue()) > 0) {
                discountValue = voucher.getMaxDiscountValue();
            }
        } else {
            discountValue = voucher.getDiscountValue();
        }

        session.setAttribute("discount", discountValue);
        session.setAttribute("voucherId", voucherId);
        log.info("Applied voucher: voucherId={}, discountValue={}", voucherId, discountValue);

        Map<String, Object> response = updateCartTotals(null, session, cart, BigDecimal.ZERO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-order")
    @ResponseBody
    public ResponseEntity<?> createOrder(
            @RequestParam String selectedCustomer,
            @RequestParam BigDecimal shippingFee,
            @RequestParam String paymentMethod,
            @RequestParam String deliveryMethod,
            HttpSession session) {
        log.info("Tạo đơn hàng: selectedCustomer={}, shippingFee={}, paymentMethod={}, deliveryMethod={}",
                selectedCustomer, shippingFee, paymentMethod, deliveryMethod);

        try {
            // Kiểm tra đầu vào
            if (selectedCustomer == null || selectedCustomer.trim().isEmpty()) {
                log.error("Khách hàng không hợp lệ: selectedCustomer={}", selectedCustomer);
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn khách hàng!"));
            }

            if (shippingFee == null || shippingFee.compareTo(BigDecimal.ZERO) < 0) {
                log.error("Phí vận chuyển không hợp lệ: shippingFee={}", shippingFee);
                return ResponseEntity.badRequest().body(Map.of("error", "Phí vận chuyển không hợp lệ!"));
            }

            if (paymentMethod == null || !Arrays.asList("Tiền mặt", "Chuyển khoản").contains(paymentMethod)) {
                log.error("Phương thức thanh toán không hợp lệ: paymentMethod={}", paymentMethod);
                return ResponseEntity.badRequest().body(Map.of("error", "Phương thức thanh toán không hợp lệ!"));
            }

            if (deliveryMethod == null || !Arrays.asList("Tại quầy", "Giao hàng").contains(deliveryMethod)) {
                log.error("Hình thức giao hàng không hợp lệ: deliveryMethod={}", deliveryMethod);
                return ResponseEntity.badRequest().body(Map.of("error", "Hình thức giao hàng không hợp lệ!"));
            }

            Order cart = (Order) session.getAttribute("cart");
            if (cart == null || cart.getOrderDetails().isEmpty()) {
                log.warn("Giỏ hàng trống hoặc null");
                return ResponseEntity.badRequest().body(Map.of("error", "Giỏ hàng trống!"));
            }

            Optional<User> optionalUser = userService.findByPhone(selectedCustomer);
            if (optionalUser.isEmpty()) {
                log.error("Không tìm thấy khách hàng: phone={}", selectedCustomer);
                return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy khách hàng!"));
            }

            User user = optionalUser.get();

            Order order = new Order();
            order.setUser(user);
            order.setCreatedAt(LocalDateTime.now());
            order.setPaymentMethod(paymentMethod);
            order.setDeliveryMethod(deliveryMethod);
            order.setShippingFee(shippingFee);
            order.setStatus("Completed");
            order.setCode("HD" + System.currentTimeMillis());

            List<OrderDetail> detailList = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;
            Map<Integer, Integer> updatedStockQuantities = new HashMap<>();

            for (OrderDetail item : cart.getOrderDetails()) {
                ProductDetail productDetail = productService.getProductDetailById(item.getProductDetail().getId());
                if (productDetail == null) {
                    log.error("Không tìm thấy chi tiết sản phẩm: productDetailId={}", item.getProductDetail().getId());
                    return ResponseEntity.badRequest().body(Map.of("error", "Sản phẩm " + item.getProductName() + " không tồn tại."));
                }
                if (productDetail.getStockQuantity() < item.getQuantity()) {
                    log.error("Hàng tồn kho không đủ: productDetailId={}, stockQuantity={}",
                            productDetail.getId(), productDetail.getStockQuantity());
                    return ResponseEntity.badRequest().body(Map.of("error", "Sản phẩm " + item.getProductName() + " không đủ hàng."));
                }

                OrderDetail detail = new OrderDetail();
                detail.setOrder(order);
                detail.setProductDetail(productDetail);
                detail.setProductName(item.getProductName());
                detail.setQuantity(item.getQuantity());
                detail.setPrice(item.getPrice());

                productDetail.setStockQuantity(productDetail.getStockQuantity() - item.getQuantity());
                productService.saveProductDetail(productDetail);

                List<ProductDetail> productDetails = productService.getProductDetailsByProductId(productDetail.getProduct().getId());
                int totalStock = productDetails.stream()
                        .mapToInt(ProductDetail::getStockQuantity)
                        .sum();
                updatedStockQuantities.put(productDetail.getProduct().getId(), totalStock);

                total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                detailList.add(detail);
            }

            BigDecimal discount = (BigDecimal) session.getAttribute("discount");
            Long voucherId = (Long) session.getAttribute("voucherId");
            if (discount != null && voucherId != null) {
                total = total.subtract(discount);
                DiscountVoucher voucher = discountVoucherService.getById(voucherId);
                if (voucher != null) {
                    voucher.setUsageCount(voucher.getUsageCount() + 1);
                    discountVoucherService.save(voucher);
                    log.info("Cập nhật số lần sử dụng voucher: voucherId={}, newUsageCount={}", voucherId, voucher.getUsageCount());
                }
            }

            total = total.add(shippingFee);
            order.setTotalPrice(total);
            order.setOrderDetails(detailList);

            orderService.saveOrder(order);
            log.info("Tạo đơn hàng thành công: orderCode={}", order.getCode());

            session.removeAttribute("cart");
            session.removeAttribute("discount");
            session.removeAttribute("voucherId");

            Map<String, Object> jsonResponse = new HashMap<>();
            jsonResponse.put("message", "Đơn hàng đã được tạo thành công! Mã: " + order.getCode());
            jsonResponse.put("stockQuantities", updatedStockQuantities);

            return ResponseEntity.ok(jsonResponse);
        } catch (Exception e) {
            log.error("Lỗi khi tạo đơn hàng: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi khi tạo đơn hàng: " + e.getMessage()));
        }
    }

    @GetMapping("/download-receipt/{orderCode}")
    public ResponseEntity<ByteArrayResource> downloadReceipt(@PathVariable String orderCode, HttpSession session) {
        try {
            // Lấy đơn hàng theo mã
            Order order = orderService.findByCode(orderCode);
            if (order == null) {
                log.error("Không tìm thấy đơn hàng: orderCode={}", orderCode);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ByteArrayResource(new byte[0]));
            }

            // Khởi tạo các collection Hibernate để tránh LazyInitializationException
            Hibernate.initialize(order.getOrderDetails());
            order.getOrderDetails().forEach(detail -> {
                Hibernate.initialize(detail.getProductDetail());
                Hibernate.initialize(detail.getProductDetail().getColor());
                Hibernate.initialize(detail.getProductDetail().getSize());
            });

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Sử dụng font DejaVu Sans để hỗ trợ tiếng Việt
            PdfFont font;
            try {
                font = PdfFontFactory.createFont("fonts/DejavuSans.ttf", "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                log.info("Font DejavuSans loaded successfully.");
            } catch (Exception e) {
                log.error("Failed to load font DejavuSans.ttf: {}", e.getMessage(), e);
                font = PdfFontFactory.createFont(); // Font mặc định
            }
            document.setFont(font);


            // Thêm tiêu đề và thông tin shop
            document.add(new Paragraph("ACV Store")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(24)
                    .setBold());
            document.add(new Paragraph("Địa chỉ: FPT Là Nhà")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12));
            document.add(new Paragraph("Số điện thoại: 0123456789")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12));
            document.add(new Paragraph("\n")
                    .setMarginBottom(10));
            document.add(new Paragraph("=====================================")
                    .setTextAlignment(TextAlignment.CENTER));

            // Thêm thông tin hóa đơn
            document.add(new Paragraph("HÓA ĐƠN THANH TOÁN")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18)
                    .setBold()
                    .setMarginTop(10));
            document.add(new Paragraph("Mã đơn hàng: " + order.getCode())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12));
            document.add(new Paragraph("Ngày tạo: " + order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12));
            document.add(new Paragraph("Khách hàng: " + order.getUser().getFullName())
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(12));
            document.add(new Paragraph("Số điện thoại: " + order.getUser().getPhone())
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(12));
            document.add(new Paragraph("\n")
                    .setMarginBottom(10));

            // Tạo bảng chi tiết đơn hàng với viền
            float[] columnWidths = {1, 3, 1, 2, 2};
            Table table = new Table(columnWidths);
            table.setWidth(UnitValue.createPercentValue(100));
            table.addHeaderCell(new Paragraph("STT")
                    .setBold()
                    .setBackgroundColor(new DeviceRgb(200, 200, 200)));
            table.addHeaderCell(new Paragraph("Sản phẩm")
                    .setBold()
                    .setBackgroundColor(new DeviceRgb(200, 200, 200)));
            table.addHeaderCell(new Paragraph("Số lượng")
                    .setBold()
                    .setBackgroundColor(new DeviceRgb(200, 200, 200)));
            table.addHeaderCell(new Paragraph("Đơn giá")
                    .setBold()
                    .setBackgroundColor(new DeviceRgb(200, 200, 200)));
            table.addHeaderCell(new Paragraph("Thành tiền")
                    .setBold()
                    .setBackgroundColor(new DeviceRgb(200, 200, 200)));

            int index = 1;
            for (OrderDetail detail : order.getOrderDetails()) {
                table.addCell(new Paragraph(String.valueOf(index++))
                        .setFontSize(10));
                table.addCell(new Paragraph(detail.getProductName() + " (" +
                        (detail.getProductDetail().getColor() != null ? detail.getProductDetail().getColor().getName() : "Không có màu") + ", " +
                        (detail.getProductDetail().getSize() != null ? detail.getProductDetail().getSize().getName() : "Không có kích thước") + ")")
                        .setFontSize(10));
                table.addCell(new Paragraph(String.valueOf(detail.getQuantity()))
                        .setFontSize(10));
                table.addCell(new Paragraph(orderService.formatCurrency(detail.getPrice()))
                        .setFontSize(10));
                table.addCell(new Paragraph(orderService.formatCurrency(detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity()))))
                        .setFontSize(10));
            }

            document.add(table);

            // Thêm tóm tắt
            document.add(new Paragraph("\n")
                    .setMarginBottom(10));
            document.add(new Paragraph("=====================================")
                    .setTextAlignment(TextAlignment.CENTER));
            BigDecimal subTotal = order.getOrderDetails().stream()
                    .map(detail -> detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int totalQuantity = order.getOrderDetails().stream()
                    .mapToInt(OrderDetail::getQuantity)
                    .sum();

            // Kiểm tra và lấy giá trị discount từ session
            BigDecimal discount = BigDecimal.ZERO;
            Object discountObj = session.getAttribute("discount");
            if (discountObj != null) {
                try {
                    discount = (BigDecimal) discountObj;
                } catch (ClassCastException e) {
                    log.error("Lỗi ép kiểu discount từ session: {}", e.getMessage(), e);
                }
            }

            BigDecimal totalAfterDiscount = subTotal.subtract(discount).add(order.getShippingFee());

            document.add(new Paragraph("Tổng số lượng: " + totalQuantity)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(12));
            document.add(new Paragraph("Tiền tạm tính: " + orderService.formatCurrency(subTotal))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(12));
            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                document.add(new Paragraph("Giảm giá: " + orderService.formatCurrency(discount))
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontSize(12));
            }
            document.add(new Paragraph("Phí vận chuyển: " + orderService.formatCurrency(order.getShippingFee()))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(12));
            document.add(new Paragraph("Tổng cộng: " + orderService.formatCurrency(totalAfterDiscount))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(12)
                    .setBold());

            // Tạo và thêm mã QR
            String qrText = "Mã đơn hàng: " + order.getCode() + "\nTổng tiền: " + orderService.formatCurrency(totalAfterDiscount);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix;
            try {
                bitMatrix = qrCodeWriter.encode(qrText, BarcodeFormat.QR_CODE, 100, 100);
                ByteArrayOutputStream qrBaos = new ByteArrayOutputStream();
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", qrBaos);
                Image qrImage = new Image(ImageDataFactory.create(qrBaos.toByteArray()))
                        .scaleToFit(100, 100);

                document.add(qrImage);
            } catch (WriterException e) {
                log.error("Lỗi khi tạo mã QR: {}", e.getMessage(), e);
            }

            // Thêm dòng cảm ơn
            document.add(new Paragraph("\n")
                    .setMarginTop(10));
            document.add(new Paragraph("Cảm ơn Quý khách. Hẹn gặp lại!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12));

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hoa_don_" + order.getCode() + ".pdf");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(pdfBytes.length)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (Exception e) {
            log.error("Lỗi khi tạo PDF cho đơn hàng {}: {}", orderCode, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ByteArrayResource(new byte[0]));
        }
    }

    @PostMapping("/vnpay-payment")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createVNPayPayment(
            @RequestParam String selectedCustomer,
            @RequestParam BigDecimal shippingFee,
            @RequestParam String paymentMethod,
            @RequestParam String deliveryMethod,
            HttpSession session) {
        log.info("Tạo URL thanh toán VNPay: selectedCustomer={}, shippingFee={}, paymentMethod={}, deliveryMethod={}",
                selectedCustomer, shippingFee, paymentMethod, deliveryMethod);

        try {
            // Kiểm tra dữ liệu đầu vào
            if (selectedCustomer == null || selectedCustomer.trim().isEmpty()) {
                log.error("Khách hàng không hợp lệ: selectedCustomer={}", selectedCustomer);
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn khách hàng!"));
            }

            if (shippingFee == null || shippingFee.compareTo(BigDecimal.ZERO) < 0) {
                log.error("Phí vận chuyển không hợp lệ: shippingFee={}", shippingFee);
                return ResponseEntity.badRequest().body(Map.of("error", "Phí vận chuyển không hợp lệ!"));
            }

            if (!"VNPay".equals(paymentMethod)) {
                log.error("Phương thức thanh toán không phải VNPay: paymentMethod={}", paymentMethod);
                return ResponseEntity.badRequest().body(Map.of("error", "Phương thức thanh toán không hợp lệ!"));
            }

            if (deliveryMethod == null || !Arrays.asList("Tại quầy", "Giao hàng").contains(deliveryMethod)) {
                log.error("Hình thức giao hàng không hợp lệ: deliveryMethod={}", deliveryMethod);
                return ResponseEntity.badRequest().body(Map.of("error", "Hình thức giao hàng không hợp lệ!"));
            }

            Order cart = (Order) session.getAttribute("cart");
            if (cart == null || cart.getOrderDetails().isEmpty()) {
                log.warn("Giỏ hàng trống hoặc null");
                return ResponseEntity.badRequest().body(Map.of("error", "Giỏ hàng trống!"));
            }

            Optional<User> optionalUser = userService.findByPhone(selectedCustomer);
            if (optionalUser.isEmpty()) {
                log.error("Không tìm thấy khách hàng: phone={}", selectedCustomer);
                return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy khách hàng!"));
            }

            // Tính tổng tiền
            BigDecimal subTotal = cart.getOrderDetails().stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal discount = (BigDecimal) session.getAttribute("discount");
            if (discount == null) discount = BigDecimal.ZERO;

            BigDecimal total = subTotal.subtract(discount).add(shippingFee);
            long amount = total.multiply(BigDecimal.valueOf(100)).longValue(); // VNPay yêu cầu số tiền nhân 100 (VND)

            // Tạo mã đơn hàng
            String orderCode = "HD" + System.currentTimeMillis();
            String vnp_TxnRef = orderCode;
            String vnp_IpAddr = "127.0.0.1"; // Có thể lấy IP thực tế từ request nếu cần
            String vnp_OrderInfo = "Thanh toán đơn hàng: " + vnp_TxnRef;

            // Tạo các tham số cho URL thanh toán
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", VNPayConfig.VNP_TMNCODE);
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
            vnp_Params.put("vnp_OrderType", "billpayment");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", VNPayConfig.VNP_RETURN_URL);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            // Định dạng thời gian
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String vnp_CreateDate = now.format(formatter);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            // Tạo chữ ký bảo mật
            String hashData = vnp_Params.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            String vnp_SecureHash = hmacSHA512(VNPayConfig.VNP_HASHSECRET, hashData);
            vnp_Params.put("vnp_SecureHash", vnp_SecureHash);

            // Tạo URL thanh toán
            StringBuilder paymentUrl = new StringBuilder(VNPayConfig.VNP_URL + "?");
            paymentUrl.append(vnp_Params.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&")));

            // Lưu thông tin đơn hàng tạm thời vào session để xử lý sau khi thanh toán
            Map<String, Object> pendingOrder = new HashMap<>();
            pendingOrder.put("selectedCustomer", selectedCustomer);
            pendingOrder.put("shippingFee", shippingFee);
            pendingOrder.put("paymentMethod", paymentMethod);
            pendingOrder.put("deliveryMethod", deliveryMethod);
            pendingOrder.put("cart", cart);
            pendingOrder.put("discount", discount);
            pendingOrder.put("voucherId", session.getAttribute("voucherId"));
            pendingOrder.put("orderCode", orderCode);
            session.setAttribute("pendingVNPayOrder", pendingOrder);

            log.info("Tạo URL thanh toán VNPay thành công: URL={}", paymentUrl);
            return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl.toString()));
        } catch (Exception e) {
            log.error("Lỗi khi tạo URL thanh toán VNPay: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi khi tạo URL thanh toán: " + e.getMessage()));
        }
    }

    // Hàm tạo chữ ký bảo mật bằng HmacSHA512
    private String hmacSHA512(String key, String data) throws Exception {
        Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        sha512_HMAC.init(secret_key);
        byte[] bytes = sha512_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hash = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hash.append('0');
            hash.append(hex);
        }
        return hash.toString();
    }

    @GetMapping("/vnpay-return")
    public String handleVNPayReturn(HttpServletRequest request, HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        log.info("Nhận callback từ VNPay");

        try {
            // Lấy các tham số từ VNPay
            Map<String, String> vnp_Params = new HashMap<>();
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                String paramValue = request.getParameter(paramName);
                vnp_Params.put(paramName, paramValue);
            }

            // Lấy chữ ký từ VNPay
            String vnp_SecureHash = vnp_Params.get("vnp_SecureHash");
            vnp_Params.remove("vnp_SecureHash");

            // Tạo chữ ký để kiểm tra
            String hashData = vnp_Params.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            String computedHash = hmacSHA512(VNPayConfig.VNP_HASHSECRET, hashData);

            // Kiểm tra chữ ký
            if (!computedHash.equalsIgnoreCase(vnp_SecureHash)) {
                log.error("Chữ ký không hợp lệ: computedHash={}, vnp_SecureHash={}", computedHash, vnp_SecureHash);
                redirectAttributes.addFlashAttribute("error", "Chữ ký không hợp lệ!");
                return "redirect:/acvstore/banhang";
            }

            // Kiểm tra trạng thái giao dịch
            String responseCode = vnp_Params.get("vnp_ResponseCode");
            String orderCode = vnp_Params.get("vnp_TxnRef");

            if (!"00".equals(responseCode)) {
                log.warn("Thanh toán VNPay thất bại: responseCode={}", responseCode);
                redirectAttributes.addFlashAttribute("error", "Thanh toán thất bại! Mã lỗi: " + responseCode);
                session.removeAttribute("pendingVNPayOrder");
                return "redirect:/acvstore/banhang";
            }

            // Lấy thông tin đơn hàng từ session
            Map<String, Object> pendingOrder = (Map<String, Object>) session.getAttribute("pendingVNPayOrder");
            if (pendingOrder == null) {
                log.error("Không tìm thấy thông tin đơn hàng tạm thời trong session");
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin đơn hàng!");
                return "redirect:/acvstore/banhang";
            }

            // Gán lại các giá trị từ pendingOrder
            String selectedCustomer = (String) pendingOrder.get("selectedCustomer");
            BigDecimal shippingFee = (BigDecimal) pendingOrder.get("shippingFee");
            String paymentMethod = (String) pendingOrder.get("paymentMethod");
            String deliveryMethod = (String) pendingOrder.get("deliveryMethod");
            Order cart = (Order) pendingOrder.get("cart");
            BigDecimal discount = (BigDecimal) pendingOrder.get("discount");
            Long voucherId = (Long) pendingOrder.get("voucherId");
            String savedOrderCode = (String) pendingOrder.get("orderCode");

            // Kiểm tra mã đơn hàng
            if (!savedOrderCode.equals(orderCode)) {
                log.error("Mã đơn hàng không khớp: savedOrderCode={}, vnp_TxnRef={}", savedOrderCode, orderCode);
                redirectAttributes.addFlashAttribute("error", "Mã đơn hàng không khớp!");
                session.removeAttribute("pendingVNPayOrder");
                return "redirect:/acvstore/banhang";
            }

            // Tạo đơn hàng
            Optional<User> optionalUser = userService.findByPhone(selectedCustomer);
            if (optionalUser.isEmpty()) {
                log.error("Không tìm thấy khách hàng: phone={}", selectedCustomer);
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy khách hàng!");
                return "redirect:/acvstore/banhang";
            }

            User user = optionalUser.get();
            Order order = new Order();
            order.setUser(user);
            order.setCreatedAt(LocalDateTime.now());
            order.setPaymentMethod(paymentMethod);
            order.setDeliveryMethod(deliveryMethod);
            order.setShippingFee(shippingFee);
            order.setStatus("Completed");
            order.setCode(orderCode);

            List<OrderDetail> detailList = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;
            Map<Integer, Integer> updatedStockQuantities = new HashMap<>();

            for (OrderDetail item : cart.getOrderDetails()) {
                ProductDetail productDetail = productService.getProductDetailById(item.getProductDetail().getId());
                if (productDetail == null) {
                    log.error("Không tìm thấy chi tiết sản phẩm: productDetailId={}", item.getProductDetail().getId());
                    redirectAttributes.addFlashAttribute("error", "Sản phẩm " + item.getProductName() + " không tồn tại!");
                    return "redirect:/acvstore/banhang";
                }
                if (productDetail.getStockQuantity() < item.getQuantity()) {
                    log.error("Hàng tồn kho không đủ: productDetailId={}, stockQuantity={}", productDetail.getId(), productDetail.getStockQuantity());
                    redirectAttributes.addFlashAttribute("error", "Sản phẩm " + item.getProductName() + " không đủ hàng!");
                    return "redirect:/acvstore/banhang";
                }

                OrderDetail detail = new OrderDetail();
                detail.setOrder(order);
                detail.setProductDetail(productDetail);
                detail.setProductName(item.getProductName());
                detail.setQuantity(item.getQuantity());
                detail.setPrice(item.getPrice());

                productDetail.setStockQuantity(productDetail.getStockQuantity() - item.getQuantity());
                productService.saveProductDetail(productDetail);

                List<ProductDetail> productDetails = productService.getProductDetailsByProductId(productDetail.getProduct().getId());
                int totalStock = productDetails.stream()
                        .mapToInt(ProductDetail::getStockQuantity)
                        .sum();
                updatedStockQuantities.put(productDetail.getProduct().getId(), totalStock);

                total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                detailList.add(detail);
            }

            if (discount != null && voucherId != null) {
                total = total.subtract(discount);
                DiscountVoucher voucher = discountVoucherService.getById(voucherId);
                if (voucher != null) {
                    voucher.setUsageCount(voucher.getUsageCount() + 1);
                    discountVoucherService.save(voucher);
                    log.info("Cập nhật số lần sử dụng voucher: voucherId={}, newUsageCount={}", voucherId, voucher.getUsageCount());
                }
            }

            total = total.add(shippingFee);
            order.setTotalPrice(total);
            order.setOrderDetails(detailList);

            orderService.saveOrder(order);
            log.info("Tạo đơn hàng thành công từ VNPay: orderCode={}", order.getCode());

            // Xóa dữ liệu tạm trong session
            session.removeAttribute("cart");
            session.removeAttribute("discount");
            session.removeAttribute("voucherId");
            session.removeAttribute("pendingVNPayOrder");

            // Thêm thông tin để tải PDF tự động trên trang banhang
            redirectAttributes.addFlashAttribute("message", "Thanh toán VNPay thành công! Mã đơn: " + order.getCode());
            redirectAttributes.addFlashAttribute("stockQuantities", updatedStockQuantities);
            redirectAttributes.addFlashAttribute("downloadReceipt", order.getCode()); // Dùng để kích hoạt tải PDF trên trang banhang

            // Chuyển hướng về trang bán hàng
            return "redirect:/acvstore/banhang";
        } catch (Exception e) {
            log.error("Lỗi khi xử lý callback VNPay: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xử lý thanh toán VNPay: " + e.getMessage());
            return "redirect:/acvstore/banhang";
        }
    }

    @PostMapping("/add-customer")
    public String addCustomer(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam(required = false) String email) {
        log.info("Adding new customer: fullName={}, phone={}, email={}", fullName, phone, email);

        if (fullName == null || fullName.trim().isEmpty() || phone == null || phone.trim().isEmpty()) {
            log.error("Invalid customer data: fullName={}, phone={}", fullName, phone);
            return "redirect:/acvstore/banhang?error=invalid_customer_data";
        }

        User user = new User();
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setUsername(phone);
        user.setRole("CUSTOMER");

        userService.saveUser(user);
        log.info("Customer added successfully: phone={}", phone);

        return "redirect:/acvstore/banhang?successAdd=true";
    }

    @GetMapping("/product/variant")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getVariantDetails(
            @RequestParam Integer productId,
            @RequestParam Integer colorId,
            @RequestParam Integer sizeId) {
        log.info("Fetching variant details: productId={}, colorId={}, sizeId={}", productId, colorId, sizeId);

        Map<String, Object> response = new HashMap<>();

        if (productId == null || colorId == null || sizeId == null) {
            log.error("Invalid parameters: productId={}, colorId={}, sizeId={}", productId, colorId, sizeId);
            response.put("error", "Dữ liệu không hợp lệ!");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            ProductDetail productDetail = productService.getProductDetailByProductIdAndColorIdAndSizeId(productId, colorId, sizeId);
            if (productDetail == null) {
                log.error("Variant not found: productId={}, colorId={}, sizeId={}", productId, colorId, sizeId);
                response.put("error", "Không tìm thấy biến thể với màu sắc và kích thước đã chọn!");
                return ResponseEntity.badRequest().body(response);
            }

            Hibernate.initialize(productDetail.getProduct());
            Hibernate.initialize(productDetail.getColor());
            Hibernate.initialize(productDetail.getSize());
            Hibernate.initialize(productDetail.getImages());

            response.put("productDetailId", productDetail.getId());
            response.put("stockQuantity", productDetail.getStockQuantity());
            response.put("price", productDetail.getPrice());
            if (productDetail.getImages() != null && !productDetail.getImages().isEmpty()) {
                response.put("imageUrl", "/Uploads/" + productDetail.getImages().get(0).getImageUrl()); // Sửa từ /images/ thành /Uploads/
            } else {
                response.put("imageUrl", null);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching variant details: {}", e.getMessage(), e);
            response.put("error", "Lỗi khi lấy thông tin biến thể: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/product/{productId}/variants")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductVariants(@PathVariable Integer productId) {
        log.info("Fetching product variants: productId={}", productId);

        Map<String, Object> response = new HashMap<>();

        if (productId == null) {
            log.error("Invalid productId: {}", productId);
            response.put("error", "Dữ liệu không hợp lệ!");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            List<ProductDetail> details = productService.getProductDetailsByProductId(productId);
            if (details == null || details.isEmpty()) {
                log.warn("Product has no variants: productId={}", productId);
                response.put("error", "Sản phẩm không có biến thể!");
                return ResponseEntity.badRequest().body(response);
            }

            details.forEach(detail -> {
                Hibernate.initialize(detail.getColor());
                Hibernate.initialize(detail.getSize());
                Hibernate.initialize(detail.getImages());
            });

            List<Map<String, Object>> colors = details.stream()
                    .map(ProductDetail::getColor)
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(color -> {
                        Map<String, Object> colorMap = new HashMap<>();
                        colorMap.put("id", color.getId());
                        colorMap.put("name", color.getName());
                        return colorMap;
                    })
                    .collect(Collectors.toList());

            List<Map<String, Object>> sizes = details.stream()
                    .map(ProductDetail::getSize)
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(size -> {
                        Map<String, Object> sizeMap = new HashMap<>();
                        sizeMap.put("id", size.getId());
                        sizeMap.put("name", size.getName());
                        return sizeMap;
                    })
                    .collect(Collectors.toList());

            response.put("colors", colors);
            response.put("sizes", sizes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching product variants: {}", e.getMessage(), e);
            response.put("error", "Lỗi khi lấy danh sách biến thể: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/product-detail/{productDetailId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductDetailInfo(@PathVariable Integer productDetailId) {
        log.info("Fetching product detail info: productDetailId={}", productDetailId);

        Map<String, Object> response = new HashMap<>();

        if (productDetailId == null) {
            log.error("Invalid productDetailId: {}", productDetailId);
            response.put("error", "Dữ liệu không hợp lệ!");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            ProductDetail productDetail = productService.getProductDetailById(productDetailId);
            if (productDetail == null) {
                log.error("Product detail not found: productDetailId={}", productDetailId);
                response.put("error", "Không tìm thấy biến thể sản phẩm!");
                return ResponseEntity.badRequest().body(response);
            }

            Hibernate.initialize(productDetail.getProduct());
            response.put("productId", productDetail.getProduct().getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching product detail info: {}", e.getMessage(), e);
            response.put("error", "Lỗi khi lấy thông tin biến thể: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/stock-quantities")
    @ResponseBody
    public ResponseEntity<Map<Integer, Integer>> getStockQuantities() {
        log.info("Fetching stock quantities for all products");

        List<ProductDetail> productDetails = productService.getAllProductDetails();
        Map<Integer, Integer> stockQuantities = new HashMap<>();

        if (productDetails != null && !productDetails.isEmpty()) {
            Map<Product, List<ProductDetail>> productMap = productDetails.stream()
                    .collect(Collectors.groupingBy(ProductDetail::getProduct));

            productMap.forEach((product, details) -> {
                int totalStock = details.stream()
                        .mapToInt(ProductDetail::getStockQuantity)
                        .sum();
                stockQuantities.put(product.getId(), totalStock);
            });
        }

        return ResponseEntity.ok(stockQuantities);
    }

    private Map<String, Object> updateCartTotals(Model model, HttpSession session, Order cart, BigDecimal shippingFee) {
        log.debug("Updating cart totals with shippingFee={}", shippingFee);

        Map<String, Object> totals = new HashMap<>();
        List<Map<String, Object>> cartItems = new ArrayList<>();

        BigDecimal subTotal = BigDecimal.ZERO;
        if (cart != null && !cart.getOrderDetails().isEmpty()) {
            subTotal = cart.getOrderDetails().stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            for (OrderDetail item : cart.getOrderDetails()) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("productDetailId", item.getProductDetail().getId());
                itemData.put("productName", item.getProductName());
                itemData.put("price", orderService.formatCurrency(item.getPrice()));
                itemData.put("quantity", item.getQuantity());
                itemData.put("subTotal", orderService.formatCurrency(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
                itemData.put("productDetailColor", item.getProductDetail().getColor() != null ? item.getProductDetail().getColor().getName() : "Không có màu");
                itemData.put("productDetailSize", item.getProductDetail().getSize() != null ? item.getProductDetail().getSize().getName() : "Không có kích thước");
                cartItems.add(itemData);
            }
        }

        BigDecimal discount = (BigDecimal) session.getAttribute("discount");
        Long voucherId = (Long) session.getAttribute("voucherId");

        boolean voucherApplied = true;
        boolean wasVoucherRemoved = false;
        BigDecimal removedDiscount = BigDecimal.ZERO;

        if (discount != null && voucherId != null) {
            DiscountVoucher voucher = discountVoucherService.getById(voucherId);
            if (voucher != null && subTotal.compareTo(voucher.getMinOrderValue()) < 0) {
                removedDiscount = discount;
                session.removeAttribute("discount");
                session.removeAttribute("voucherId");
                discount = BigDecimal.ZERO;
                voucherApplied = false;
                wasVoucherRemoved = true;
                log.info("Removed discount: subTotal={} is less than minOrderValue={}", subTotal, voucher.getMinOrderValue());
            }
        } else {
            discount = BigDecimal.ZERO;
            voucherApplied = false;
            wasVoucherRemoved = false;
        }

        BigDecimal total = subTotal.subtract(discount).add(shippingFee);

        totals.put("cartItems", cartItems);
        totals.put("subTotal", orderService.formatCurrency(subTotal));
        totals.put("discount", orderService.formatCurrency(discount));
        totals.put("total", orderService.formatCurrency(total));
        totals.put("discountAmount", discount.compareTo(BigDecimal.ZERO) > 0);
        totals.put("voucherApplied", voucherApplied);
        totals.put("wasVoucherRemoved", wasVoucherRemoved);
        if (wasVoucherRemoved) {
            totals.put("removedDiscount", orderService.formatCurrency(removedDiscount));
        }

        if (model != null) {
            model.addAttribute("cart", cart);
            model.addAttribute("subTotalFormatted", orderService.formatCurrency(subTotal));
            model.addAttribute("discountFormatted", orderService.formatCurrency(discount));
            model.addAttribute("totalPriceFormatted", orderService.formatCurrency(total));
            model.addAttribute("discountAmount", discount);
        }

        log.debug("Cart totals updated: subTotal={}, discount={}, shippingFee={}, total={}, voucherApplied={}, wasVoucherRemoved={}",
                subTotal, discount, shippingFee, total, voucherApplied, wasVoucherRemoved);
        return totals;
    }

    @GetMapping("/cart/get")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCart(HttpSession session) {
        log.info("Fetching cart data");

        Order cart = (Order) session.getAttribute("cart");
        if (cart == null) {
            cart = new Order();
            cart.setOrderDetails(new ArrayList<>());
            session.setAttribute("cart", cart);
        } else {
            Hibernate.initialize(cart.getOrderDetails());
            Iterator<OrderDetail> iterator = cart.getOrderDetails().iterator();
            while (iterator.hasNext()) {
                OrderDetail detail = iterator.next();
                Hibernate.initialize(detail.getProductDetail());
                Hibernate.initialize(detail.getProductDetail().getColor());
                Hibernate.initialize(detail.getProductDetail().getSize());
                ProductDetail productDetail = productService.getProductDetailById(detail.getProductDetail().getId());
                if (productDetail == null) {
                    log.warn("Removing invalid product from cart: productDetailId={}", detail.getProductDetail().getId());
                    iterator.remove();
                }
            }
            session.setAttribute("cart", cart);
        }

        Map<String, Object> response = updateCartTotals(null, session, cart, BigDecimal.ZERO);
        return ResponseEntity.ok(response);
    }

    // Lấy danh sách đơn hàng tạm thời
    @GetMapping("/held-orders")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getHeldOrders(HttpSession session) {
        log.info("Fetching held orders from session");
        List<Map<String, Object>> heldOrders = (List<Map<String, Object>>) session.getAttribute("heldOrders");
        if (heldOrders == null) {
            heldOrders = new ArrayList<>();
            session.setAttribute("heldOrders", heldOrders);
        }
        return ResponseEntity.ok(heldOrders);
    }

    // Thêm đơn hàng tạm thời
    @PostMapping("/hold-order")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> holdOrder(
            @RequestParam String selectedCustomer,
            @RequestParam String customerName,
            @RequestParam(required = false, defaultValue = "0") BigDecimal shippingFee,
            @RequestParam String deliveryMethod,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) BigDecimal cashAmount,
            HttpSession session) {
        log.info("Holding order for customer: selectedCustomer={}, customerName={}, shippingFee={}, deliveryMethod={}, paymentMethod={}",
                selectedCustomer, customerName, shippingFee, deliveryMethod, paymentMethod);

        // Kiểm tra dữ liệu đầu vào
        if (selectedCustomer == null || selectedCustomer.trim().isEmpty()) {
            log.warn("Invalid customer phone: {}", selectedCustomer);
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn khách hàng."));
        }
        if (customerName == null || customerName.trim().isEmpty()) {
            log.warn("Invalid customer name: {}", customerName);
            return ResponseEntity.badRequest().body(Map.of("error", "Tên khách hàng không hợp lệ."));
        }

        // Kiểm tra giỏ hàng
        Order cart = (Order) session.getAttribute("cart");
        if (cart == null || cart.getOrderDetails().isEmpty()) {
            log.warn("Cart is empty or null");
            return ResponseEntity.badRequest().body(Map.of("error", "Giỏ hàng trống."));
        }

        // Khởi tạo danh sách đơn hàng tạm thời
        List<Map<String, Object>> heldOrders = (List<Map<String, Object>>) session.getAttribute("heldOrders");
        if (heldOrders == null) {
            heldOrders = new ArrayList<>();
            session.setAttribute("heldOrders", heldOrders);
        }

        // Tạo dữ liệu đơn hàng tạm thời
        Map<String, Object> heldOrder = new HashMap<>();
        heldOrder.put("id", "HD" + System.currentTimeMillis());
        heldOrder.put("customerPhone", selectedCustomer);
        heldOrder.put("customer", customerName);
        heldOrder.put("items", cart.getOrderDetails().stream().map(item -> {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("productDetailId", item.getProductDetail().getId());
            itemData.put("productName", item.getProductName());
            itemData.put("quantity", item.getQuantity());
            itemData.put("price", item.getPrice());
            itemData.put("productDetailColor", item.getProductDetail().getColor() != null ?
                    item.getProductDetail().getColor().getName() : "Không có màu");
            itemData.put("productDetailSize", item.getProductDetail().getSize() != null ?
                    item.getProductDetail().getSize().getName() : "Không có kích thước");
            return itemData;
        }).collect(Collectors.toList()));
        heldOrder.put("total", cart.getOrderDetails().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        heldOrder.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        heldOrder.put("shippingFee", shippingFee);
        heldOrder.put("voucherId", session.getAttribute("voucherId"));
        heldOrder.put("deliveryMethod", deliveryMethod);
        heldOrder.put("paymentMethod", paymentMethod);
        heldOrder.put("discount", session.getAttribute("discount") != null ? session.getAttribute("discount") : BigDecimal.ZERO);
        heldOrder.put("cashAmount", cashAmount != null ? cashAmount : BigDecimal.ZERO);

        // Thêm đơn hàng vào danh sách tạm thời
        heldOrders.add(heldOrder);
        session.setAttribute("heldOrders", heldOrders);

        // Lưu thông tin vào session
        session.setAttribute("selectedCustomer", selectedCustomer);
        session.setAttribute("selectedCustomerName", customerName);
        session.setAttribute("shippingFee", shippingFee);
        session.setAttribute("deliveryMethod", deliveryMethod);
        session.setAttribute("paymentMethod", paymentMethod);
        session.setAttribute("cashAmount", cashAmount);

        // Xóa giỏ hàng hiện tại và các thuộc tính liên quan
        session.removeAttribute("cart");
        session.removeAttribute("discount");
        session.removeAttribute("voucherId");

        // Cập nhật tổng giỏ hàng và trả về phản hồi
        Map<String, Object> response = updateCartTotals(null, session, null, shippingFee);
        response.put("message", "Đơn hàng đã được giữ!");
        response.put("selectedCustomer", selectedCustomer);
        response.put("customerName", customerName);
        response.put("shippingFee", shippingFee.toString());
        response.put("voucherId", session.getAttribute("voucherId") != null ? session.getAttribute("voucherId").toString() : "");
        response.put("deliveryMethod", deliveryMethod);
        response.put("paymentMethod", paymentMethod);
        response.put("discount", session.getAttribute("discount") != null ? session.getAttribute("discount").toString() : "0");
        response.put("cashAmount", cashAmount != null ? cashAmount.toString() : "0");

        return ResponseEntity.ok(response);
    }

    // Khôi phục đơn hàng tạm thời
    @PostMapping("/restore-order")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> restoreOrder(@RequestParam String orderId, HttpSession session) {
        log.info("Restoring order: orderId={}", orderId);
        List<Map<String, Object>> heldOrders = (List<Map<String, Object>>) session.getAttribute("heldOrders");
        if (heldOrders == null) {
            log.warn("No held orders found in session");
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy đơn hàng tạm thời."));
        }

        Optional<Map<String, Object>> heldOrderOpt = heldOrders.stream()
                .filter(order -> order.get("id").equals(orderId))
                .findFirst();
        if (heldOrderOpt.isEmpty()) {
            log.warn("Held order not found: orderId={}", orderId);
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy đơn hàng tạm thời."));
        }

        Map<String, Object> heldOrder = heldOrderOpt.get();

        // Xóa giỏ hàng hiện tại và khởi tạo mới
        session.removeAttribute("cart");
        Order cart = new Order();
        cart.setOrderDetails(new ArrayList<>());
        session.setAttribute("cart", cart);

        // Khôi phục các sản phẩm
        List<Map<String, Object>> items = (List<Map<String, Object>>) heldOrder.get("items");
        List<String> errors = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Integer productDetailId = (Integer) item.get("productDetailId");
            Integer quantity = (Integer) item.get("quantity");
            ProductDetail productDetail = productService.getProductDetailById(productDetailId);
            if (productDetail == null) {
                errors.add("Sản phẩm " + item.get("productName") + " không tồn tại.");
                continue;
            }
            if (productDetail.getStockQuantity() < quantity) {
                int availableQuantity = productDetail.getStockQuantity();
                errors.add("Sản phẩm " + item.get("productName") + " chỉ còn " + availableQuantity + " hàng (yêu cầu: " + quantity + "). Số lượng đã điều chỉnh.");
                quantity = availableQuantity;
            }

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setProductDetail(productDetail);
            orderDetail.setProductName((String) item.get("productName"));
            orderDetail.setPrice(new BigDecimal(item.get("price").toString()));
            orderDetail.setQuantity(quantity);
            orderDetail.setOrder(cart);
            cart.getOrderDetails().add(orderDetail);
        }

        // Khôi phục thông tin khác
        String selectedCustomer = (String) heldOrder.get("customerPhone");
        String customerName = (String) heldOrder.get("customer");
        BigDecimal shippingFee = heldOrder.get("shippingFee") != null ? new BigDecimal(heldOrder.get("shippingFee").toString()) : BigDecimal.ZERO;
        Long voucherId = heldOrder.get("voucherId") != null ? Long.valueOf(heldOrder.get("voucherId").toString()) : null;
        String deliveryMethod = (String) heldOrder.get("deliveryMethod");
        String paymentMethod = (String) heldOrder.get("paymentMethod");
        BigDecimal discount = heldOrder.get("discount") != null ? new BigDecimal(heldOrder.get("discount").toString()) : BigDecimal.ZERO;
        BigDecimal cashAmount = heldOrder.get("cashAmount") != null ? new BigDecimal(heldOrder.get("cashAmount").toString()) : BigDecimal.ZERO;

        // Lưu thông tin vào session
        session.setAttribute("selectedCustomer", selectedCustomer);
        session.setAttribute("selectedCustomerName", customerName);
        session.setAttribute("shippingFee", shippingFee);
        session.setAttribute("deliveryMethod", deliveryMethod);
        session.setAttribute("paymentMethod", paymentMethod);
        session.setAttribute("cashAmount", cashAmount);

        // Áp dụng lại mã giảm giá nếu có
        if (voucherId != null) {
            DiscountVoucher voucher = discountVoucherService.getById(voucherId);
            if (voucher != null) {
                BigDecimal subTotal = cart.getOrderDetails().stream()
                        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                LocalDateTime now = LocalDateTime.now();
                if (now.isAfter(voucher.getStartDate()) && now.isBefore(voucher.getEndDate()) &&
                        subTotal.compareTo(voucher.getMinOrderValue()) >= 0 &&
                        voucher.getUsageCount() < voucher.getQuantity()) {
                    BigDecimal discountValue;
                    if ("PERCENT".equalsIgnoreCase(voucher.getType())) {
                        discountValue = subTotal.multiply(voucher.getDiscountValue());
                        if (voucher.getMaxDiscountValue() != null && discountValue.compareTo(voucher.getMaxDiscountValue()) > 0) {
                            discountValue = voucher.getMaxDiscountValue();
                        }
                    } else {
                        discountValue = voucher.getDiscountValue();
                    }
                    session.setAttribute("discount", discountValue);
                    session.setAttribute("voucherId", voucherId);
                } else {
                    session.removeAttribute("discount");
                    session.removeAttribute("voucherId");
                    errors.add("Mã giảm giá không còn hợp lệ hoặc đơn hàng không đủ điều kiện.");
                    voucherId = null; // Đặt lại voucherId để phản ánh trong response
                    discount = BigDecimal.ZERO;
                }
            } else {
                session.removeAttribute("discount");
                session.removeAttribute("voucherId");
                errors.add("Mã giảm giá không tồn tại.");
                voucherId = null;
                discount = BigDecimal.ZERO;
            }
        } else {
            session.removeAttribute("discount");
            session.removeAttribute("voucherId");
        }

        // Xóa đơn hàng tạm thời
        heldOrders.remove(heldOrder);
        session.setAttribute("heldOrders", heldOrders);

        // Cập nhật tổng giỏ hàng
        Map<String, Object> response = updateCartTotals(null, session, cart, shippingFee);
        response.put("selectedCustomer", selectedCustomer);
        response.put("customerName", customerName);
        response.put("shippingFee", shippingFee.toString());
        response.put("voucherId", voucherId != null ? voucherId.toString() : "");
        response.put("deliveryMethod", deliveryMethod != null ? deliveryMethod : "");
        response.put("paymentMethod", paymentMethod != null ? paymentMethod : "");
        response.put("discount", session.getAttribute("discount") != null ? session.getAttribute("discount").toString() : "0");
        response.put("cashAmount", cashAmount.toString());

        if (!errors.isEmpty()) {
            response.put("errors", errors);
            response.put("message", "Khôi phục đơn thành công, nhưng có lỗi: " + String.join("; ", errors));
        } else {
            response.put("message", "Khôi phục đơn thành công!");
        }
        return ResponseEntity.ok(response);
    }


    private DiscountCampaign getCampaignById(Long id) {
        return discountCampaignService.getById(id); // Cần thêm phương thức getById trong DiscountCampaignService
    }

    // Xóa đơn hàng tạm thời
    @PostMapping("/delete-held-order")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteHeldOrder(@RequestParam String orderId, HttpSession session) {
        log.info("Deleting held order: orderId={}", orderId);
        List<Map<String, Object>> heldOrders = (List<Map<String, Object>>) session.getAttribute("heldOrders");
        if (heldOrders == null) {
            log.warn("No held orders found in session");
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy đơn hàng tạm thời."));
        }

        boolean removed = heldOrders.removeIf(order -> order.get("id").equals(orderId));
        if (!removed) {
            log.warn("Held order not found: orderId={}", orderId);
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy đơn hàng tạm thời."));
        }

        session.setAttribute("heldOrders", heldOrders);
        return ResponseEntity.ok(Map.of("message", "Đã xóa đơn tạm thời!"));
    }

    @GetMapping("/held-order-details/{orderId}")
    @ResponseBody
    public ResponseEntity<?> getHeldOrderDetails(@PathVariable String orderId, HttpSession session) {
        log.info("Fetching held order details for orderId={}", orderId);
        List<Map<String, Object>> heldOrders = (List<Map<String, Object>>) session.getAttribute("heldOrders");
        if (heldOrders == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Không tìm thấy đơn hàng tạm thời."));
        }

        Optional<Map<String, Object>> heldOrderOpt = heldOrders.stream()
                .filter(order -> order.get("id").equals(orderId))
                .findFirst();

        if (heldOrderOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Đơn hàng tạm thời không tồn tại."));
        }

        Map<String, Object> heldOrder = heldOrderOpt.get();
        List<Map<String, Object>> items = (List<Map<String, Object>>) heldOrder.get("items");

        List<HeldOrderDetailDTO> detailList = items.stream().map(item -> {
            HeldOrderDetailDTO dto = new HeldOrderDetailDTO();
            dto.setProductName((String) item.get("productName"));
            dto.setColor((String) item.getOrDefault("productDetailColor", "Không có màu"));
            dto.setSize((String) item.getOrDefault("productDetailSize", "Không có kích thước"));
            dto.setQuantity((Integer) item.get("quantity"));
            dto.setPrice(new BigDecimal(item.get("price").toString()));
            dto.setSubTotal(dto.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
            return dto;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("id", orderId);
        response.put("customer", heldOrder.get("customer"));
        response.put("createdAt", heldOrder.get("createdAt"));
        BigDecimal originalTotal = heldOrder.get("total") != null
                ? new BigDecimal(heldOrder.get("total").toString())
                : BigDecimal.ZERO;
        BigDecimal discount = heldOrder.get("discount") != null
                ? new BigDecimal(heldOrder.get("discount").toString())
                : BigDecimal.ZERO;
        BigDecimal shippingFee = heldOrder.get("shippingFee") != null
                ? new BigDecimal(heldOrder.get("shippingFee").toString())
                : BigDecimal.ZERO;

        BigDecimal finalTotal = originalTotal.subtract(discount).add(shippingFee);
        response.put("total", finalTotal);

        response.put("items", detailList);

        // ✅ Thêm các field bị thiếu:
        response.put("discount", heldOrder.getOrDefault("discount", BigDecimal.ZERO));
        response.put("shippingFee", heldOrder.getOrDefault("shippingFee", BigDecimal.ZERO));
        response.put("deliveryMethod", heldOrder.getOrDefault("deliveryMethod", "Không có"));
        response.put("paymentMethod", heldOrder.getOrDefault("paymentMethod", "Không có"));

        return ResponseEntity.ok(response);
    }


    @GetMapping("/held-orders-page")
    public String viewHeldOrdersPage(HttpSession session, Model model) {
        List<Map<String, Object>> heldOrders = (List<Map<String, Object>>) session.getAttribute("heldOrders");
        if (heldOrders == null) {
            heldOrders = new ArrayList<>();
        }

        // Thêm định dạng trước khi truyền sang model
        for (Map<String, Object> order : heldOrders) {
            BigDecimal total = (BigDecimal) order.get("total");
            order.put("totalFormatted", orderService.formatCurrency(total));
        }

        model.addAttribute("heldOrders", heldOrders);
        return "WebQuanLy/held-orders";
    }

    // Dùng riêng cho form submit để redirect:
    @PostMapping("/restore-order-form")
    public String restoreOrderRedirect(@RequestParam String orderId, HttpSession session, RedirectAttributes redirectAttributes) {
        ResponseEntity<Map<String, Object>> response = restoreOrder(orderId, session); // dùng lại logic cũ
        Map<String, Object> data = response.getBody();

        if (data != null && data.containsKey("error")) {
            redirectAttributes.addFlashAttribute("error", data.get("error"));
        }
        return "redirect:/acvstore/banhang";
    }

    @PostMapping("/delete-held-order-form")
    public String deleteHeldOrderForm(@RequestParam String orderId, HttpSession session, RedirectAttributes redirectAttributes) {
        ResponseEntity<Map<String, Object>> response = deleteHeldOrder(orderId, session);
        Map<String, Object> data = response.getBody();

        if (data != null && data.containsKey("error")) {
            redirectAttributes.addFlashAttribute("error", data.get("error"));
        } else {
            redirectAttributes.addFlashAttribute("message", data.get("message"));
        }

        return "redirect:/acvstore/banhang/held-orders-page";
    }
}