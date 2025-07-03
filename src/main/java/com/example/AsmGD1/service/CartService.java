////package com.example.AsmGD1.service;
////
////import com.example.AsmGD1.entity.Cart;
////import com.example.AsmGD1.entity.Product;
////import com.example.AsmGD1.entity.Order;
////import com.example.AsmGD1.entity.OrderDetail;
////import com.example.AsmGD1.repository.CartRepository;
////import com.example.AsmGD1.repository.OrderDetailRepository;
////import com.example.AsmGD1.repository.OrderRepository;
////import org.springframework.stereotype.Service;
////
////import java.util.ArrayList;
////import java.util.List;
////import java.util.Optional;
////
////@Service
////public class CartService {
////
////    private final CartRepository cartRepository;
////    private final OrderDetailRepository orderDetailRepository;
////    private final OrderRepository orderRepository;
////
////    public CartService(CartRepository cartRepository, OrderDetailRepository orderDetailRepository, OrderRepository orderRepository) {
////        this.cartRepository = cartRepository;
////        this.orderDetailRepository = orderDetailRepository;
////        this.orderRepository = orderRepository;
////    }
////
////    private final List<Cart> cartItems = new ArrayList<>(); // Lưu giỏ hàng tạm thời
////
////    public List<Cart> addToCart(Product product) {
////        boolean exists = false;
////        for (Cart item : cartItems) {
////            if (item.getProduct().getId().equals(product.getId())) {
////                item.setQuantity(item.getQuantity() + 1);
////                exists = true;
////                break;
////            }
////        }
////        if (!exists) {
////            Cart newItem = new Cart();
////            newItem.setProduct(product);
////            newItem.setQuantity(1);
////            cartItems.add(newItem);
////        }
////        return cartItems;
////    }
////
////    public void updateCart(Integer cartId, Integer quantity) {
////        for (Cart item : cartItems) {
////            if (item.getId().equals(cartId)) {
////                item.setQuantity(quantity);
////                return;
////            }
////        }
////        throw new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng");
////    }
////
////
////    public void deleteCartItem(Integer id) {
////        this.cartRepository.deleteById(id);
////    }
////
////
////
////
////    public List<Cart> getCart() {
////        return new ArrayList<>(cartItems);
////    }
////
////    public double calculateTotal(List<Cart> cartItems) {
////        return cartItems.stream().mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity()).sum();
////    }
////
////    public void clearCart() {
////        cartItems.clear();
////    }
////
////    public void checkout() {
////        if (cartItems.isEmpty()) {
////            throw new RuntimeException("Giỏ hàng trống");
////        }
////
////        Order order = new Order();
////        order.setTotalPrice(calculateTotal(cartItems));
////        order.setStatus("Pending");
////        orderRepository.save(order);
////
////        for (Cart cartItem : cartItems) {
////            OrderDetail orderDetail = new OrderDetail();
////            orderDetail.setOrder(order);
////            orderDetail.setProduct(cartItem.getProduct());
////            orderDetail.setQuantity(cartItem.getQuantity());
////            orderDetail.setPrice(cartItem.getProduct().getPrice() * cartItem.getQuantity());
////            orderDetailRepository.save(orderDetail);
////        }
////
////        clearCart();
////    }
////}
//
//package com.example.AsmGD1.service;
//
//import com.example.AsmGD1.entity.*;
//import com.example.AsmGD1.repository.*;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//@Service
//public class CartService {
//
//    private final CartRepository cartRepository;
//    private final ProductRepository productRepository;
//    private final UserRepository userRepository;
//    private final OrderRepository orderRepository;
//    private final OrderDetailRepository orderDetailRepository;
//
//    public CartService(CartRepository cartRepository, ProductRepository productRepository, UserRepository userRepository, OrderRepository orderRepository, OrderDetailRepository orderDetailRepository) {
//        this.cartRepository = cartRepository;
//        this.productRepository = productRepository;
//        this.userRepository = userRepository;
//        this.orderRepository = orderRepository;
//        this.orderDetailRepository = orderDetailRepository;
//    }
//
//    public List<Cart> getCartByUser() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String identifier = auth.getName(); // Có thể là username hoặc email
//
//        // Nếu đăng nhập bằng OAuth2, lấy email từ attributes
//        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
//            Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
//            identifier = (String) attributes.get("email"); // Thay thế identifier bằng email
//        }
//
//        System.out.println("Fetching cart for user: " + identifier);
//
//        // Tìm user theo email hoặc username
//        Optional<User> userOpt = userRepository.findByEmail(identifier);
//        if (userOpt.isEmpty()) {
//            userOpt = userRepository.findByUsername(identifier); // Thử tìm bằng username nếu không tìm thấy bằng email
//        }
//
//        if (userOpt.isEmpty()) {
//            System.out.println("User not found: " + identifier);
//            return List.of();
//        }
//
//        return cartRepository.findByUser(userOpt.get());
//    }
//
//
//    public void addToCart(Integer productId, int quantity) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String identifier = auth.getName(); // Có thể là username hoặc email
//
//        // Nếu đăng nhập bằng OAuth2, lấy email từ attributes
//        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
//            Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
//            identifier = (String) attributes.get("email"); // Thay thế identifier bằng email
//        }
//
//        System.out.println("Adding to cart for user: " + identifier);
//
//        // Tìm user theo email hoặc username
//        Optional<User> userOpt = userRepository.findByEmail(identifier);
//        if (userOpt.isEmpty()) {
//            userOpt = userRepository.findByUsername(identifier); // Thử tìm bằng username nếu không tìm thấy bằng email
//        }
//
//        if (userOpt.isEmpty()) {
//            System.out.println("User not found: " + identifier);
//            return; // Thoát nếu không tìm thấy user
//        }
//
//        User user = userOpt.get();
//        Optional<Product> productOpt = productRepository.findById(productId);
//        if (productOpt.isEmpty()) {
//            System.out.println("Product not found: " + productId);
//            return; // Thoát nếu không tìm thấy sản phẩm
//        }
//
//        Product product = productOpt.get();
//        Optional<Cart> existingCartItem = cartRepository.findByUserAndProduct(user, product);
//
//        if (existingCartItem.isPresent()) {
//            Cart cartItem = existingCartItem.get();
//            cartItem.setQuantity(cartItem.getQuantity() + quantity);
//            cartRepository.save(cartItem);
//            System.out.println("Updated cart quantity: " + cartItem.getQuantity());
//        } else {
//            Cart cartItem = new Cart();
//            cartItem.setUser(user);
//            cartItem.setProduct(product);
//            cartItem.setQuantity(quantity);
//            cartRepository.save(cartItem);
//            System.out.println("New cart item added.");
//        }
//    }
//
//
//    public void removeFromCart(Integer cartItemId) {
//        Optional<Cart> cartOpt = cartRepository.findById(cartItemId);
//        if (cartOpt.isPresent()) {
//            Cart cartItem = cartOpt.get();
//            if (cartItem.getQuantity() > 1) {
//                cartItem.setQuantity(cartItem.getQuantity() - 1);
//                cartRepository.save(cartItem);
//            } else {
//                cartRepository.delete(cartItem);
//            }
//        }
//    }
//
//
//    public double calculateTotal(List<Cart> cartItems) {
//        return cartItems.stream().mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity()).sum();
//    }
//
//    private final List<Cart> cartItems = new ArrayList<>(); // Lưu giỏ hàng tạm thời
//
//    @Transactional
//    public void clearCart() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String identifier = auth.getName(); // Có thể là username hoặc email
//
//        // Nếu đăng nhập bằng OAuth2, lấy email từ attributes
//        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
//            Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
//            identifier = (String) attributes.get("email"); // Thay thế identifier bằng email
//        }
//
//        System.out.println("Clearing cart for user: " + identifier);
//
//        // Tìm user theo email hoặc username
//        Optional<User> userOpt = userRepository.findByEmail(identifier);
//        if (userOpt.isEmpty()) {
//            userOpt = userRepository.findByUsername(identifier); // Thử tìm bằng username nếu không tìm thấy bằng email
//        }
//
//        if (userOpt.isEmpty()) {
//            System.out.println("User not found: " + identifier);
//            return; // Không tìm thấy user thì không làm gì cả
//        }
//
//        User user = userOpt.get();
//        cartRepository.deleteByUser(user);
//        System.out.println("Cart cleared for user: " + identifier);
//    }
//
//
//    public void checkout() {
//        if (cartItems.isEmpty()) {
//            throw new RuntimeException("Giỏ hàng trống");
//        }
//
//        Order order = new Order();
//        order.setTotalPrice(calculateTotal(cartItems));
//        order.setStatus("Pending");
//        orderRepository.save(order);
//
//        for (Cart cartItem : cartItems) {
//            OrderDetail orderDetail = new OrderDetail();
//            orderDetail.setOrder(order);
//            orderDetail.setProduct(cartItem.getProduct());
//            orderDetail.setQuantity(cartItem.getQuantity());
//            orderDetail.setPrice(cartItem.getProduct().getPrice() * cartItem.getQuantity());
//            orderDetailRepository.save(orderDetail);
//        }
//
//        clearCart();
//    }
//}
