    package com.example.AsmGD1.config;

    import com.example.AsmGD1.service.CustomOAuth2UserService;
    import com.example.AsmGD1.service.UserService;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import jakarta.servlet.http.HttpSession;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.core.annotation.Order;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.crypto.password.NoOpPasswordEncoder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

    import java.io.IOException;

    @Configuration
    @EnableWebSecurity
    public class SecurityConfig {

        @Autowired
        private UserService userService;

        @Autowired
        private CustomOAuth2UserService customOAuth2UserService;

        @Bean
        public PasswordEncoder passwordEncoder() {
            return NoOpPasswordEncoder.getInstance();
        }

        // SecurityFilterChain for customers
        @Bean
        @Order(1)
        public SecurityFilterChain customerSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                    .securityMatcher("/customer/**")
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/customer/index", "/customer/login", "/customer/register").permitAll()
                            .requestMatchers("/customer/**").hasRole("CUSTOMER")
                            .anyRequest().authenticated()
                    )
                    .formLogin(login -> login
                            .loginPage("/customer/login")
                            .loginProcessingUrl("/customer/login")
                            .successHandler(customerSuccessHandler())
                            .failureUrl("/customer/login?error=invalid_credentials")
                            .permitAll()
                    )
                    .logout(logout -> logout
                            .logoutUrl("/logout")
                            .logoutSuccessHandler((request, response, authentication) -> {
                                String referer = request.getHeader("Referer");
                                if (referer != null && referer.contains("/acvstore")) {
                                    response.sendRedirect("/acvstore/login");
                                } else {
                                    response.sendRedirect("/customer/login");
                                }
                            })
                            .invalidateHttpSession(true)
                            .deleteCookies("JSESSIONID")
                            .permitAll()
                    )
                    .exceptionHandling(exception -> exception
                            .accessDeniedHandler((request, response, accessDeniedException) -> {
                                response.sendRedirect("/access-denied?loginPage=/customer/login");
                            })
                    );

            return http.build();
        }

        // SecurityFilterChain for admin and employee
        @Bean
        @Order(2)
        public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                    .securityMatcher("/acvstore/**", "/employee/**")
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/acvstore/login", "/acvstore/face-login", "/acvstore/register-face").permitAll()
                            .requestMatchers("/acvstore/**").hasAnyRole("ADMIN", "EMPLOYEE")
                            .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                            .anyRequest().authenticated()
                    )
                    .formLogin(login -> login
                            .loginPage("/acvstore/login")
                            .loginProcessingUrl("/acvstore/login")
                            .successHandler(adminSuccessHandler())
                            .failureUrl("/acvstore/login?error=invalid_credentials")
                            .permitAll()
                    )
                    .logout(logout -> logout
                            .logoutUrl("/logout")
                            .logoutSuccessHandler((request, response, authentication) -> {
                                response.sendRedirect("/acvstore/login");
                            })
                            .invalidateHttpSession(true)
                            .deleteCookies("JSESSIONID")
                            .permitAll()
                    )
                    .exceptionHandling(exception -> exception
                            .accessDeniedHandler((request, response, accessDeniedException) -> {
                                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                                String loginPage = "/customer/login";
                                if (authentication != null && authentication.isAuthenticated()) {
                                    if (authentication.getAuthorities().stream()
                                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_CUSTOMER"))) {
                                        loginPage = "/customer/login";
                                    } else if (authentication.getAuthorities().stream()
                                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_EMPLOYEE"))) {
                                        loginPage = "/acvstore/login";
                                    }
                                }
                                response.sendRedirect("/access-denied?loginPage=" + loginPage);
                            })
                    );

            return http.build();
        }

        // Default SecurityFilterChain for other URLs
        @Bean
        @Order(3)
        public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/register", "/register/**").permitAll()
                            .requestMatchers("/auth/forgot-password", "/auth/verify-otp", "/auth/reset-password", "/auth/resend-otp").permitAll()
                            .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                            .requestMatchers("/api/user-info").permitAll()
                            .requestMatchers("/profile").authenticated()
                            .anyRequest().authenticated()
                    )
                    .oauth2Login(oauth2 -> oauth2
                            .loginPage("/customer/login")
                            .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                            .successHandler((request, response, authentication) -> {
                                HttpSession session = request.getSession();
                                if (session.getAttribute("pendingUser") != null) {
                                    response.sendRedirect("/oauth2/register");
                                } else {
                                    response.sendRedirect("/");
                                }
                            })
                    )
                    .formLogin(login -> login
                            .loginPage("/customer/login")
                            .permitAll()
                    )
                    .logout(logout -> logout
                            .logoutUrl("/logout")
                            .logoutSuccessHandler((request, response, authentication) -> {
                                if (authentication != null && authentication.getAuthorities().stream()
                                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_EMPLOYEE"))) {
                                    response.sendRedirect("/acvstore/login");
                                } else {
                                    response.sendRedirect("/customer/login");
                                }
                            })
                            .invalidateHttpSession(true)
                            .deleteCookies("JSESSIONID")
                            .permitAll()
                    )
                    .exceptionHandling(exception -> exception
                            .accessDeniedHandler((request, response, accessDeniedException) -> {
                                response.sendRedirect("/access-denied?loginPage=/customer/login");
                            })
                    );

            return http.build();
        }

        @Bean
        public AuthenticationSuccessHandler customerSuccessHandler() {
            return new AuthenticationSuccessHandler() {
                @Override
                public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                    Authentication authentication) throws IOException {
                    if (authentication.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_CUSTOMER"))) {
                        response.sendRedirect("/customer/index");
                    } else {
                        SecurityContextHolder.clearContext();
                        response.sendRedirect("/customer/login?error=invalid_role");
                    }
                }
            };
        }

        @Bean
        public AuthenticationSuccessHandler adminSuccessHandler() {
            return new AuthenticationSuccessHandler() {
                @Override
                public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                    Authentication authentication) throws IOException {
                    System.out.println("Authorities: " + authentication.getAuthorities());

                    if (authentication.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                        System.out.println("Đăng nhập thành công với vai trò ADMIN");
                        // ✅ Thay vì vào trang chính → chuyển sang xác thực khuôn mặt
                        response.sendRedirect("/acvstore/verify-face");
                    } else if (authentication.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_EMPLOYEE"))) {
                        System.out.println("Đăng nhập thành công với vai trò EMPLOYEE");
                        response.sendRedirect("/employee/dashboard");
                    } else {
                        System.out.println("Sai vai trò, chuyển hướng đến /acvstore/login?error=invalid_role");
                        SecurityContextHolder.clearContext();
                        response.sendRedirect("/acvstore/login?error=invalid_role");
                    }
                }
            };
        }
    }