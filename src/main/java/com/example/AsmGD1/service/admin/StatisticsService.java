package com.example.AsmGD1.service.admin;

import com.example.AsmGD1.dto.LowStockProductDTO;
import com.example.AsmGD1.dto.ProductStatisticsDTO;
import com.example.AsmGD1.dto.StatisticsDataDTO;
import com.example.AsmGD1.repository.OrderDetailRepository;
import com.example.AsmGD1.repository.OrderRepository;
import com.example.AsmGD1.repository.admin.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;

    public StatisticsDataDTO getStatistics(LocalDate fromDate, LocalDate toDate) {
        StatisticsDataDTO dto = new StatisticsDataDTO();
        LocalDate today = LocalDate.now();
        Locale locale = new Locale("vi", "VN");
        NumberFormat formatter = NumberFormat.getNumberInstance(locale);

        // Doanh số và số đơn hôm nay
        Double todayRevenue = orderRepository.getRevenueBetween(today, today);
        Long todayOrderCount = orderRepository.countCompletedOrdersBetween(today, today);
        dto.setTodayOrderInfo(todayOrderCount + " đơn hàng / " + formatCurrency(todayRevenue));

        // Doanh số và số đơn trong khoảng
        Double revenue = orderRepository.getRevenueBetween(fromDate, toDate);
        Long orderCount = orderRepository.countCompletedOrdersBetween(fromDate, toDate);
        Integer soldQuantity = orderRepository.getSoldQuantityBetween(fromDate, toDate);
        dto.setOrderInfo(orderCount + " đơn hàng / " + formatCurrency(revenue));
        dto.setSoldQuantity(soldQuantity != null ? soldQuantity : 0);

        // Dữ liệu biểu đồ
        List<Map<String, Object>> chartData = orderRepository.getOrderProductStatsBetween(fromDate, toDate);
        List<String> labelsList = new ArrayList<>();
        List<Integer> ordersList = new ArrayList<>();
        List<Integer> productsList = new ArrayList<>();

        // Đảm bảo tất cả các ngày trong khoảng đều có dữ liệu
        LocalDate currentDate = fromDate;
        Map<LocalDate, Map<String, Object>> chartDataMap = new HashMap<>();
        for (Map<String, Object> row : chartData) {
            LocalDate day = LocalDate.parse(row.get("day").toString());
            chartDataMap.put(day, row);
        }

        while (!currentDate.isAfter(toDate)) {
            labelsList.add(currentDate.format(DateTimeFormatter.ofPattern("dd/MM")));
            Map<String, Object> row = chartDataMap.getOrDefault(currentDate, null);
            ordersList.add(row != null ? ((Number) row.get("order_count")).intValue() : 0);
            productsList.add(row != null ? ((Number) row.get("product_count")).intValue() : 0);
            currentDate = currentDate.plusDays(1);
        }

        dto.setChartLabels(labelsList);
        dto.setChartOrders(ordersList);
        dto.setChartProducts(productsList);

        // Trạng thái đơn hàng
        Map<String, Double> statusMap = getOrderStatusPercentBetween(fromDate, toDate);
        dto.setPaidPercent(statusMap.getOrDefault("Paid", 0.0));
        dto.setConfirmedPercent(statusMap.getOrDefault("Confirmed", 0.0));
        dto.setPendingInvoicePercent(statusMap.getOrDefault("Pending", 0.0));
        dto.setWaitingShipPercent(statusMap.getOrDefault("WaitingShip", 0.0));
        dto.setWaitingConfirmPercent(statusMap.getOrDefault("WaitingConfirm", 0.0));
        dto.setShippingPercent(statusMap.getOrDefault("Shipping", 0.0));
        dto.setCancelledPercent(statusMap.getOrDefault("Cancelled", 0.0));
        dto.setReturnedPercent(statusMap.getOrDefault("Returned", 0.0));
        dto.setSuccessPercent(statusMap.getOrDefault("Completed", 0.0));

        // Tăng trưởng
        Double dailyRevenue = todayRevenue;
        Double yesterdayRevenue = orderRepository.getRevenueBetween(today.minusDays(1), today.minusDays(1));
        dto.setDailyRevenue(formatCurrency(dailyRevenue));
        dto.setDailyGrowth(calculateGrowth(dailyRevenue, yesterdayRevenue));

        Double monthlyRevenue = orderRepository.getRevenueBetween(today.withDayOfMonth(1), today);
        Double lastMonthRevenue = orderRepository.getRevenueBetween(
                today.minusMonths(1).withDayOfMonth(1),
                today.minusMonths(1).withDayOfMonth(today.minusMonths(1).lengthOfMonth()));
        dto.setMonthlyRevenue(formatCurrency(monthlyRevenue));
        dto.setMonthlyGrowth(calculateGrowth(monthlyRevenue, lastMonthRevenue));

        Double yearlyRevenue = orderRepository.getRevenueBetween(today.withDayOfYear(1), today);
        Double lastYearRevenue = orderRepository.getRevenueBetween(
                today.minusYears(1).withDayOfYear(1),
                today.minusYears(1).withDayOfYear(today.minusYears(1).lengthOfYear()));
        dto.setYearlyRevenue(formatCurrency(yearlyRevenue));
        dto.setYearlyGrowth(calculateGrowth(yearlyRevenue, lastYearRevenue));

        Integer monthlyProductCount = orderRepository.getSoldQuantityBetween(today.withDayOfMonth(1), today);
        Integer lastMonthProductCount = orderRepository.getSoldQuantityBetween(
                today.minusMonths(1).withDayOfMonth(1),
                today.minusMonths(1).withDayOfMonth(today.minusMonths(1).lengthOfMonth()));
        dto.setMonthlyProductCount(monthlyProductCount != null ? monthlyProductCount : 0);
        dto.setMonthlyProductGrowth(calculateGrowth(
                monthlyProductCount != null ? (double) monthlyProductCount : 0.0,
                lastMonthProductCount != null ? (double) lastMonthProductCount : 0.0));

        Long dailyOrderCount = todayOrderCount;
        Long yesterdayOrderCount = orderRepository.countCompletedOrdersBetween(today.minusDays(1), today.minusDays(1));
        dto.setDailyOrderCount(dailyOrderCount);
        dto.setDailyOrderGrowth(calculateGrowth(dailyOrderCount.doubleValue(), yesterdayOrderCount.doubleValue()));

        Long monthlyOrderCount = orderRepository.countCompletedOrdersBetween(today.withDayOfMonth(1), today);
        Long lastMonthOrderCount = orderRepository.countCompletedOrdersBetween(
                today.minusMonths(1).withDayOfMonth(1),
                today.minusMonths(1).withDayOfMonth(today.minusMonths(1).lengthOfMonth()));
        dto.setMonthlyOrderCount(monthlyOrderCount);
        dto.setMonthlyOrderGrowth(calculateGrowth(monthlyOrderCount.doubleValue(), lastMonthOrderCount.doubleValue()));

        // Top sản phẩm bán chạy và sắp hết hàng
        dto.setTopSellingProducts(getTopSellingProductDetails(fromDate, toDate));
        dto.setLowStockProducts(getLowStockProductDetails());

        return dto;
    }

    private String formatCurrency(Double amount) {
        if (amount == null) return "0 VND";
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + " VND";
    }

    public Map<String, Double> getOrderStatusPercentBetween(LocalDate start, LocalDate end) {
        List<Object[]> result = orderRepository.getOrderStatusCountsBetween(start, end);
        long total = result.stream().mapToLong(row -> ((Number) row[1]).longValue()).sum();

        Map<String, Double> percentMap = new HashMap<>();
        for (Object[] row : result) {
            String status = (String) row[0];
            long count = ((Number) row[1]).longValue();
            double percent = total == 0 ? 0.0 : ((double) count * 100 / total);
            percentMap.put(status, Math.round(percent * 10.0) / 10.0);
        }
        return percentMap;
    }

    public double calculateGrowth(Double current, Double previous) {
        if (previous == null || previous == 0) return current != null && current > 0 ? 100.0 : 0.0;
        return Math.round(((current - previous) / previous) * 1000) / 10.0;
    }

    public List<ProductStatisticsDTO> getTopSellingProductDetails(LocalDate fromDate, LocalDate toDate) {
        List<Object[]> rawData = orderDetailRepository.findTopSellingProductDetailsRaw(fromDate, toDate);
        List<ProductStatisticsDTO> result = new ArrayList<>();

        for (Object[] row : rawData) {
            ProductStatisticsDTO dto = new ProductStatisticsDTO();
            dto.setProductDetailId((Integer) row[0]);
            dto.setProductName((String) row[1]);
            dto.setColor((String) row[2]);
            dto.setSize((String) row[3]);
            dto.setPrice(row[4] != null ? ((Number) row[4]).doubleValue() : 0.0);
            dto.setTotalSold(((Number) row[5]).intValue());
            dto.setImageUrl((String) row[6]);
            result.add(dto);
        }
        return result;
    }

    public List<LowStockProductDTO> getLowStockProductDetails() {
        List<Object[]> rawData = orderDetailRepository.findLowStockProductDetailsRaw();
        List<LowStockProductDTO> result = new ArrayList<>();

        for (Object[] row : rawData) {
            LowStockProductDTO dto = new LowStockProductDTO();
            dto.setProductDetailId((Integer) row[0]);
            dto.setProductName((String) row[1]);
            dto.setColor((String) row[2]);
            dto.setSize((String) row[3]);
            dto.setPrice(row[4] != null ? ((Number) row[4]).doubleValue() : 0.0);
            dto.setStockQuantity((Integer) row[5]);
            dto.setImageUrl((String) row[6]);
            result.add(dto);
        }
        return result;
    }
}