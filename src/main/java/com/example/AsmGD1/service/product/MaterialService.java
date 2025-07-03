package com.example.AsmGD1.service.product;

import com.example.AsmGD1.entity.product.Material;
import com.example.AsmGD1.repository.product.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialService {
    @Autowired
    private MaterialRepository materialRepository;

    public List<Material> getAllMaterial() {
        return materialRepository.findAll();
    }

    public List<Material> searchMaterials(String name) {
        return materialRepository.findByNameContainingIgnoreCase(name);
    }

    public Material getMaterialById(Integer id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found with id: " + id));
    }

    public Material save(Material material) {
        return materialRepository.save(material);
    }

    public void delete(Integer id) {
        materialRepository.deleteById(id);
    }
}