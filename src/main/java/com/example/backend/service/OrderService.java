package com.example.backend.service;

import com.example.backend.dto.CheckoutDTO;
import com.example.backend.dto.OrderDTO;
import com.example.backend.dto.OrderItemDTO;
import com.example.backend.entity.*;
import com.example.backend.repository.CartItemRepository;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.ProductVariantRepository;
import com.example.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional // áp dụng cho toàn bộ phương thức
public class OrderService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductVariantRepository variantRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartItemRepository cartItemRepository;


    public Order createOrder(CheckoutDTO dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setAddress(dto.getAddress());
        order.setPhone(dto.getPhone());
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.CREATED);

        // Lưu trước để lấy ID
        Order savedOrder = orderRepository.save(order);

        List<OrderItem> items = new java.util.ArrayList<>();

        for (var itemDTO : dto.getItems()) {
            ProductVariant variant = variantRepository.findById(itemDTO.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));

            double discountedPrice = variant.getProduct().getPrice() * (1 - variant.getProduct().getDiscount() / 100.0);
            double totalPrice = discountedPrice * itemDTO.getQuantity();

            OrderItem item = new OrderItem();
            item.setVariantId(variant.getId());
            item.setQuantity(itemDTO.getQuantity());
            item.setPrice(totalPrice);
            item.setOrder(savedOrder); // ✅ Không còn lỗi ở đây
            items.add(item);
        }

        savedOrder.setItems(items);

        // Lưu lại để cập nhật order items
        savedOrder = orderRepository.save(savedOrder);

        // Xoá item trong cart
        List<Long> variantIds = dto.getItems().stream()
                .map(item -> item.getVariantId())
                .collect(Collectors.toList());

        cartItemRepository.deleteByCartUserIdAndProductVariantIdIn(userId, variantIds);

        return savedOrder;
    }
    public List<OrderDTO> getOrdersByUser(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream().map(this::convertToDTO).toList();
    }

    public Optional<OrderDTO> getOrderByIdAndUser(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .map(this::convertToDTO);
    }


    // Cập nhật trạng thái đơn hàng
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus(status);
            orderRepository.save(order);
        }
    }
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setAddress(order.getAddress());
        dto.setPhone(order.getPhone());
        dto.setCreatedAt(order.getCreatedAt());
// Lấy giá trị số từ enum OrderStatus
        dto.setStatus(order.getStatus().getStatusCode());  // Chuyển enum thành số
        List<OrderItemDTO> itemDTOs = order.getItems().stream().map(item -> {
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setVariantId(item.getVariantId());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setPrice(item.getPrice());
            return itemDTO;
        }).toList();

        dto.setItems(itemDTOs);
        return dto;
    }

}
