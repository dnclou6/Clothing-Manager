package com.example.AsmGD1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStatisticsDTO {
    private Integer productDetailId;
    private String productName;
    private String color;
    private String size;
    private Integer totalSold;
    private Double price;        // ✅ thêm trường giá
    private String imageUrl;     // ✅ thêm trường ảnh
}
