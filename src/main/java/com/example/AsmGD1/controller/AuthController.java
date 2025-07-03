package com.example.AsmGD1.controller;

import com.example.AsmGD1.entity.User;
import com.example.AsmGD1.service.AuthService;
import com.example.AsmGD1.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @GetMapping("/customer/login")
    public String customerLoginPage() {
        return "WebKhachHang/kh_login";
    }

    @PostMapping("/login-success")
    public String loginSuccess() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/acvstore/verify-face";
        } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))) {
            return "redirect:/employee/login";
        }
        return "redirect:/customer/index";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping("/acvstore/login")
    public String adminLoginPage() {
        return "WebQuanLy/admin_login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register/saveUser")
    public String saveUser(@ModelAttribute("user") User user) {
        userService.saveUser(user);
        return "login";
    }

    @GetMapping("/auth/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/auth/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model) {
        try {
            authService.sendOtp(email);
            User user = authService.getUserByEmail(email);
            if (user != null) {
                model.addAttribute("email", email);
                model.addAttribute("otpExpiry", user.getOtpExpiry());
            }
            return "verify-otp";
        } catch (Exception e) {
            model.addAttribute("error", "Gửi OTP thất bại! Lỗi: " + e.getMessage());
            return "forgot-password";
        }
    }

    @PostMapping("/auth/verify-otp")
    public String verifyOtp(@RequestParam String email, @RequestParam String otp, Model model) {
        String result = authService.verifyOtp(email, otp);
        User user = authService.getUserByEmail(email);
        model.addAttribute("email", email);

        if (user != null && user.getOtpExpiry() != null) {
            model.addAttribute("otpExpiry", user.getOtpExpiry());
        }

        if ("expired".equals(result)) {
            model.addAttribute("error", "expired");
            return "verify-otp";
        }
        if ("invalid".equals(result)) {
            model.addAttribute("error", "invalid");
            return "verify-otp";
        }
        if ("valid".equals(result)) {
            return "reset-password";
        }
        model.addAttribute("error", "not_found");
        return "verify-otp";
    }

    @PostMapping("/auth/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false));
        }
        authService.sendOtp(email);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/auth/reset-password")
    public String resetPassword(@RequestParam String email, @RequestParam String password) {
        authService.resetPassword(email, password);
        return "redirect:/login?success";
    }

    @GetMapping("/api/user-info")
    @ResponseBody
    public Map<String, String> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, String> info = new HashMap<>();
        if (userDetails != null) {
            info.put("username", userDetails.getUsername());
            info.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
        } else {
            info.put("role", "GUEST");
        }
        return info;
    }

    @GetMapping("/acvstore/register-face")
    public String showRegisterFacePage(Authentication auth) {
        if (auth == null || !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/acvstore/login?error=access_denied";
        }
        return "WebQuanLy/register-face";
    }

    @PostMapping("/acvstore/register-face")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerFace(@RequestBody Map<String, String> req, Authentication auth) {
        String base64 = req.get("image").replace("data:image/jpeg;base64,", "");
        String username = auth.getName();
        Optional<User> userOpt = authService.findByUsername(username);

        boolean success = userOpt.isPresent() && authService.registerFace(userOpt.get(), base64);
        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "Đăng ký khuôn mặt thành công!" : "Đăng ký khuôn mặt thất bại."
        ));
    }

    @GetMapping("/acvstore/verify-face")
    public String showFaceVerifyPage() {
        return "WebQuanLy/verify-face";
    }

    @PostMapping("/acvstore/verify-face")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyFace(@RequestBody Map<String, String> req, HttpSession session, Authentication auth) {
        String base64 = req.get("image").replace("data:image/jpeg;base64,", "");
        String username = auth.getName();
        boolean match = authService.isFaceMatched(username, base64);

        if (match) {
            session.setAttribute("faceVerified", true);
            session.setAttribute("ROLE", "ADMIN"); // Phục vụ cho interceptor
        }

        return ResponseEntity.ok(Map.of(
                "success", match,
                "message", match ? "Xác thực khuôn mặt thành công" : "Khuôn mặt không khớp"
        ));
    }

}
