package com.example.backend.controller;

import com.example.backend.config.CustomUserDetails;
import com.example.backend.dto.CartDTO;
import com.example.backend.dto.CartItemDTO;
import com.example.backend.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<CartDTO> addToCart(@RequestBody CartItemDTO cartItemDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        CartDTO cartDTO = cartService.addToCart(userId, cartItemDTO);
        return ResponseEntity.ok(cartDTO);
    }

    @PostMapping("/merge")
    public ResponseEntity<CartDTO> mergeCart(@RequestBody List<CartItemDTO> localCartItems) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        CartDTO cartDTO = cartService.mergeCart(userId, localCartItems);
        return ResponseEntity.ok(cartDTO);
    }

    @GetMapping
    public ResponseEntity<CartDTO> getCart() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        CartDTO cartDTO = cartService.getCart(userId);
        return ResponseEntity.ok(cartDTO);
    }
    @PutMapping("/update")
    public ResponseEntity<CartDTO> updateCartItem(@RequestBody CartItemDTO cartItemDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        Long variantId = cartItemDTO.getVariantId();
        int quantity = cartItemDTO.getQuantity();

        try {
            CartDTO cartDTO = cartService.updateCartItem(userId, variantId, quantity);
            System.out.println("Principal class: " + authentication.getPrincipal().getClass().getName());
            return ResponseEntity.ok(cartDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

    }
    @DeleteMapping("/remove/{variantId}")
    public ResponseEntity<CartDTO> removeCartItem(@PathVariable Long variantId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        try {
            CartDTO cartDTO = cartService.removeCartItem(userId, variantId);
            return ResponseEntity.ok(cartDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

}
