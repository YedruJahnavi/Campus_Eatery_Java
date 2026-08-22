package com.campuseatery.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "delivery_addresses")
public class DeliveryAddress {
    @Id
    private String id;
    
    private String userId; // Reference to User
    private String label;
    private String addressLine;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String pincode;
    private Double lat;
    private Double lon;
    private boolean isDefault;
}
