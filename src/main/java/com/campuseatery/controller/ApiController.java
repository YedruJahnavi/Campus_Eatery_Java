package com.campuseatery.controller;

import com.campuseatery.service.ApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final ApiService apiService;

    @Value("${clerk.publishable-key}")
    private String clerkPublishableKey;

    @Value("${clerk.frontend-api}")
    private String clerkFrontendApi;

    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        return ResponseEntity.ok(Map.of(
            "clerkPublishableKey", clerkPublishableKey,
            "frontendApi", clerkFrontendApi
        ));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<?> getRecommendations(
            @RequestParam(defaultValue = "5") int limit,
            @RequestHeader(value = "X-User-Id", defaultValue = "dummy_student_id") String userId) {
        try {
            int safeLimit = Math.max(1, Math.min(50, limit));
            return ResponseEntity.ok(apiService.getRecommendations(userId, safeLimit));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }

    @GetMapping("/vendors")
    public ResponseEntity<?> getVendors(@RequestParam(required = false) String search) {
        try {
            return ResponseEntity.ok(Map.of("vendors", apiService.getVendors(search)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/vendors/{id}")
    public ResponseEntity<?> getVendorById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(apiService.getVendorById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }
}
