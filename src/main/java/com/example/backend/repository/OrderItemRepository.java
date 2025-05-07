package com.example.backend.repository;

import com.example.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {
    // Tìm các sản phẩm trong một đơn hàng theo ID đơn hàng
    List<OrderItem> findByOrderId(Long orderId);
}
