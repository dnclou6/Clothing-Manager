package com.example.AsmGD1.controller.admin;

import com.example.AsmGD1.entity.Order;
import com.example.AsmGD1.entity.OrderDetail;
import com.example.AsmGD1.entity.ProductDetail;
import com.example.AsmGD1.service.OrderDetailService;
import com.example.AsmGD1.service.OrderService;
import com.example.AsmGD1.service.UserService;
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
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/acvstore")
public class InvoiceController {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @GetMapping("/hoadon")
    public String listInvoices(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate,
                               @RequestParam(required = false) String deliveryMethod,
                               @RequestParam(required = false) String paymentMethod,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               Model model) {

        keyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        status = (status != null && !status.trim().isEmpty()) ? status : null;
        startDate = (startDate != null && !startDate.trim().isEmpty()) ? startDate : null;
        endDate = (endDate != null && !endDate.trim().isEmpty()) ? endDate : null;
        deliveryMethod = (deliveryMethod != null && !deliveryMethod.trim().isEmpty()) ? deliveryMethod : null;
        paymentMethod = (paymentMethod != null && !paymentMethod.trim().isEmpty()) ? paymentMethod : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> pageInvoices = orderService.searchOrdersWithPagination(keyword, status, startDate, endDate, deliveryMethod, paymentMethod, pageable);
        List<Order> invoices = pageInvoices.getContent();

        model.addAttribute("pageSize", size);

        for (Order invoice : invoices) {
            orderService.updateOrderTotal(invoice.getId());
            invoice.setFormattedTotal(String.format("%,.0f đ", invoice.getTotalPrice()));
            invoice.setFormattedShippingFee(invoice.getShippingFee().compareTo(BigDecimal.ZERO) == 0
                    ? "Miễn phí" : String.format("%,.0f đ", invoice.getShippingFee()));
        }

        model.addAttribute("invoices", invoices);
        model.addAttribute("currentPage", pageInvoices.getNumber());
        model.addAttribute("totalPages", pageInvoices.getTotalPages());
        model.addAttribute("totalItems", pageInvoices.getTotalElements());

        model.addAttribute("totalOrders", orderService.countAllOrders());
        model.addAttribute("pendingOrders", orderService.countByStatus("Pending"));
        model.addAttribute("deliveringOrders", orderService.countByStatus("Delivering"));
        model.addAttribute("completedOrders", orderService.countByStatus("Completed"));
        model.addAttribute("cancelledOrders", orderService.countByStatus("Cancelled"));

        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("deliveryMethod", deliveryMethod);
        model.addAttribute("paymentMethod", paymentMethod);

        List<Map<String, Object>> revenueData = orderService.getRevenueByDay();
        List<String> labels = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        for (Map<String, Object> row : revenueData) {
            labels.add(row.get("day").toString());
            values.add((BigDecimal) row.get("totalRevenue"));
        }
        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartValues", values);

        return "WebQuanLy/hoadon";
    }

    @GetMapping("/hoadon/{id}")
    public String viewInvoice(@PathVariable Integer id, Model model) {
        Order order = orderService.findById(id);
        if (order == null) {
            return "redirect:/acvstore/hoadon";
        }

        BigDecimal totalPrice = order.getTotalPrice();
        String formattedTotal = String.format("%,.0f đ", totalPrice);
        List<OrderDetail> orderDetails = orderDetailService.findByOrderId(id);

        model.addAttribute("invoice", order);
        model.addAttribute("formattedTotal", formattedTotal);
        model.addAttribute("orderDetails", orderDetails);
        model.addAttribute("invoiceId", id);

        return "WebQuanLy/hoadon_chitiet";
    }

    @GetMapping("/hoadon/{orderId}/add-detail")
    public String showAddOrderDetailForm(@PathVariable Integer orderId, Model model) {
        Order order = orderService.findById(orderId);
        if (order == null) {
            return "redirect:/acvstore/hoadon";
        }
        List<ProductDetail> productDetails = productService.getAllProductDetails();

        model.addAttribute("order", order);
        model.addAttribute("productDetails", productDetails);
        model.addAttribute("orderDetail", new OrderDetail());
        return "WebQuanLy/orderdetails_form";
    }

    @PostMapping("/hoadon/{orderId}/save-detail")
    public String saveOrderDetail(@PathVariable Integer orderId,
                                  @ModelAttribute("orderDetail") OrderDetail detail,
                                  @RequestParam("productDetailId") Integer productDetailId) {
        Order order = orderService.findById(orderId);
        if (order == null) {
            return "redirect:/acvstore/hoadon";
        }
        ProductDetail productDetail = productService.getProductDetailById(productDetailId);
        if (productDetail == null) {
            return "redirect:/acvstore/hoadon/" + orderId + "?error=invalidProduct";
        }

        if (productDetail.getStockQuantity() < detail.getQuantity()) {
            return "redirect:/acvstore/hoadon/" + orderId + "?error=insufficientStock";
        }

        List<OrderDetail> existingDetails = orderDetailService.findByOrderId(orderId);
        boolean updated = false;

        for (OrderDetail existing : existingDetails) {
            if (existing.getProductDetail().getId().equals(productDetailId)) {
                int newQuantity = existing.getQuantity() + detail.getQuantity();
                if (productDetail.getStockQuantity() < newQuantity) {
                    return "redirect:/acvstore/hoadon/" + orderId + "?error=insufficientStock";
                }
                existing.setQuantity(newQuantity);
                orderDetailService.save(existing);
                updated = true;
                break;
            }
        }

        if (!updated) {
            detail.setOrder(order);
            detail.setProductDetail(productDetail);
            detail.setPrice(productDetail.getPrice());
            orderDetailService.save(detail);
        }

        productDetail.setStockQuantity(productDetail.getStockQuantity() - detail.getQuantity());
        productService.saveProductDetail(productDetail);
        orderService.updateOrderTotal(orderId);

        return "redirect:/acvstore/hoadon/" + orderId;
    }

    @GetMapping("/hoadon/delete-detail/{id}")
    public String deleteOrderDetail(@PathVariable Integer id) {
        OrderDetail detail = orderDetailService.findById(id);
        if (detail == null) {
            return "redirect:/acvstore/hoadon";
        }
        Integer orderId = detail.getOrder().getId();
        ProductDetail productDetail = detail.getProductDetail();

        productDetail.setStockQuantity(productDetail.getStockQuantity() + detail.getQuantity());
        productService.saveProductDetail(productDetail);

        orderDetailService.delete(id);
        orderService.updateOrderTotal(orderId);

        return "redirect:/acvstore/hoadon/" + orderId;
    }

    @GetMapping("/hoadon/delete/{id}")
    public String deleteInvoice(@PathVariable("id") Integer id) {
        Order order = orderService.findById(id);
        if (order != null) {
            orderService.deleteOrder(id);
        }
        return "redirect:/acvstore/hoadon";
    }

    @GetMapping("/hoadon/create")
    public String showCreateInvoiceForm(Model model) {
        model.addAttribute("order", new Order());
        model.addAttribute("users", userService.getAllUsersByCustomer());
        return "WebQuanLy/hoadon_create";
    }

    @PostMapping("/hoadon/save")
    public String saveInvoice(@ModelAttribute("order") Order order) {
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalPrice(BigDecimal.ZERO);
        orderService.saveOrder(order);
        return "redirect:/acvstore/hoadon";
    }

    @GetMapping("/hoadon/confirm/{id}")
    public String confirmOrder(@PathVariable("id") Integer id) {
        Order order = orderService.findById(id);
        if (order != null && "Pending".equalsIgnoreCase(order.getStatus())) {
            order.setStatus("Delivering");
            orderService.saveOrder(order);
        }
        return "redirect:/acvstore/hoadon";
    }

    @GetMapping("/hoadon/cancel/{id}")
    public String cancelOrder(@PathVariable("id") Integer id) {
        Order order = orderService.findById(id);
        if (order != null && "Pending".equalsIgnoreCase(order.getStatus())) {
            order.setStatus("Cancelled");
            orderService.saveOrder(order);
        }
        return "redirect:/acvstore/hoadon";
    }

    @PostMapping("/api/invoice/confirm")
    @ResponseBody
    public Map<String, Object> confirmInvoice(@RequestBody Map<String, Integer> request) {
        Integer orderId = request.get("invoiceId");
        Map<String, Object> response = new HashMap<>();
        try {
            if (orderId == null) {
                response.put("success", false);
                response.put("message", "ID hóa đơn không hợp lệ");
                return response;
            }
            Order order = orderService.findById(orderId);
            if (order != null && "Pending".equalsIgnoreCase(order.getStatus())) {
                order.setStatus("Delivering");
                orderService.saveOrder(order);
                response.put("success", true);
                response.put("status", order.getStatusDisplay());
            } else {
                response.put("success", false);
                response.put("message", "Không thể xác nhận hóa đơn trong trạng thái hiện tại");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/api/invoice/cancel")
    @ResponseBody
    public Map<String, Object> cancelInvoice(@RequestBody Map<String, Integer> request) {
        Integer orderId = request.get("invoiceId");
        Map<String, Object> response = new HashMap<>();
        try {
            if (orderId == null) {
                response.put("success", false);
                response.put("message", "ID hóa đơn không hợp lệ");
                return response;
            }
            Order order = orderService.findById(orderId);
            if (order != null && "Pending".equalsIgnoreCase(order.getStatus())) {
                order.setStatus("Cancelled");
                orderService.saveOrder(order);
                response.put("success", true);
                response.put("status", order.getStatusDisplay());
            } else {
                response.put("success", false);
                response.put("message", "Không thể hủy hóa đơn trong trạng thái hiện tại");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/api/invoice/mark-paid")
    @ResponseBody
    public Map<String, Object> markAsPaid(@RequestBody Map<String, Integer> request) {
        Integer orderId = request.get("invoiceId");
        Map<String, Object> response = new HashMap<>();
        try {
            if (orderId == null) {
                response.put("success", false);
                response.put("message", "ID hóa đơn không hợp lệ");
                return response;
            }
            Order order = orderService.findById(orderId);
            if (order != null && !("Completed".equalsIgnoreCase(order.getStatus()) || "Cancelled".equalsIgnoreCase(order.getStatus()))) {
                order.setStatus("Completed");
                orderService.saveOrder(order);
                response.put("success", true);
                response.put("status", order.getStatusDisplay());
            } else {
                response.put("success", false);
                response.put("message", "Không thể đánh dấu đã thanh toán trong trạng thái hiện tại");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/api/invoice/details/{id}")
    @ResponseBody
    public Map<String, Object> getInvoiceDetails(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (id == null) {
                response.put("success", false);
                response.put("message", "ID hóa đơn không hợp lệ");
                return response;
            }
            Order order = orderService.findById(id);
            if (order == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy hóa đơn");
                return response;
            }

            Map<String, Object> invoice = new HashMap<>();
            invoice.put("id", order.getId());
            invoice.put("code", order.getCode());
            invoice.put("customer_name", order.getUser() != null ? order.getUser().getFullName() : "Không xác định");
            invoice.put("created_date", order.getCreatedAt() != null ? order.getCreatedAt().toString() : "");
            invoice.put("delivery_method", order.getDeliveryMethod());
            invoice.put("status", order.getStatusDisplay());
            invoice.put("subtotal", orderService.calculateTotalPriceByOrderId(id).toString());
            invoice.put("shipping_fee", order.getShippingFee() != null ? order.getShippingFee().toString() : "0");
            invoice.put("total_amount", order.getTotalPrice() != null ? order.getTotalPrice().toString() : "0");
            invoice.put("payment_method", order.getPaymentMethod());
            invoice.put("payment_status", "Completed".equalsIgnoreCase(order.getStatus()) ? "Đã thanh toán" : "Chưa thanh toán");

            List<OrderDetail> orderDetails = orderDetailService.findByOrderId(id);
            List<Map<String, Object>> products = new ArrayList<>();
            for (OrderDetail detail : orderDetails) {
                Map<String, Object> product = new HashMap<>();
                product.put("productName", detail.getProductName());
                product.put("quantity", detail.getQuantity());
                product.put("price", detail.getPrice().toString());
                BigDecimal total = detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
                product.put("total", total.toString());
                Map<String, Object> productDetail = new HashMap<>();
                productDetail.put("color", detail.getProductDetail().getColor() != null ? detail.getProductDetail().getColor().getName() : "N/A");
                productDetail.put("size", detail.getProductDetail().getSize() != null ? detail.getProductDetail().getSize().getName() : "N/A");
                product.put("productDetail", productDetail);
                products.add(product);
            }
            invoice.put("products", products);

            response.put("success", true);
            response.put("invoice", invoice);
        } catch (Exception e) {
            logger.error("Error fetching invoice details for ID: {}", id, e);
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/hoadon/download-receipt/{orderCode}")
    public ResponseEntity<ByteArrayResource> downloadReceipt(@PathVariable String orderCode) {
        try {
            // Lấy đơn hàng theo mã
            Order order = orderService.findByCode(orderCode);
            if (order == null) {
                logger.error("Không tìm thấy đơn hàng: orderCode={}", orderCode);
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
                logger.info("Font DejavuSans loaded successfully.");
            } catch (Exception e) {
                logger.error("Failed to load font DejavuSans.ttf: {}", e.getMessage(), e);
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

            // Lưu ý: InvoiceController không có thông tin discount trong session
            // Giả sử giảm giá được lưu trong Order hoặc tính toán lại nếu cần
            BigDecimal discount = BigDecimal.ZERO; // Có thể cần điều chỉnh logic này
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
                logger.error("Lỗi khi tạo mã QR: {}", e.getMessage(), e);
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
            logger.error("Lỗi khi tạo PDF cho đơn hàng {}: {}", orderCode, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ByteArrayResource(new byte[0]));
        }
    }
}