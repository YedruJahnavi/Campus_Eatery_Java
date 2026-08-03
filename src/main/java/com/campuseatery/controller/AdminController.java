package com.campuseatery.controller;

import com.campuseatery.dto.AdminLoginDto;
import com.campuseatery.dto.UserStatusUpdateDto;
import com.campuseatery.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // A helper method to simulate the requireAdmin middleware
    private void requireAdmin(String adminId) {
        adminService.verifyAdmin(adminId);
    }

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> login(@RequestBody AdminLoginDto dto) {
        if (adminService.authenticateAdmin(dto.getUsername(), dto.getPassword())) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "token", "admin_session_token",
                "adminId", "admin_demo_1"
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of("detail", "Invalid admin credentials"));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@RequestHeader("X-User-Id") String adminId) {
        try {
            requireAdmin(adminId);
            return ResponseEntity.ok(adminService.getStats());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestHeader("X-User-Id") String adminId) {
        try {
            requireAdmin(adminId);
            return ResponseEntity.ok(adminService.getUsers());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(
            @RequestHeader("X-User-Id") String adminId,
            @PathVariable String id,
            @RequestBody UserStatusUpdateDto dto) {
        try {
            requireAdmin(adminId);
            return ResponseEntity.ok(adminService.updateUserStatus(id, dto.getIsActive()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("detail", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }

    @GetMapping("/vendor-requests")
    public ResponseEntity<?> getVendorRequests(@RequestHeader("X-User-Id") String adminId) {
        try {
            requireAdmin(adminId);
            return ResponseEntity.ok(adminService.getVendorRequests());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }

    @PostMapping("/approve-vendor/{id}")
    public ResponseEntity<?> approveVendor(
            @RequestHeader("X-User-Id") String adminId,
            @PathVariable String id) {
        try {
            requireAdmin(adminId);
            adminService.approveVendor(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("detail", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }

    @DeleteMapping("/demo-data")
    public ResponseEntity<?> clearDemoData(@RequestHeader("X-User-Id") String adminId) {
        try {
            requireAdmin(adminId);
            return ResponseEntity.ok(Map.of("success", true, "deleted", adminService.clearDemoData()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }
}
