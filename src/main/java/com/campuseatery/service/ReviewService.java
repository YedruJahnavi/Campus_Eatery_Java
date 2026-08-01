package com.campuseatery.service;

import com.campuseatery.dto.ReviewCreateDto;
import com.campuseatery.model.Order;
import com.campuseatery.model.Review;
import com.campuseatery.model.Stall;
import com.campuseatery.repository.OrderRepository;
import com.campuseatery.repository.ReviewRepository;
import com.campuseatery.repository.StallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final StallRepository stallRepository;

    public Review submitReview(String userId, ReviewCreateDto dto) {
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
                
        if (!order.getStudentId().equals(userId) || !"delivered".equals(order.getStatus())) {
            throw new IllegalArgumentException("Order not eligible for review");
        }

        if (reviewRepository.findByOrderId(order.getId()).isPresent()) {
            throw new IllegalArgumentException("Review already exists for this order");
        }

        Review review = new Review();
        review.setOrderId(dto.getOrderId());
        review.setStallId(order.getStallId());
        review.setUserId(userId);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        
        Review savedReview = reviewRepository.save(review);

        // Update Stall Average Rating
        List<Review> stallReviews = reviewRepository.findByStallId(order.getStallId());
        double avg = stallReviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        
        Stall stall = stallRepository.findById(order.getStallId()).orElse(null);
        if (stall != null) {
            stall.setRating(Math.round(avg * 10.0) / 10.0);
            stallRepository.save(stall);
        }

        return savedReview;
    }

    public List<Review> getStallReviews(String stallId) {
        // TODO: In a real app we would populate user_id -> name. We can map this later via UserRepository.
        List<Review> reviews = reviewRepository.findByStallId(stallId);
        reviews.sort((r1, r2) -> {
            if (r1.getCreatedAt() == null || r2.getCreatedAt() == null) return 0;
            return r2.getCreatedAt().compareTo(r1.getCreatedAt());
        });
        return reviews;
    }
}
