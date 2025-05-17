package com.example.backend.service;

import com.example.backend.dto.CheckoutDTO;
import com.example.backend.dto.CustomerInfoDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

@Transactional
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

            // Kiểm tra số lượng tồn kho
            if (variant.getStock() < itemDTO.getQuantity()) {
                throw new RuntimeException("Số lượng sản phẩm " + variant.getId() + " không đủ");
            }

            // Trừ số lượng trong kho
            variant.setStock(variant.getStock() - itemDTO.getQuantity());
            variantRepository.save(variant);

            double discountedPrice = variant.getProduct().getPrice() * (1 - variant.getProduct().getDiscount() / 100.0);
            double totalPrice = discountedPrice * itemDTO.getQuantity();

            OrderItem item = new OrderItem();
            item.setVariantId(variant.getId());
            item.setQuantity(itemDTO.getQuantity());
            item.setPrice(totalPrice);
            item.setOrder(savedOrder);
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
//    public void updateOrderStatus(Long orderId, OrderStatus status) {
//        Optional<Order> orderOptional = orderRepository.findById(orderId);
//        if (orderOptional.isPresent()) {
//            Order order = orderOptional.get();
//            order.setStatus(status);
//            orderRepository.save(order);
//        }
//    }
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus(status);
            orderRepository.save(order);
        } else {
            throw new RuntimeException("Đơn hàng không tồn tại");
        }
    }
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setAddress(order.getAddress());
        dto.setPhone(order.getPhone());
        dto.setCreatedAt(order.getCreatedAt());

        // Thêm thông tin khách hàng
        CustomerInfoDTO customerInfo = new CustomerInfoDTO();
        customerInfo.setName(order.getUser().getUsername());
        customerInfo.setEmail(order.getUser().getEmail());
        dto.setCustomerInfo(customerInfo);

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
    public Page<OrderDTO> getAllOrders(Pageable pageable, String search) {
        Page<Order> orders;
        if (search == null || search.trim().isEmpty()) {
            orders = orderRepository.findAll(pageable);
        } else {
            orders = orderRepository.findByPhoneContainingIgnoreCase(search, pageable);
        }
        return orders.map(this::convertToDTO);
    }

    @Transactional
    public void cancelOrderByUser(Long orderId, Long userId) {
        Optional<Order> orderOptional = orderRepository.findByIdAndUserId(orderId, userId);

        if (orderOptional.isEmpty()) {
            throw new RuntimeException("Đơn hàng không tồn tại hoặc không thuộc về bạn");
        }

        Order order = orderOptional.get();

        // Chỉ cho phép hủy khi đơn ở trạng thái CREATED(0) hoặc PROCESSING(1)
        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PROCESSING) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng khi đang ở trạng thái Chờ xử lý hoặc Đang chuẩn bị");
        }

        // Tái sử dụng hàm updateOrderStatus có sẵn
        updateOrderStatus(orderId, OrderStatus.CANCELLED);

        // Có thể thêm logic hoàn trả số lượng sản phẩm vào kho nếu cần
        restoreProductQuantities(order);
    }

    private void restoreProductQuantities(Order order) {
        for (OrderItem item : order.getItems()) {
            ProductVariant variant = variantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));
            variant.setStock(variant.getStock() + item.getQuantity());
            variantRepository.save(variant);
        }
    }
    // Trong OrderService.java
    public Optional<OrderDTO> getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(this::convertToDTO);
    }

}
