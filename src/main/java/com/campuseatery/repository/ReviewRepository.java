package com.campuseatery.repository;

import com.campuseatery.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByStallId(String stallId);
    Optional<Review> findByOrderId(String orderId);
    List<Review> findByOrderIdIn(List<String> orderIds);
    void deleteByIsDemoTrue();
    long countByIsDemoTrue();
}
