package com.example.backend.repository;

import com.example.backend.entity.OrderItem;
import com.example.backend.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {
    // Tìm các sản phẩm trong một đơn hàng theo ID đơn hàng
    List<OrderItem> findByOrderId(Long orderId);
//    variantId
@Query("""
        SELECT COALESCE(SUM(oi.quantity), 0)
          FROM OrderItem oi
         WHERE oi.variantId  = :variantId
           AND oi.order.status = :status
    """)
Integer sumSoldByVariantAndStatus(
        @Param("variantId") Long variantId,
        @Param("status") OrderStatus status
);


}
