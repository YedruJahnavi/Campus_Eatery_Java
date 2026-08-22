package com.campuseatery.controller;

import com.campuseatery.model.Order;
import com.campuseatery.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final OrderService orderService;

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableOrders(@RequestHeader("X-User-Id") String riderId) {
        try {
            List<Order> orders = orderService.getAvailableDeliveries();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/my-deliveries")
    public ResponseEntity<?> getMyDeliveries(@RequestHeader("X-User-Id") String riderId) {
        try {
            List<Order> orders = orderService.getMyDeliveries(riderId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error: " + e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/accept")
    public ResponseEntity<?> acceptDelivery(@RequestHeader("X-User-Id") String riderId, @PathVariable String orderId) {
        try {
            Order order = orderService.acceptDelivery(orderId, riderId);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error: " + e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/complete")
    public ResponseEntity<?> completeDelivery(@RequestHeader("X-User-Id") String riderId, @PathVariable String orderId, @RequestBody Map<String, String> body) {
        try {
            String deliveryCode = body.get("deliveryCode");
            Order order = orderService.completeDelivery(orderId, riderId, deliveryCode);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error: " + e.getMessage()));
        }
    }
}
