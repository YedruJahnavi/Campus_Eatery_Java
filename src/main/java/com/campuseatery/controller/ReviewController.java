package com.campuseatery.controller;

import com.campuseatery.dto.ReviewCreateDto;
import com.campuseatery.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<?> submitReview(@RequestHeader("X-User-Id") String userId, @RequestBody ReviewCreateDto dto) {
        try {
            return ResponseEntity.status(201).body(reviewService.submitReview(userId, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("detail", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }

    @GetMapping("/{stallId}")
    public ResponseEntity<?> getStallReviews(@PathVariable String stallId) {
        try {
            return ResponseEntity.ok(reviewService.getStallReviews(stallId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("detail", "Internal server error"));
        }
    }
}
