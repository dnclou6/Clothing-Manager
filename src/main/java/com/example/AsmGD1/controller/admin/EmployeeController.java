package com.example.AsmGD1.controller.admin;

import com.example.AsmGD1.entity.Employee;
import com.example.AsmGD1.entity.User;

import com.example.AsmGD1.service.UserService;
import com.example.AsmGD1.service.admin.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/acvstore/nhanvien")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listEmployees(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);
        System.out.println("Số lượng nhân viên: " + employees.size());
        return "/WebQuanLy/nhanvien";
    }

    @GetMapping("/add")
    public String showAddEmployeeForm(Model model) {
        Employee employee = new Employee();
        employee.setUser(new User());
        model.addAttribute("employee", employee);
        return "/WebQuanLy/add-employee";
    }

    @PostMapping("/save")
    public String saveEmployee(@Valid @ModelAttribute("employee") Employee employee,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "/WebQuanLy/add-employee";
        }

        User user = employee.getUser();
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            result.rejectValue("user.email", "notnull.email", "Email không được để trống");
            return "/WebQuanLy/add-employee";
        }

        // Kiểm tra trùng lặp
        if (userService.existsByUsername(user.getUsername())) {
            result.rejectValue("user.username", "duplicate.username", "Tên đăng nhập đã tồn tại");
            return "/WebQuanLy/add-employee";
        }
        if (userService.existsByEmail(user.getEmail())) {
            result.rejectValue("user.email", "duplicate.email", "Email đã tồn tại");
            return "/WebQuanLy/add-employee";
        }
        if (employeeService.existsByEmployeeCode(employee.getEmployeeCode())) {
            result.rejectValue("employeeCode", "duplicate.employeeCode", "Mã nhân viên đã tồn tại");
            return "/WebQuanLy/add-employee";
        }

        user.setRole("EMPLOYEE");
        user.setCreatedAt(new java.util.Date());
        user.setIsDeleted(false);
        user = userService.saveUser(user); // Lưu User

        employee.setUser(user); // Gán User vào Employee
        employee.setCreatedAt(LocalDateTime.now());
        employee.setIsDeleted(false);
        employee.setStatus(true); // Set default status to active
        employeeService.saveEmployee(employee); // Lưu Employee

        return "redirect:/acvstore/nhanvien";
    }

    @GetMapping("/edit/{id}")
    public String showEditEmployeeForm(@PathVariable("id") Integer id, Model model) {
        Employee employee = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id: " + id));
        model.addAttribute("employee", employee);
        return "/WebQuanLy/edit-employee";
    }

    @PostMapping("/update/{id}")
    public String updateEmployee(@PathVariable("id") Integer id,
                                 @Valid @ModelAttribute("employee") Employee employee,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "/WebQuanLy/edit-employee";
        }

        Employee existingEmployee = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id: " + id));
        User existingUser = existingEmployee.getUser();

        // Kiểm tra trùng lặp với bản ghi khác
        if (!existingUser.getUsername().equals(employee.getUser().getUsername()) &&
                userService.existsByUsername(employee.getUser().getUsername())) {
            result.rejectValue("user.username", "duplicate.username", "Tên đăng nhập đã tồn tại");
            return "/WebQuanLy/edit-employee";
        }
        if (!existingUser.getEmail().equals(employee.getUser().getEmail()) &&
                userService.existsByEmail(employee.getUser().getEmail())) {
            result.rejectValue("user.email", "duplicate.email", "Email đã tồn tại");
            return "/WebQuanLy/edit-employee";
        }
        if (!existingEmployee.getEmployeeCode().equals(employee.getEmployeeCode()) &&
                employeeService.existsByEmployeeCode(employee.getEmployeeCode())) {
            result.rejectValue("employeeCode", "duplicate.employeeCode", "Mã nhân viên đã tồn tại");
            return "/WebQuanLy/edit-employee";
        }

        // Cập nhật User, giữ nguyên password và date_of_birth nếu không có giá trị mới
        existingUser.setUsername(employee.getUser().getUsername());
        existingUser.setFullName(employee.getUser().getFullName());
        existingUser.setEmail(employee.getUser().getEmail());
        existingUser.setPhone(employee.getUser().getPhone());
        existingUser.setAddress(employee.getUser().getAddress());

        // Chỉ cập nhật password nếu có giá trị mới
        if (employee.getUser().getPassword() != null && !employee.getUser().getPassword().trim().isEmpty()) {
            existingUser.setPassword(employee.getUser().getPassword());
        }

        // Chỉ cập nhật date_of_birth nếu có giá trị mới
        if (employee.getUser().getDateOfBirth() != null) {
            existingUser.setDateOfBirth(employee.getUser().getDateOfBirth());
        }

        userService.saveUser(existingUser);

        // Cập nhật Employee
        existingEmployee.setEmployeeCode(employee.getEmployeeCode());
        existingEmployee.setHireDate(employee.getHireDate());
        existingEmployee.setDepartment(employee.getDepartment());
        employeeService.saveEmployee(existingEmployee);

        return "redirect:/acvstore/nhanvien";
    }

    @GetMapping("/delete/{id}")
    @Transactional
    public String deleteEmployee(@PathVariable("id") Integer id) {
        Employee employee = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id: " + id));

        // Đảm bảo User được tải
        User user = userService.getUserById(employee.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + employee.getUser().getId()));

        // Xóa Employee
        employeeService.deleteEmployee(id);

        // Xóa User
        userService.deleteUser(user.getId());

        return "redirect:/acvstore/nhanvien";
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleEmployeeStatus(@PathVariable("id") Integer id) {
        employeeService.toggleStatus(id);
        return "redirect:/acvstore/nhanvien";
    }
}