package com.example.AsmGD1.config;

import com.example.AsmGD1.entity.User;
import com.example.AsmGD1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalUserAdvice {

    @Autowired
    private UserService userService;

    @ModelAttribute
    public void addUserToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String username = auth.getName(); // get username từ Spring Security
            User user = userService.findByUsername(username); // CHỖ NÀY NÊN CÓ method findByUsername

            System.out.println("✅ USER LOADED: " + user);
            model.addAttribute("user", user);
        }
    }
}
