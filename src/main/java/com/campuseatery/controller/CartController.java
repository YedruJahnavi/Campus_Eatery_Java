package com.campuseatery.controller;

import com.campuseatery.dto.CartItemDto;
import com.campuseatery.dto.CartUpdateDto;
import com.campuseatery.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<?> getCart(@RequestHeader("X-User-Id") String userId) {
        try {
            return ResponseEntity.ok(cartService.getCart(userId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addItem(@RequestHeader("X-User-Id") String userId, @RequestBody CartItemDto dto) {
        try {
            return ResponseEntity.ok(cartService.addItem(userId, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> removeItem(@RequestHeader("X-User-Id") String userId, @PathVariable String itemId) {
        try {
            return ResponseEntity.ok(cartService.removeItem(userId, itemId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateItem(
            @RequestHeader("X-User-Id") String userId, 
            @PathVariable String itemId, 
            @RequestBody CartUpdateDto dto) {
        try {
            return ResponseEntity.ok(cartService.updateItem(userId, itemId, dto.getAction()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }
}
