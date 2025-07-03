package com.example.AsmGD1.service.product;

import com.example.AsmGD1.entity.product.Color;
import com.example.AsmGD1.repository.product.ColorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColorService {
    @Autowired
    private ColorRepository colorRepository;

    public List<Color> getAllColors() {
        return colorRepository.findAll();
    }

    public List<Color> searchColors(String name) {
        return colorRepository.findByNameContainingIgnoreCase(name);
    }

    public Color getColorById(Integer id) {
        return colorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Color not found with id: " + id));
    }

    public Color saveColor(Color color) {
        return colorRepository.save(color);
    }

    public void deleteColor(Integer id) {
        colorRepository.deleteById(id);
    }
}