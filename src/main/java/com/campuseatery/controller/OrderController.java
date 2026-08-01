package com.campuseatery.controller;

import com.campuseatery.dto.OrderWithReviewDto;
import com.campuseatery.dto.UpdateOrderStatusDto;
import com.campuseatery.model.Order;
import com.campuseatery.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<?> getStudentOrders(@RequestHeader("X-User-Id") String userId) {
        try {
            List<OrderWithReviewDto> orders = orderService.getStudentOrders(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestHeader("X-User-Id") String userId) {
        try {
            String orderId = orderService.checkout(userId);
            return ResponseEntity.ok(Map.of("success", true, "order_id", orderId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }

    @GetMapping("/vendor")
    public ResponseEntity<?> getVendorOrders(@RequestHeader("X-User-Id") String vendorId) {
        try {
            List<Order> orders = orderService.getVendorOrders(vendorId);
            return ResponseEntity.ok(orders);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @RequestHeader("X-User-Id") String vendorId,
            @PathVariable String id,
            @RequestBody UpdateOrderStatusDto dto) {
        try {
            Order order = orderService.updateOrderStatus(vendorId, id, dto.getStatus());
            return ResponseEntity.ok(order);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("detail", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }
}
