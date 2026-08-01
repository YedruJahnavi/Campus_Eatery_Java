package com.campuseatery.controller;

import com.campuseatery.dto.MenuItemDto;
import com.campuseatery.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDOR')")
public class VendorController {

    private final VendorService vendorService;

    @GetMapping("/menu")
    public ResponseEntity<?> getMenu(@RequestHeader("X-User-Id") String vendorId) {
        try {
            return ResponseEntity.ok(vendorService.getMenuItems(vendorId));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }

    @PostMapping("/menu")
    public ResponseEntity<?> addMenuItem(@RequestHeader("X-User-Id") String vendorId, @RequestBody MenuItemDto dto) {
        try {
            return ResponseEntity.ok(vendorService.addMenuItem(vendorId, dto));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Failed to create menu item"));
        }
    }

    @PutMapping("/menu/{id}")
    public ResponseEntity<?> updateMenuItem(
            @RequestHeader("X-User-Id") String vendorId,
            @PathVariable String id,
            @RequestBody MenuItemDto dto) {
        try {
            return ResponseEntity.ok(vendorService.updateMenuItem(vendorId, id, dto));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("detail", "Unauthorized or Stall not found"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Failed to update menu item"));
        }
    }

    @DeleteMapping("/menu/{id}")
    public ResponseEntity<?> deleteMenuItem(
            @RequestHeader("X-User-Id") String vendorId,
            @PathVariable String id) {
        try {
            vendorService.deleteMenuItem(vendorId, id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("detail", "Unauthorized or Stall not found"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Failed to delete menu item"));
        }
    }
}
