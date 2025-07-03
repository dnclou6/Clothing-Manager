//package com.example.AsmGD1.controller;
//
//import com.example.AsmGD1.entity.Cart;
//import com.example.AsmGD1.service.CartService;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/cart")
//public class CartController {
//
//    private final CartService cartService;
//
//    public CartController(CartService cartService) {
//        this.cartService = cartService;
//    }
//
//    @GetMapping
//    public String viewCart(Model model) {
//        List<Cart> cartItems = cartService.getCartByUser();
//        double totalPrice = cartService.calculateTotal(cartItems);
//        model.addAttribute("cartItems", cartItems);
//        model.addAttribute("totalPrice", totalPrice);
//
//        return "cart";
//    }
//
//    @PostMapping("/add/{productId}/{quantity}")
//    public String addToCart(@PathVariable Integer productId, @PathVariable int quantity) {
//        cartService.addToCart(productId, quantity);
//        return "redirect:/cart";
//    }
//
//    @GetMapping("/deleteProductCart/{id}")
//    public String deleteProductCart(@PathVariable("id") Integer id) {
//        cartService.removeFromCart(id);
//        return "redirect:/cart";
//    }
//    @GetMapping("/clear")
//    public String clearCart() {
//        cartService.clearCart();
//        return "redirect:/cart";
//    }
//}
