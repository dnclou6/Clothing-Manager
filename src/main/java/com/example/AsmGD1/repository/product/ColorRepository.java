package com.example.AsmGD1.repository.product;

import com.example.AsmGD1.entity.product.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColorRepository extends JpaRepository<Color, Integer> {
    @Query("SELECT c FROM Color c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Color> findByNameContainingIgnoreCase(String name);
}