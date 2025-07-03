package com.example.AsmGD1.repository.product;

import com.example.AsmGD1.entity.product.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SizeRepository extends JpaRepository<Size, Integer> {
    @Query("SELECT s FROM Size s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Size> findByNameContainingIgnoreCase(String name);
}