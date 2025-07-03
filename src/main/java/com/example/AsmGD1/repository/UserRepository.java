package com.example.AsmGD1.repository;

import com.example.AsmGD1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByIdCard(String idCard);
    List<User> findByRole(String role);
    @Query("SELECT u FROM User u WHERE u.role = 'CUSTOMER'")
    List<User> findAllCustomers();
    List<User> findByRoleAndIsDeletedFalse(String role);
    Optional<User> findByFaceId(String faceId);

}
