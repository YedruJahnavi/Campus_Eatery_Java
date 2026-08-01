package com.campuseatery.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id; // Clerk user ID
    
    private String email;
    private String role = "customer";
    private Boolean isActive = true;
    private Boolean isVerified = false;
    
    private String name;
    private String mobileNumber;
    private String collegeName;
    private String collegeLocation;
    private String registrationNumber;
    private String yearOfStudy;
    private String studyBranch;
    private String approvalStatus = "approved";
    private String deviceToken;

    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
}
