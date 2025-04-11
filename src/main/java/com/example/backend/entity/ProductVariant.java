package com.example.backend.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
// ProductVariant.java
@Entity
@Table(name = "product_variants")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int stock;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonBackReference // <== Thêm annotation này
    private Product product;

    @ManyToOne
    @JoinColumn(name = "size_id")
    private Size size;

    @ManyToOne
    @JoinColumn(name = "color_id")
    private Color color;

    // Getters and Setters

    public ProductVariant(Long id, int stock, Product product, Size size, Color color) {
        this.id = id;
        this.stock = stock;
        this.product = product;
        this.size = size;
        this.color = color;
    }

    public ProductVariant() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}