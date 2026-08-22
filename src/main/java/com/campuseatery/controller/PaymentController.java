package com.campuseatery.controller;

import com.campuseatery.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestHeader("X-User-Id") String userId) {
        try {
            Map<String, Object> orderDetails = paymentService.createRazorpayOrder(userId);
            return ResponseEntity.ok(orderDetails);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Failed to create order: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, String> payload) {
        
        String razorpayPaymentId = payload.get("razorpay_payment_id");
        String razorpayOrderId = payload.get("razorpay_order_id");
        String razorpaySignature = payload.get("razorpay_signature");
        String localOrderId = payload.get("local_order_id");

        if (razorpayPaymentId == null || razorpayOrderId == null || razorpaySignature == null || localOrderId == null) {
            return ResponseEntity.badRequest().body(Map.of("detail", "Missing payment parameters"));
        }

        try {
            boolean isValid = paymentService.verifySignature(localOrderId, razorpayOrderId, razorpayPaymentId, razorpaySignature);
            if (isValid) {
                return ResponseEntity.ok(Map.of("success", true, "detail", "Payment verified successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "detail", "Invalid signature"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Verification failed: " + e.getMessage()));
        }
    }
}
