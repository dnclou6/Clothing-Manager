package com.example.AsmGD1.repository.product;

import com.example.AsmGD1.entity.product.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Integer> {
    @Query("SELECT m FROM Material m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Material> findByNameContainingIgnoreCase(String name);
}