package com.example.AsmGD1.controller.admin;

import com.example.AsmGD1.entity.User;
import com.example.AsmGD1.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date; // Thêm import này
import java.util.List;

@Controller
@RequestMapping("/acvstore/khachhang")
public class CustomerController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String listCustomers(Model model) {
        List<User> customers = userService.getAllCustomers();
        model.addAttribute("customers", customers);
        System.out.println("Số lượng khách hàng: " + customers.size());
        return "/WebQuanLy/customer-list";
    }

    @GetMapping("/addCustomer")
    public String showAddCustomerForm(Model model) {
        User customer = new User();
        model.addAttribute("customer", customer);
        return "/WebQuanLy/add-customer";
    }

    @PostMapping("/saveCustomer")
    public String saveCustomer(@Valid @ModelAttribute("customer") User customer,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "/WebQuanLy/add-customer";
        }

        // Tự động tạo username từ email
        String emailPrefix = customer.getEmail().split("@")[0];
        String username = emailPrefix;
        int counter = 1;
        while (userService.existsByUsername(username)) {
            username = emailPrefix + counter++;
        }
        customer.setUsername(username);

        // Kiểm tra trùng email và idCard như cũ
        if (userService.existsByEmail(customer.getEmail())) {
            result.rejectValue("email", "duplicate.email", "Email đã tồn tại");
            return "/WebQuanLy/add-customer";
        }
        if (customer.getIdCard() != null && userService.existsByIdCard(customer.getIdCard())) {
            result.rejectValue("idCard", "duplicate.idCard", "CMND/CCCD đã tồn tại");
            return "/WebQuanLy/add-customer";
        }if ("admin".equalsIgnoreCase(customer.getUsername())) {
            result.rejectValue("username", "invalid.username", "Tên đăng nhập 'admin' không được phép sử dụng");
            return "/WebQuanLy/add-customer";
        }

        customer.setRole("CUSTOMER");
        customer.setCreatedAt(new Date());
        customer.setIsDeleted(false);
        userService.saveUser(customer);
        return "redirect:/acvstore/khachhang";
    }

    @GetMapping("/edit/{id}")
    public String showEditCustomerForm(@PathVariable("id") Integer id, Model model) {
        User customer = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer Id: " + id));
        model.addAttribute("customer", customer);
        return "/WebQuanLy/edit-customer";
    }

    @PostMapping("/update/{id}")
    public String updateCustomer(@PathVariable("id") Integer id,
                                 @Valid @ModelAttribute("customer") User customer,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "/WebQuanLy/edit-customer";
        }

        User existingCustomer = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer Id: " + id));

        // Kiểm tra trùng với bản ghi khác
        if (!existingCustomer.getUsername().equals(customer.getUsername()) &&
                userService.existsByUsername(customer.getUsername())) {
            result.rejectValue("username", "duplicate.username", "Tên đăng nhập đã tồn tại");
            return "/WebQuanLy/edit-customer";
        }
        if (!existingCustomer.getEmail().equals(customer.getEmail()) &&
                userService.existsByEmail(customer.getEmail())) {
            result.rejectValue("email", "duplicate.email", "Email đã tồn tại");
            return "/WebQuanLy/edit-customer";
        }
        if (customer.getIdCard() != null && !existingCustomer.getIdCard().equals(customer.getIdCard()) &&
                userService.existsByIdCard(customer.getIdCard())) {
            result.rejectValue("idCard", "duplicate.idCard", "CMND/CCCD đã tồn tại");
            return "/WebQuanLy/edit-customer";
        }
        // Chỉ cập nhật mật khẩu nếu người dùng nhập mật khẩu mới
        if (customer.getPassword() != null && !customer.getPassword().isEmpty()) {
            existingCustomer.setPassword(customer.getPassword());
        }

        // Cập nhật các trường khác
        existingCustomer.setUsername(customer.getUsername());
        existingCustomer.setFullName(customer.getFullName());
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setPhone(customer.getPhone());
        existingCustomer.setAddress(customer.getAddress());
        existingCustomer.setDateOfBirth(customer.getDateOfBirth());
        existingCustomer.setIdCard(customer.getIdCard());
        existingCustomer.setGender(customer.getGender());
        existingCustomer.setProvince(customer.getProvince());
        existingCustomer.setDistrict(customer.getDistrict());
        existingCustomer.setWard(customer.getWard());
        existingCustomer.setAddressDetail(customer.getAddressDetail());

        userService.saveUser(existingCustomer);
        return "redirect:/acvstore/khachhang";
    }

    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable("id") Integer id) {
        User customer = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer Id: " + id));
        userService.deleteUser(id);
        return "redirect:/acvstore/khachhang";
    }
}
