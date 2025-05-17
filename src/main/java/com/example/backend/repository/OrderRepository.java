package com.example.backend.repository;

import com.example.backend.entity.Order;
import com.example.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Tìm tất cả đơn hàng của người dùng
    List<Order> findByUserId(Long userId);

    // Tìm đơn hàng theo id và người dùng
    Optional<Order> findByIdAndUserId(Long orderId, Long userId);
    Page<Order> findByPhoneContainingIgnoreCase(String phone, Pageable pageable);

}
