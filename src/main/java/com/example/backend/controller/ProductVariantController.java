package com.example.backend.controller;

import com.example.backend.dto.ProductVariantDTO;
import com.example.backend.entity.ProductVariant;
import com.example.backend.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/product-variants")
public class ProductVariantController {
    @Autowired
    private ProductVariantRepository productVariantRepository;

    @GetMapping("/{id}")
    public ResponseEntity<ProductVariantDTO> getProductVariantById(@PathVariable Long id) {
        Optional<ProductVariant> variant = productVariantRepository.findById(id);
        if (variant.isPresent()) {
            ProductVariant v = variant.get();
            ProductVariantDTO dto = new ProductVariantDTO();
            dto.setId(v.getId());
            dto.setStock(v.getStock());
            dto.setColor(v.getColor());
            dto.setSize(v.getSize());
            dto.setProductId(v.getProduct().getId()); // Thêm productId
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
