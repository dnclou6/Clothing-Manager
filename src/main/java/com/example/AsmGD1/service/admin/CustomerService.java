package com.example.AsmGD1.service.admin;

import com.example.AsmGD1.entity.Customer;

import com.example.AsmGD1.repository.admin.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private com.example.AsmGD1.repository.admin.CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public void saveCustomer(Customer customer) {
        customerRepository.save(customer);
    }

    public Optional<Customer> getCustomerById(Integer id) {
        return customerRepository.findById(id);
    }

    public void deleteCustomer(Integer id) {
        customerRepository.deleteById(id);
    }

}