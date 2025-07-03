package com.example.AsmGD1.controller.admin;

import com.example.AsmGD1.entity.DiscountVoucher;
import com.example.AsmGD1.entity.User;
import com.example.AsmGD1.service.admin.DiscountVoucherService;
import com.example.AsmGD1.service.admin.EmailService;
import com.example.AsmGD1.service.admin.UserDiscountVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

@Controller
@RequestMapping("/acvstore")
public class DiscountVoucherController {

    @Autowired
    private EmailService emailService;
    @Autowired
    private DiscountVoucherService discountVoucherService;
    @Autowired
    private UserDiscountVoucherService userDiscountVoucherService;

    @GetMapping("/vouchers")
    public String list(Model model) {
        List<DiscountVoucher> vouchers = discountVoucherService.getAllVouchers();

        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new Locale("vi", "VN"));
        vouchers.forEach(v -> {
            String value = nf.format(v.getDiscountValue());
            if ("PERCENT".equalsIgnoreCase(v.getType())) {
                v.setDiscountValueFormatted(value + " %");
            } else {
                v.setDiscountValueFormatted(value + " ₫");
            }

            if (v.getMaxDiscountValue() != null) {
                v.setMaxDiscountValueFormatted(nf.format(v.getMaxDiscountValue()) + " ₫");
            }

            if (v.getMinOrderValue() != null) {
                v.setMinOrderValueFormatted(nf.format(v.getMinOrderValue()) + " ₫");
            }
        });

        model.addAttribute("vouchers", vouchers);
        model.addAttribute("getStatus", (Function<DiscountVoucher, String>) discountVoucherService::getStatus);
        return "WebQuanLy/voucher-list";
    }

    @GetMapping("/vouchers/create")
    public String createForm(Model model) {
        model.addAttribute("voucher", new DiscountVoucher());
        model.addAttribute("customers", userDiscountVoucherService.getAllCustomers());
        return "WebQuanLy/voucher-create";
    }

    @PostMapping("/vouchers/create")
    public String create(@RequestParam String discountValue,
                         @RequestParam(required = false) String maxDiscountValue,
                         @RequestParam(required = false) String minOrderValue,
                         @ModelAttribute DiscountVoucher voucher,
                         @RequestParam(required = false) boolean sendMail,
                         @RequestParam(required = false) List<Long> selectedCustomerIds,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        if (discountVoucherService.existsByCode(voucher.getCode())) {
            model.addAttribute("errorMessage", "Mã phiếu đã tồn tại!");
            model.addAttribute("voucher", voucher);
            model.addAttribute("customers", userDiscountVoucherService.getAllCustomers());
            return "WebQuanLy/voucher-create";
        }

        try {
            voucher.setDiscountValue(new BigDecimal(discountValue.replace(",", "")));
            if (maxDiscountValue != null && !maxDiscountValue.isEmpty()) {
                voucher.setMaxDiscountValue(new BigDecimal(maxDiscountValue.replace(",", "")));
            }
            if (minOrderValue != null && !minOrderValue.isEmpty()) {
                voucher.setMinOrderValue(new BigDecimal(minOrderValue.replace(",", "")));
            }

            DiscountVoucher savedVoucher = discountVoucherService.save(voucher);

            if (!voucher.isPublic() && selectedCustomerIds != null) {
                List<User> selectedUsers = userDiscountVoucherService.getUsersByIds(selectedCustomerIds);
                for (User user : selectedUsers) {
                    userDiscountVoucherService.assignVoucherToUser(user, savedVoucher);
                }

                if (sendMail) {
                    emailService.sendVoucherToMany(selectedUsers, savedVoucher);
                    redirectAttributes.addFlashAttribute("mailMessage", "Đã gửi email đến khách hàng được chọn");
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Tạo phiếu giảm giá thành công!");
            return "redirect:/acvstore/vouchers";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi tạo phiếu giảm giá: " + e.getMessage());
            model.addAttribute("voucher", voucher);
            model.addAttribute("customers", userDiscountVoucherService.getAllCustomers());
            return "WebQuanLy/voucher-create";
        }
    }

    @GetMapping("/vouchers/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("voucher", discountVoucherService.getById(id));
        model.addAttribute("customers", userDiscountVoucherService.getAllCustomers());
        return "WebQuanLy/voucher-edit";
    }

    @PostMapping("/vouchers/edit/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam String discountValue,
                         @RequestParam(required = false) String maxDiscountValue,
                         @RequestParam(required = false) String minOrderValue,
                         @ModelAttribute DiscountVoucher voucher,
                         @RequestParam(required = false) boolean sendMail,
                         @RequestParam(required = false) List<Long> selectedCustomerIds,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        voucher.setId(id);
        voucher.setDiscountValue(new BigDecimal(discountValue.replace(",", "")));
        if (maxDiscountValue != null && !maxDiscountValue.isEmpty()) {
            voucher.setMaxDiscountValue(new BigDecimal(maxDiscountValue.replace(",", "")));
        }
        if (minOrderValue != null && !minOrderValue.isEmpty()) {
            voucher.setMinOrderValue(new BigDecimal(minOrderValue.replace(",", "")));
        }

        DiscountVoucher savedVoucher = discountVoucherService.save(voucher);

        if (!voucher.isPublic() && selectedCustomerIds != null) {
            List<User> selectedUsers = userDiscountVoucherService.getUsersByIds(selectedCustomerIds);
            for (User user : selectedUsers) {
                userDiscountVoucherService.assignVoucherToUser(user, savedVoucher);
            }

            if (sendMail) {
                try {
                    emailService.sendVoucherToMany(selectedUsers, savedVoucher);
                    redirectAttributes.addFlashAttribute("mailMessage", "Đã gửi mail cập nhật cho khách hàng");
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi gửi mail: " + e.getMessage());
                }
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phiếu giảm giá thành công!");
        return "redirect:/acvstore/vouchers";
    }

    @PostMapping("/vouchers/delete/{id}")
    public String delete(@PathVariable Long id) {
        discountVoucherService.delete(id);
        return "redirect:/acvstore/vouchers";
    }
}