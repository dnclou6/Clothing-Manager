package com.example.AsmGD1.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HeldOrderDetailDTO {
    private String productName;
    private String color;
    private String size;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subTotal;
}
