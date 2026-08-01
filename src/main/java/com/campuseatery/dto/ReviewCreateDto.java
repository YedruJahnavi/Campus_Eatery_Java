package com.campuseatery.dto;

import lombok.Data;

@Data
public class ReviewCreateDto {
    private String orderId;
    private String stallId;
    private Integer rating;
    private String comment;
}
