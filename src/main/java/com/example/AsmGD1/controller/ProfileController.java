package com.example.AsmGD1.controller;

import com.example.AsmGD1.entity.User;
import com.example.AsmGD1.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra nếu user đăng nhập bằng OAuth2
        if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email"); // Lấy email từ OAuth2 response
            Optional<User> userOptional = userRepository.findByEmail(email); // Tìm user bằng email
            userOptional.ifPresent(user -> model.addAttribute("user", user));
        } else {
            String username = authentication.getName(); // Nếu không phải OAuth2 thì dùng username bình thường
            Optional<User> userOptional = userRepository.findByUsername(username);
            userOptional.ifPresent(user -> model.addAttribute("user", user));
        }

        return "profile";
    }


    @GetMapping("/profile/edit")
    public String editProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Optional<User> userOptional = Optional.empty();

        if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email"); // Lấy email từ OAuth2
            userOptional = userRepository.findByEmail(email); // Tìm user theo email
        } else {
            String username = authentication.getName();
            userOptional = userRepository.findByUsername(username);
        }

        if (userOptional.isPresent()) {
            model.addAttribute("user", userOptional.get());
            return "update_profile"; // Trả về trang chỉnh sửa hồ sơ
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("user") User updatedUser, @RequestParam("password") String password) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Optional<User> userOptional = Optional.empty();

        if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            userOptional = userRepository.findByEmail(email);
        } else {
            String username = authentication.getName();
            userOptional = userRepository.findByUsername(username);
        }

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setFullName(updatedUser.getFullName());
            user.setPhone(updatedUser.getPhone());
            user.setAddress(updatedUser.getAddress());

            // Chỉ cập nhật email và password nếu user đăng nhập bằng tài khoản thường
            if (!(authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User)) {
                user.setEmail(updatedUser.getEmail());
                user.setPassword(password); // Không mã hóa theo yêu cầu
            }

            userRepository.save(user);
        }
        return "redirect:/profile";
    }

}
