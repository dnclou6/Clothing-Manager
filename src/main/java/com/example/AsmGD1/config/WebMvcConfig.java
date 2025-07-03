package com.example.AsmGD1.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private FaceVerificationInterceptor faceVerificationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(faceVerificationInterceptor)
                .addPathPatterns("/acvstore/**")
                .excludePathPatterns("/acvstore/login", "/acvstore/verify-face", "/logout", "/css/**", "/js/**", "/images/**");
    }
}
