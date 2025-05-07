package com.example.backend.dto;

import lombok.Data;

@Data
public class OrderItemDTO {
    private Long variantId;
    private int quantity;
    private double price;

    // Getters & Setters
    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
