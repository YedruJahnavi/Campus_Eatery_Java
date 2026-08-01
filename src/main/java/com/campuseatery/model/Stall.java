package com.campuseatery.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "stalls")
public class Stall {
    @Id
    private String id;
    
    private String vendorId; // Reference to User
    private String name;
    private String description;
    private String fssaiLicense;
    private String collegeLocation;
    private String gstNumber;
    private Boolean isActive = true;
    private Boolean isDemo = false;
    private Double rating;
    private Integer prepTimeMinutes;
    private String imageUrl;
    private Double lat;
    private Double lon;
    
    @CreatedDate
    private Instant createdAt;
}
