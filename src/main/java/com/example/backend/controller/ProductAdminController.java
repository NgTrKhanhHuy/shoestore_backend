package com.example.backend.controller;

import com.example.backend.dto.ProductRequestDTO;
import com.example.backend.dto.ProductResponseDto;
import com.example.backend.entity.Product;
import com.example.backend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")


public class ProductAdminController {
    @Autowired
    private  ProductService productService;

    //    public ProductAdminController(ProductService productService) {
//        this.productService = productService;
//    }
//    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> getProduct(@PathVariable Long productId) {
        Product product = productService.findById(productId);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(productService.convertToDto(product));
    }
    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) { // Thêm tham số search
        Page<ProductResponseDto> productPage = productService.findPaginatedProducts(
                PageRequest.of(page, size),
                search
        );
        return ResponseEntity.ok(productPage);
    }
    /**
     * Endpoint thêm sản phẩm mới với dữ liệu sản phẩm và file ảnh.
     * Sử dụng multipart/form-data với 2 phần:
     * - product: JSON string chứa dữ liệu sản phẩm (ProductRequestDTO)
     * - file: File ảnh (MultipartFile)
     */
    // Endpoint thêm sản phẩm mới
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(
            @RequestPart("product") @Valid ProductRequestDTO productDTO,
            BindingResult bindingResult,
            @RequestPart("file") MultipartFile file) {
        if (bindingResult.hasErrors()) {
            // Nếu có lỗi, trả về lỗi 400 cùng danh sách lỗi
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        try {
            Product product = productService.createProduct(productDTO, file);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi khi thêm sản phẩm: " + e.getMessage());
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
            @RequestPart("product") @Valid ProductRequestDTO productDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            Product updatedProduct = productService.updateProduct(productDTO, file);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi khi cập nhật sản phẩm: " + e.getMessage());
        }
    }

}