package com.example.backend.entity;

public enum OrderStatus {
    CREATED(0),     // đã đặt
    PROCESSING(1),  // đang chuẩn bị
    SHIPPING(2),    // đang giao
    DELIVERED(3),   // đã giao
    CANCELLED(4);   // đã hủy

    private final int statusCode;

    OrderStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    // Phương thức để lấy enum từ giá trị số
    public static OrderStatus fromStatusCode(int statusCode) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.statusCode == statusCode) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status code: " + statusCode);
    }
}
