package com.example.backend.repository;

import com.example.backend.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
}
