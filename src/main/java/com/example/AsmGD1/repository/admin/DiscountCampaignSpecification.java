package com.example.AsmGD1.repository.admin;

import com.example.AsmGD1.entity.DiscountCampaign;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;

public class DiscountCampaignSpecification {

    public static Specification<DiscountCampaign> buildFilter(
            String keyword, Date startDate, Date endDate,
            String status, String discountLevel
    ) {
        return (root, query, cb) -> {
            // ✅ Log ra các tham số đang lọc
            System.out.println("GỌI filterCampaigns TRONG Service với filter:");
            System.out.println("Keyword: " + keyword);
            System.out.println("Start date: " + startDate);
            System.out.println("End date: " + endDate);
            System.out.println("Status: " + status);
            System.out.println("Discount level: " + discountLevel);

            Predicate predicate = cb.conjunction();

            if (keyword != null && !keyword.isEmpty()) {
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
                Predicate codePredicate = cb.like(cb.lower(root.get("code")), "%" + keyword.toLowerCase() + "%");
                predicate = cb.and(predicate, cb.or(namePredicate, codePredicate));
            }

            if (startDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("startDate"), startDate));
            }

            if (endDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("endDate"), endDate));
            }

            if (status != null && !status.isEmpty()) {
                Date now = new Date();
                switch (status) {
                    case "ONGOING":
                        predicate = cb.and(predicate,
                                cb.lessThanOrEqualTo(root.get("startDate"), now),
                                cb.greaterThanOrEqualTo(root.get("endDate"), now));
                        break;
                    case "UPCOMING":
                        predicate = cb.and(predicate, cb.greaterThan(root.get("startDate"), now));
                        break;
                    case "ENDED":
                        predicate = cb.and(predicate, cb.lessThan(root.get("endDate"), now));
                        break;
                }
            }

            if (discountLevel != null && !discountLevel.isEmpty()) {
                try {
                    double level = Double.parseDouble(discountLevel);
                    predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("discountPercent"), level));
                } catch (NumberFormatException ignored) {
                }
            }

            // Bắt buộc lọc isDeleted = false
            predicate = cb.and(predicate, cb.isFalse(root.get("isDeleted")));

            return predicate;
        };
    }

}
