package com.example.AsmGD1.service.admin;

import com.example.AsmGD1.entity.Employee;

import com.example.AsmGD1.repository.admin.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private com.example.AsmGD1.repository.admin.EmployeeRepository employeeRepository;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .filter(emp -> !Boolean.TRUE.equals(emp.getIsDeleted()))
                .toList();
    }

    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Optional<Employee> getEmployeeById(Integer id) {
        return employeeRepository.findById(id);
    }

    public void deleteEmployee(Integer id) {
        Employee employee = getEmployeeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id: " + id));

        if (employee.getUser() != null && employee.getUser().getId() != null) {
            employee.setIsDeleted(true);
            employeeRepository.save(employee);
        } else {
            throw new IllegalStateException("User associated with Employee is not valid");
        }
    }

    public boolean existsByEmployeeCode(String employeeCode) {
        return employeeRepository.findByEmployeeCode(employeeCode).isPresent();
    }

    public void toggleStatus(Integer id) {
        Employee employee = getEmployeeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id: " + id));
        employee.setStatus(!Boolean.TRUE.equals(employee.getStatus())); // Toggle status
        employeeRepository.save(employee);
    }
}
