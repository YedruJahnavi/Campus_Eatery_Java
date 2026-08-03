package com.campuseatery.controller;

import com.campuseatery.dto.AddressDto;
import com.campuseatery.dto.VendorRequestDto;
import com.campuseatery.model.DeliveryAddress;
import com.campuseatery.model.User;
import com.campuseatery.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // TODO: In a real app, userId should come from Spring Security Context (Clerk JWT token)
    // For now, we simulate it via a custom header "X-User-Id"

    @GetMapping("/address")
    public ResponseEntity<DeliveryAddress> getAddress(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(userService.getAddress(userId));
    }

    @PutMapping("/address")
    public ResponseEntity<?> updateAddress(@RequestHeader("X-User-Id") String userId, @RequestBody AddressDto dto) {
        try {
            DeliveryAddress address = userService.updateAddress(userId, dto);
            return ResponseEntity.ok(address);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("X-User-Id") String userId) {
        try {
            User user = userService.getMe(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("X-User-Id") String userId, @RequestBody com.campuseatery.dto.UserProfileDto dto) {
        try {
            User user = userService.updateProfile(userId, dto);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/vendor-request")
    public ResponseEntity<?> vendorRequest(@RequestHeader("X-User-Id") String clerkId, @RequestBody VendorRequestDto dto) {
        try {
            userService.processVendorRequest(clerkId, dto);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
