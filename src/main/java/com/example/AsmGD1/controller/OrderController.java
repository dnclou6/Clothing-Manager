//package com.example.AsmGD1.controller;
//
//import com.example.AsmGD1.entity.Order;
//import com.example.AsmGD1.service.OrderService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Optional;
//
//@Controller
//@RequestMapping("/orders")
//public class OrderController {
//    @Autowired
//    private OrderService orderService;
//
//    @GetMapping
//    public String listOrders(Model model) {
//        List<Order> orders = orderService.getAllOrders();
//        model.addAttribute("orders", orders);
//        return "orders";
//    }
//
//    @GetMapping("/{id}")
//    public String getOrderDetail(@PathVariable Integer id, Model model) {
//        Optional<Order> order = orderService.getOrderById(id);
//        order.ifPresent(value -> model.addAttribute("order", value));
//        return order.isPresent() ? "order_detail" : "redirect:/orders";
//    }
//
//    @PostMapping("/save")
//    public String saveOrder(@ModelAttribute Order order) {
//        orderService.saveOrder(order);
//        return "redirect:/orders";
//    }
//
//    @GetMapping("/delete/{id}")
//    public String deleteOrder(@PathVariable Integer id) {
//        orderService.deleteOrder(id);
//        return "redirect:/orders";
//    }
//}
