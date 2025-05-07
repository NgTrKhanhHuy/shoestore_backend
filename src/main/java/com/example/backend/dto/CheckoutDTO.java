package com.example.backend.dto;

import lombok.Data;

import java.util.List;
@Data
public class CheckoutDTO {
    private String address;
    private String phone;
    private List<CheckoutItemDTO> items;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<CheckoutItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CheckoutItemDTO> items) {
        this.items = items;
    }
}
