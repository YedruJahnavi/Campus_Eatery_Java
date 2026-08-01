package com.campuseatery.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "rider_profiles")
public class RiderProfile {
    @Id
    private String id;
    
    private String userId; // Reference to User
    private Boolean isOnline = false;
    private Boolean bankAccountVerified = false;
    private String bankAccountMasked;
    private String upiId;
    
    private String currentLat;
    private String currentLon;
}
