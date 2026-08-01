package com.campuseatery.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "carts")
public class Cart {
    @Id
    private String id;
    
    private String userId; // Reference to User
    private List<CartItem> items;
    private Double total = 0.0;
    
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
    
    @Data
    public static class CartItem {
        private String menuItemId; // Reference to MenuItem
        private String name;
        private Double price;
        private Integer quantity = 1;
    }
}
