package com.example.AsmGD1.service;

import com.example.AsmGD1.entity.User;
import com.example.AsmGD1.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final HttpSession session;

    public CustomOAuth2UserService(UserRepository userRepository, HttpSession session) {
        this.userRepository = userRepository;
        this.session = session;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        User user = processOAuth2User(oAuth2User, userRequest.getClientRegistration().getRegistrationId());

        boolean isNewUser = user.getPassword() == null || user.getPhone() == null || user.getAddress() == null;

        if (isNewUser) {
            session.setAttribute("pendingUser", user); // Chỉ lưu session nếu là user mới
        } else {
            session.removeAttribute("pendingUser"); // Xóa nếu user đã có
        }

        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole())),
                oAuth2User.getAttributes(),
                "name"
        );
    }


    private User processOAuth2User(OAuth2User oAuth2User, String provider) {
        String email = oAuth2User.getAttribute("email");
        String id = oAuth2User.getAttribute("id");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new IllegalArgumentException("Email không thể null khi đăng nhập OAuth2");
        }

        // Kiểm tra nếu email đã tồn tại trong database
        Optional<User> existingUserByEmail = userRepository.findByEmail(email);
        if (existingUserByEmail.isPresent()) {
            return existingUserByEmail.get(); // Trả về user cũ nếu đã có email này
        }

        // Kiểm tra nếu username đã tồn tại
        Optional<User> existingUserByUsername = userRepository.findByUsername(email);
        if (existingUserByUsername.isPresent()) {
            return existingUserByUsername.get();
        }

        // Nếu chưa tồn tại, tạo user mới
        User newUser = new User();
        newUser.setUsername(email);
        newUser.setEmail(email);
        newUser.setFullName(name);
        newUser.setRole("CUSTOMER");
        newUser.setCreatedAt(new Date());

        return userRepository.save(newUser);
    }



}
