package com.campuseatery.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Date;

@Data
@Document(collection = "payout_transactions")
public class PayoutTransaction {
    @Id
    private String id;
    
    private String riderId; // Reference to User
    private Double amount;
    private String method;
    private String bankAccountMasked;
    private String utrNumber;
    private String status = "initiated";
    private Date settledAt;
    
    @CreatedDate
    private Instant initiatedAt; // Mapped from 'initiated_at'
}
