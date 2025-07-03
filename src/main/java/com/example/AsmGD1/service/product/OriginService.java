package com.example.AsmGD1.service.product;

import com.example.AsmGD1.entity.product.Origin;
import com.example.AsmGD1.repository.product.OriginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OriginService {
    @Autowired
    private OriginRepository originRepository;

    public List<Origin> getAllOrigin() {
        return originRepository.findAll();
    }

    public List<Origin> searchOrigins(String name) {
        return originRepository.findByNameContainingIgnoreCase(name);
    }

    public Origin getOriginById(Integer id) {
        return originRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Origin not found with id: " + id));
    }

    public Origin save(Origin origin) {
        return originRepository.save(origin);
    }

    public void delete(Integer id) {
        originRepository.deleteById(id);
    }
}