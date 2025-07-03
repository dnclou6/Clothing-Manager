package com.example.AsmGD1.repository.admin;


import com.example.AsmGD1.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> findByEmployeeCode(String employeeCode);
}
