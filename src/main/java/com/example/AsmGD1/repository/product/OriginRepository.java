package com.example.AsmGD1.repository.product;

import com.example.AsmGD1.entity.product.Origin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OriginRepository extends JpaRepository<Origin, Integer> {
    @Query("SELECT o FROM Origin o WHERE LOWER(o.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Origin> findByNameContainingIgnoreCase(String name);
}