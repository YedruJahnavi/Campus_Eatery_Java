package com.campuseatery.service;

import com.campuseatery.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartCleanupJob {

    private final CartRepository cartRepository;

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void cleanupAbandonedCarts() {
        log.info("Running abandoned cart cleanup job...");
        // Usually we would query for updated_at older than X hours.
        // For simplicity, we could just find all carts and check their updated_at if they have items.
        // Assuming there are not millions of carts for this demo.
        cartRepository.findAll().forEach(cart -> {
            if (cart.getItems() != null && !cart.getItems().isEmpty()) {
                if (cart.getUpdatedAt() != null && cart.getUpdatedAt().isBefore(Instant.now().minus(24, ChronoUnit.HOURS))) {
                    log.info("Cleaning up abandoned cart for user: {}", cart.getUserId());
                    cart.getItems().clear();
                    cart.setTotal(0.0);
                    cartRepository.save(cart);
                }
            }
        });
        log.info("Completed abandoned cart cleanup job.");
    }
}
