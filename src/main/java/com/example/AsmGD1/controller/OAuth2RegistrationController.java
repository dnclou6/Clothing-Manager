package com.example.AsmGD1.controller;

import com.example.AsmGD1.entity.User;
import com.example.AsmGD1.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OAuth2RegistrationController {
    private final UserRepository userRepository;
    private final HttpSession session;

    public OAuth2RegistrationController(UserRepository userRepository, HttpSession session) {
        this.userRepository = userRepository;
        this.session = session;
    }

    @GetMapping("/oauth2/register")
    public String showRegistrationForm(Model model) {
        User user = (User) session.getAttribute("pendingUser");
        if (user == null) {
            return "redirect:/"; // Nếu không có user, về trang chủ
        }
        model.addAttribute("user", user);
        return "oauth-register";

    }

    @PostMapping("/oauth2/register")
    public String completeRegistration(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address) {

        User user = (User) session.getAttribute("pendingUser");
        if (user == null) {
            return "redirect:/";
        }

        // Kiểm tra xem username đã tồn tại chưa
        if (userRepository.findByUsername(username).isPresent()) {
            return "redirect:/oauth2/register?error=Username already exists";
        }

        user.setUsername(username);
        user.setPassword(password);
        user.setPhone(phone);
        user.setAddress(address);

        userRepository.save(user);
        session.removeAttribute("pendingUser"); // Xóa khỏi session sau khi hoàn tất

        return "redirect:/";
    }

}