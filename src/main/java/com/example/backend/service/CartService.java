package com.example.backend.service;

import com.example.backend.dto.CartDTO;
import com.example.backend.dto.CartItemDTO;
import com.example.backend.entity.*;
import com.example.backend.repository.CartItemRepository;
import com.example.backend.repository.CartRepository;
import com.example.backend.repository.ProductVariantRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Transactional
    public CartDTO addToCart(Long userId, CartItemDTO cartItemDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        ProductVariant productVariant = productVariantRepository.findById(cartItemDTO.getVariantId())
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        if (productVariant.getStock() < cartItemDTO.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }

        Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndProductVariantId(
                cart.getId(), cartItemDTO.getVariantId());

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + cartItemDTO.getQuantity());
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProductVariant(productVariant);
            cartItem.setQuantity(cartItemDTO.getQuantity());
        }

        cartItemRepository.save(cartItem);

        return convertToCartDTO(cart);
    }

    @Transactional
    public CartDTO mergeCart(Long userId, List<CartItemDTO> localCartItems) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        for (CartItemDTO localItem : localCartItems) {
            ProductVariant productVariant = productVariantRepository.findById(localItem.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Product variant not found"));

            Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndProductVariantId(
                    cart.getId(), localItem.getVariantId());

            int newQuantity = localItem.getQuantity();
            if (existingCartItem.isPresent()) {
                newQuantity += existingCartItem.get().getQuantity();
            }

            // Kiểm tra tồn kho và điều chỉnh số lượng nếu cần
            if (productVariant.getStock() < newQuantity) {
                newQuantity = productVariant.getStock(); // Giới hạn số lượng bằng tồn kho tối đa
            }

            CartItem cartItem;
            if (existingCartItem.isPresent()) {
                cartItem = existingCartItem.get();
                cartItem.setQuantity(newQuantity);
            } else {
                cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setProductVariant(productVariant);
                cartItem.setQuantity(newQuantity);
            }

            cartItemRepository.save(cartItem);
        }

        return convertToCartDTO(cart);
    }

    @Transactional
    public CartDTO updateCartItem(Long userId, Long variantId, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Optional<CartItem> optionalCartItem = cartItemRepository.findByCartIdAndProductVariantId(cart.getId(), variantId);
        if (optionalCartItem.isEmpty()) {
            throw new RuntimeException("Cart item not found");
        }

        CartItem cartItem = optionalCartItem.get();
        ProductVariant productVariant = cartItem.getProductVariant();

        if (quantity > productVariant.getStock()) {
            throw new RuntimeException("Số lượng sản phẩm " + productVariant.getProduct().getName() + " vượt quá tồn kho");
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return convertToCartDTO(cart);
    }


    @Transactional
    public CartDTO removeCartItem(Long userId, Long variantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Optional<CartItem> optionalCartItem = cartItemRepository.findByCartIdAndProductVariantId(cart.getId(), variantId);
        if (optionalCartItem.isPresent()) {
            cartItemRepository.delete(optionalCartItem.get());
        } else {
            throw new RuntimeException("Cart item not found");
        }

        return convertToCartDTO(cart);
    }


    public CartDTO getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        return convertToCartDTO(cart);
    }

    private CartDTO convertToCartDTO(Cart cart) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(cart.getId());
        cartDTO.setUserId(cart.getUser().getId());

        List<CartItemDTO> cartItemDTOs = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            ProductVariant variant = cartItem.getProductVariant();
            Product product = variant.getProduct();

            CartItemDTO cartItemDTO = new CartItemDTO();
            cartItemDTO.setVariantId(variant.getId());
            cartItemDTO.setQuantity(cartItem.getQuantity());
            cartItemDTO.setProductName(product.getName());
            cartItemDTO.setProductImageUrl(product.getImageUrl());
            cartItemDTO.setPrice(product.getPrice());
            cartItemDTO.setDiscount(product.getDiscount());

            cartItemDTOs.add(cartItemDTO);
        }
        cartDTO.setCartItems(cartItemDTOs);

        return cartDTO;
    }
    @Transactional
    public void removePurchasedItems(Long userId, List<Long> variantIds) {
        cartItemRepository.deleteByCartUserIdAndProductVariantIdIn(userId, variantIds);
    }


}