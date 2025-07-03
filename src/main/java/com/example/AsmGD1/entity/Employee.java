package com.example.AsmGD1.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "employee_code", unique = true)
    @NotBlank(message = "Mã nhân viên không được để trống")
    private String employeeCode;

    @Column(name = "hire_date")
    @NotNull(message = "Ngày vào làm không được để trống")
    private LocalDate hireDate;

    @Column(name = "department")
    private String department;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "status")
    private Boolean status; // true = Active, false = Inactive

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // Loại bỏ insertable = false, updatable = false
    private User user;

    // Constructors
    public Employee() {
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
