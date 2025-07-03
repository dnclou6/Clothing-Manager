package com.example.AsmGD1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LowStockProductDTO {
    private Integer productDetailId;
    private String productName;
    private String color;
    private String size;
    private Integer stockQuantity;
    private Double price;
    private String imageUrl;
}