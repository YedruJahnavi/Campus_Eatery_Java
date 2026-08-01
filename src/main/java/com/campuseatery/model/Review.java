package com.campuseatery.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    
    private String orderId; // Reference to Order
    private String stallId; // Reference to Stall
    private String userId; // Reference to User
    
    private Integer rating;
    private String comment;
    private Boolean isDemo = false;
    
    @CreatedDate
    private Instant createdAt;
}
