package com.campuseatery.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "orders")
public class Order {
    
    @Id
    private String id;

    private String studentId; // Reference to User
    private String stallId; // Reference to Stall
    private String riderId; // Reference to User
    
    private String status = "placed";
    private String idempotencyKey;
    
    private Double foodTotal;
    private Double deliveryFee;
    private Double platformFee;
    private Double gst;
    private Double grandTotal;
    
    private String paymentStatus;
    private String paymentTransactionId;
    
    private String deliveryCode;
    private String deliveryAddressId; // Reference to DeliveryAddress
    private Boolean isDemo = false;
    
    private List<OrderItem> items;

    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;

    @Data
    public static class OrderItem {
        private String menuItemId; // Reference to MenuItem
        private String name = "";
        private Integer quantity;
        private Double unitPricePaise;
    }
}
