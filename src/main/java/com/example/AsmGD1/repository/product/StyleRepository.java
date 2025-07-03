package com.example.AsmGD1.repository.product;

import com.example.AsmGD1.entity.product.Style;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StyleRepository extends JpaRepository<Style, Integer> {
    @Query("SELECT s FROM Style s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Style> findByNameContainingIgnoreCase(String name);
}