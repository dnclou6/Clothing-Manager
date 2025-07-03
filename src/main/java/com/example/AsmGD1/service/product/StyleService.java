package com.example.AsmGD1.service.product;

import com.example.AsmGD1.entity.product.Style;
import com.example.AsmGD1.repository.product.StyleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StyleService {
    @Autowired
    private StyleRepository styleRepository;

    public List<Style> getAllStyle() {
        return styleRepository.findAll();
    }

    public List<Style> searchStyles(String name) {
        return styleRepository.findByNameContainingIgnoreCase(name);
    }

    public Style getStyleById(Integer id) {
        return styleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Style not found with id: " + id));
    }

    public Style save(Style style) {
        return styleRepository.save(style);
    }

    public void delete(Integer id) {
        styleRepository.deleteById(id);
    }
}