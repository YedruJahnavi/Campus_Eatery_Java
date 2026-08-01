package com.campuseatery.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Date;

@Data
@Document(collection = "rider_earnings")
public class RiderEarning {
    @Id
    private String id;
    
    private String riderId; // Reference to User
    private String orderId; // Reference to Order
    
    private Double baseFee;
    private Double batchBonus;
    private Double peakBonus;
    private Double ratingBonus;
    private Double tip;
    private Double deductions;
    private Double netAmount;
    
    private String status = "pending";
    private Date clearedAt;
    
    @CreatedDate
    private Instant createdAt;
}
