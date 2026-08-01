package com.campuseatery.dto;

import com.campuseatery.model.Order;
import lombok.Data;

@Data
public class OrderWithReviewDto {
    private Order order;
    private Boolean hasReview;

    public OrderWithReviewDto(Order order, Boolean hasReview) {
        this.order = order;
        this.hasReview = hasReview;
    }
}
