//package com.example.AsmGD1.controller;
//
//import com.example.AsmGD1.entity.*;
//import com.example.AsmGD1.service.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/checkout")
//public class CheckoutController {
//
//    @Autowired
//    private CartService cartService;
//
//    @Autowired
//    private OrderService orderService;
//
//    @Autowired
//    private OrderDetailService orderDetailService;
//
//    @GetMapping("")
//    public String checkoutPage(Model model) {
//        // Lấy giỏ hàng từ dịch vụ, không cần userId
//        List<Cart> cartItems = cartService.getCartByUser();
//        if (cartItems.isEmpty()) {
//            return "redirect:/cart";
//        }
//
//        double totalPrice = cartService.calculateTotal(cartItems);
//
//        model.addAttribute("cartItems", cartItems);
//        model.addAttribute("totalPrice", totalPrice);
//
//        return "checkout";
//    }
//
//    @PostMapping("/process")
//    public String processCheckout(@RequestParam String name,
//                                  @RequestParam String address,
//                                  @RequestParam String phone) {
//        List<Cart> cartItems = cartService.getCartByUser();
//        if (cartItems.isEmpty()) {
//            return "redirect:/cart";
//        }
//
//        Order order = new Order();
//        order.setTotalPrice(cartService.calculateTotal(cartItems));
//        order.setStatus("Pending");
//        orderService.saveOrder(order);
//
//        for (Cart cartItem : cartItems) {
//            OrderDetail orderDetail = new OrderDetail();
//            orderDetail.setOrder(order);
//            orderDetail.setProduct(cartItem.getProduct());
//            orderDetail.setQuantity(cartItem.getQuantity());
//            orderDetail.setPrice(cartItem.getProduct().getPrice() * cartItem.getQuantity());
//            orderDetailService.saveOrderDetail(orderDetail);
//        }
//
//        cartService.clearCart();
//
//        return "redirect:/checkout/success";
//    }
//
//    @GetMapping("/success")
//    public String checkoutSuccess() {
//        return "checkout_success";
//    }
//}
