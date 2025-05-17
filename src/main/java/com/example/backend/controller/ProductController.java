package com.example.backend.controller;

import com.example.backend.dto.ProductResponseDto;
import com.example.backend.entity.Product;
import com.example.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

// Thêm controller mới cho public endpoints
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> listProductsPublic(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<String> sizes) { // Add sizes parameter
        return ResponseEntity.ok(productService.findPaginatedProductsForPublic(
                PageRequest.of(page, size),
                search,
                categoryId,
                sizes
        ));
    }
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> getProductDetails(@PathVariable Long productId) {
        Product product = productService.findById(productId);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(productService.convertToDto(product));
    }
}