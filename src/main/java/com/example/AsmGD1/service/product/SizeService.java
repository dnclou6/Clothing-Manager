package com.example.AsmGD1.service.product;

import com.example.AsmGD1.entity.product.Size;
import com.example.AsmGD1.repository.product.SizeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SizeService {
    @Autowired
    private SizeRepository sizeRepository;

    public List<Size> getAllSize() {
        return sizeRepository.findAll();
    }

    public List<Size> searchSizes(String name) {
        return sizeRepository.findByNameContainingIgnoreCase(name);
    }

    public Size getSizeById(Integer id) {
        return sizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Size not found with id: " + id));
    }

    public Size save(Size size) {
        return sizeRepository.save(size);
    }

    public void delete(Integer id) {
        sizeRepository.deleteById(id);
    }
}