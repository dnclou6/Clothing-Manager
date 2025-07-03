package com.example.AsmGD1.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class FaceVerificationInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        String path = request.getRequestURI();

        // Không chặn trang login, verify, logout
        if (path.startsWith("/acvstore/login") || path.startsWith("/acvstore/verify-face") || path.startsWith("/logout")) {
            return true;
        }

        // Nếu là admin chưa xác thực khuôn mặt thì chuyển hướng
        if (session != null && "ADMIN".equals(session.getAttribute("ROLE"))) {
            Boolean verified = (Boolean) session.getAttribute("faceVerified");
            if (verified == null || !verified) {
                response.sendRedirect("/acvstore/verify-face");
                return false;
            }
        }

        return true;
    }
}
