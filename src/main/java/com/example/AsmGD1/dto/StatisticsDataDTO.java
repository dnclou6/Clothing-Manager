package com.example.AsmGD1.dto;

import lombok.Data;

import java.util.List;

@Data
public class StatisticsDataDTO {
    private String todayOrderInfo;
    private String orderInfo;
    private Integer soldQuantity;
    private List<String> chartLabels;
    private List<Integer> chartOrders;
    private List<Integer> chartProducts;
    private Double paidPercent;
    private Double confirmedPercent;
    private Double pendingInvoicePercent;
    private Double waitingShipPercent;
    private Double waitingConfirmPercent;
    private Double shippingPercent;
    private Double cancelledPercent;
    private Double returnedPercent;
    private Double successPercent;
    private String dailyRevenue;
    private Double dailyGrowth;
    private String monthlyRevenue;
    private Double monthlyGrowth;
    private String yearlyRevenue;
    private Double yearlyGrowth;
    private Integer monthlyProductCount;
    private Double monthlyProductGrowth;
    private Long dailyOrderCount;
    private Double dailyOrderGrowth;
    private Long monthlyOrderCount;
    private Double monthlyOrderGrowth;
    private List<ProductStatisticsDTO> topSellingProducts;
    private List<LowStockProductDTO> lowStockProducts;
}