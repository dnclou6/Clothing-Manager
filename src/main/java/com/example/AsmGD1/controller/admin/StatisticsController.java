package com.example.AsmGD1.controller.admin;

import com.example.AsmGD1.dto.StatisticsDataDTO;
import com.example.AsmGD1.service.admin.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/acvstore")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/thongke")
    public String showStatistics(
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Model model) {

        LocalDate today = LocalDate.now();
        LocalDate fromDate = today;
        LocalDate toDate = today;
        String filterStatus = "Chưa chọn bộ lọc";

        // Xác thực và xử lý bộ lọc
        try {
            if (filter == null) {
                // Không có filter, sử dụng mặc định nhưng không highlight
                fromDate = today.withDayOfMonth(1);
                toDate = today.withDayOfMonth(today.lengthOfMonth());
            } else {
                switch (filter) {
                    case "day":
                        fromDate = toDate = today;
                        filterStatus = "Hôm nay: " + today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        break;
                    case "7days":
                        fromDate = today.minusDays(6);
                        toDate = today;
                        filterStatus = "7 ngày qua: " + fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                                " - " + toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        break;
                    case "month":
                        if (month == null) month = today.getMonthValue();
                        if (year == null) year = today.getYear();
                        if (month < 1 || month > 12 || year < 2000 || year > today.getYear() + 1) {
                            throw new IllegalArgumentException("Tháng hoặc năm không hợp lệ.");
                        }
                        fromDate = LocalDate.of(year, month, 1);
                        toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());
                        filterStatus = "Tháng " + month + "/" + year;
                        break;
                    case "year":
                        if (year == null) year = today.getYear();
                        if (year < 2000 || year > today.getYear() + 1) {
                            throw new IllegalArgumentException("Năm không hợp lệ.");
                        }
                        fromDate = LocalDate.of(year, 1, 1);
                        toDate = LocalDate.of(year, 12, 31);
                        filterStatus = "Năm " + year;
                        break;
                    case "custom_range":
                        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
                            throw new IllegalArgumentException("Khoảng thời gian không hợp lệ.");
                        }
                        fromDate = startDate;
                        toDate = endDate;
                        filterStatus = "Từ " + startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                                " đến " + endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        break;
                    default:
                        // Mặc định là tháng hiện tại nếu filter không hợp lệ
                        fromDate = today.withDayOfMonth(1);
                        toDate = today.withDayOfMonth(today.lengthOfMonth());
                        filterStatus = "Tháng " + today.getMonthValue() + "/" + today.getYear();
                        break;
                }
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            fromDate = today.withDayOfMonth(1);
            toDate = today.withDayOfMonth(today.lengthOfMonth());
            filterStatus = "Tháng " + today.getMonthValue() + "/" + today.getYear() + " (mặc định do lỗi)";
        }

        // Lấy dữ liệu thống kê từ Service
        StatisticsDataDTO stats = statisticsService.getStatistics(fromDate, toDate);

        // Thêm dữ liệu vào model
        model.addAttribute("filter", filter); // Để JavaScript kiểm tra filter
        model.addAttribute("todayOrderInfo", stats.getTodayOrderInfo());
        model.addAttribute("monthlyOrderInfo", stats.getOrderInfo());
        model.addAttribute("monthlySoldQuantity", stats.getSoldQuantity() + " sản phẩm");
        model.addAttribute("chartLabels", stats.getChartLabels());
        model.addAttribute("chartOrders", stats.getChartOrders());
        model.addAttribute("chartProducts", stats.getChartProducts());
        model.addAttribute("paidPercent", stats.getPaidPercent());
        model.addAttribute("confirmedPercent", stats.getConfirmedPercent());
        model.addAttribute("pendingInvoicePercent", stats.getPendingInvoicePercent());
        model.addAttribute("waitingShipPercent", stats.getWaitingShipPercent());
        model.addAttribute("waitingConfirmPercent", stats.getWaitingConfirmPercent());
        model.addAttribute("shippingPercent", stats.getShippingPercent());
        model.addAttribute("cancelledPercent", stats.getCancelledPercent());
        model.addAttribute("returnedPercent", stats.getReturnedPercent());
        model.addAttribute("successPercent", stats.getSuccessPercent());
        model.addAttribute("dailyRevenue", stats.getDailyRevenue());
        model.addAttribute("dailyGrowth", stats.getDailyGrowth());
        model.addAttribute("monthlyRevenue", stats.getMonthlyRevenue());
        model.addAttribute("monthlyGrowth", stats.getMonthlyGrowth());
        model.addAttribute("yearlyRevenue", stats.getYearlyRevenue());
        model.addAttribute("yearlyGrowth", stats.getYearlyGrowth());
        model.addAttribute("monthlyProductCount", stats.getMonthlyProductCount());
        model.addAttribute("monthlyProductGrowth", stats.getMonthlyProductGrowth());
        model.addAttribute("dailyOrderCount", stats.getDailyOrderCount());
        model.addAttribute("dailyOrderGrowth", stats.getDailyOrderGrowth());
        model.addAttribute("monthlyOrderCount", stats.getMonthlyOrderCount());
        model.addAttribute("monthlyOrderGrowth", stats.getMonthlyOrderGrowth());
        model.addAttribute("topSellingProducts", stats.getTopSellingProducts());
        model.addAttribute("lowStockProducts", stats.getLowStockProducts());
        model.addAttribute("filterStatus", filterStatus);

        return "WebQuanLy/statistics";
    }
}