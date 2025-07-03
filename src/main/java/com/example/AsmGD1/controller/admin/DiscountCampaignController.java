    package com.example.AsmGD1.controller.admin;

    import com.example.AsmGD1.entity.DiscountCampaign;
    import com.example.AsmGD1.entity.Product;
    import com.example.AsmGD1.entity.ProductDetail;
    import com.example.AsmGD1.service.admin.AdminDiscountCampaignService;
    import com.example.AsmGD1.service.admin.DiscountCampaignService;
    import com.example.AsmGD1.service.admin.ProductService;
    import jakarta.servlet.http.HttpServletRequest;
    import lombok.RequiredArgsConstructor;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.domain.Sort;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.*;

    import java.util.Date;
    import java.util.List;
    import java.util.Set;
    import java.util.stream.Collectors;

    @Controller
    @RequestMapping("/acvstore/discount-campaigns")
    @RequiredArgsConstructor
    public class DiscountCampaignController {

        @Autowired
        private DiscountCampaignService discountCampaignService;

        @Autowired
        private AdminDiscountCampaignService campaignService;

        @Autowired
        private ProductService productService;

        @GetMapping
        public String showDiscountCampaignList(
                @RequestParam(name = "keyword", required = false) String keyword,
                @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
                @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
                @RequestParam(name = "status", required = false) String status,
                @RequestParam(name = "discountLevel", required = false) String discountLevel,
                @RequestParam(name = "page", defaultValue = "0") int page,
                @RequestParam(name = "size", defaultValue = "10") int size,
                Model model
        ) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());

            Page<DiscountCampaign> campaignPage = discountCampaignService.getFilteredCampaigns(
                    keyword, startDate, endDate, status, discountLevel, pageable
            );

            model.addAttribute("campaignList", campaignPage.getContent());
            model.addAttribute("currentPage", campaignPage.getNumber());
            model.addAttribute("totalPages", campaignPage.getTotalPages());
            model.addAttribute("totalItems", campaignPage.getTotalElements());
            model.addAttribute("keyword", keyword);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("status", status);
            model.addAttribute("discountLevel", discountLevel);
            return "WebQuanLy/discount-campaign-list";
        }

        @GetMapping("/create")
        public String showCreateForm(Model model,
                                     @RequestParam(name = "page", defaultValue = "0") int page) {
            Pageable pageable = PageRequest.of(page, 5);
            // Using productService instead of repository directly
            Page<Product> productPage = productService.findAllPaginated(pageable);

            model.addAttribute("campaign", new DiscountCampaign());
            model.addAttribute("productPage", productPage);
            model.addAttribute("currentProductPage", page);
            model.addAttribute("totalProductPages", productPage.getTotalPages());
            return "WebQuanLy/discount-campaign-form";
        }

        @PostMapping("/create")
        public String createCampaign(
                @ModelAttribute("campaign") DiscountCampaign campaign,
                @RequestParam("selectedProductDetailIds") List<Integer> selectedDetailIds,
                HttpServletRequest request
        ) {
            campaign.setCreatedAt(new Date());
            campaign.setDeleted(false);
            campaignService.createDiscountCampaign(campaign, selectedDetailIds);
            return "redirect:/acvstore/discount-campaigns";
        }

        @GetMapping("/product-details")
        @ResponseBody
        public List<ProductDetail> getProductDetailsByProductId(@RequestParam("productId") Integer productId) {
            return campaignService.findProductDetailsByProductId(productId);
        }

        @GetMapping("/multiple-product-details")
        @ResponseBody
        public List<ProductDetail> getDetailsByProductIds(@RequestParam("productIds") List<Integer> productIds) {
            return campaignService.findByProductIds(productIds);
        }

        @GetMapping("/edit/{id}")
        public String showEditForm(@PathVariable("id") Long id, Model model) {
            DiscountCampaign campaign = campaignService.findById(id);
            if (campaign == null) return "redirect:/acvstore/discount-campaigns";

            Pageable pageable = PageRequest.of(0, 5);
            // Using productService instead of repository directly
            Page<Product> productPage = productService.findAllPaginated(pageable);

            List<ProductDetail> selectedDetails = campaignService.findSelectedDetailsByCampaignId(id);

            Set<Integer> selectedProductIds = selectedDetails.stream()
                    .map(detail -> detail.getProduct().getId())
                    .collect(Collectors.toSet());

            model.addAttribute("campaign", campaign);
            model.addAttribute("productPage", productPage);
            model.addAttribute("currentProductPage", 0);
            model.addAttribute("totalProductPages", productPage.getTotalPages());
            model.addAttribute("selectedDetails", selectedDetails);
            model.addAttribute("selectedProductIds", selectedProductIds);

            return "WebQuanLy/discount-campaign-edit";
        }

        @PostMapping("/update")
        public String updateCampaign(
                @ModelAttribute("campaign") DiscountCampaign campaign,
                @RequestParam("selectedProductDetailIds") List<Integer> selectedDetailIds
        ) {
            campaignService.updateDiscountCampaign(campaign, selectedDetailIds);
            return "redirect:/acvstore/discount-campaigns";
        }
    }