package com.campuseatery.service;

import com.campuseatery.dto.AddressDto;
import com.campuseatery.dto.VendorRequestDto;
import com.campuseatery.model.DeliveryAddress;
import com.campuseatery.model.Stall;
import com.campuseatery.model.User;
import com.campuseatery.repository.DeliveryAddressRepository;
import com.campuseatery.repository.StallRepository;
import com.campuseatery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DeliveryAddressRepository addressRepository;
    private final StallRepository stallRepository;

    public DeliveryAddress getAddress(String userId) {
        List<DeliveryAddress> addresses = addressRepository.findByUserId(userId);
        if (addresses.isEmpty()) {
            DeliveryAddress emptyAddress = new DeliveryAddress();
            emptyAddress.setAddressLine("");
            emptyAddress.setLabel("");
            return emptyAddress;
        }
        return addresses.get(0);
    }

    public DeliveryAddress updateAddress(String userId, AddressDto dto) {
        if (dto.getAddressLine() == null || dto.getAddressLine().trim().isEmpty()) {
            throw new IllegalArgumentException("Address line is required");
        }

        List<DeliveryAddress> addresses = addressRepository.findByUserId(userId);
        DeliveryAddress address;
        if (addresses.isEmpty()) {
            address = new DeliveryAddress();
            address.setUserId(userId);
        } else {
            address = addresses.get(0);
        }
        
        address.setAddressLine(dto.getAddressLine());
        address.setLabel(dto.getLabel() != null ? dto.getLabel() : "Campus");
        
        return addressRepository.save(address);
    }

    public User getMe(String userId) {
        return userRepository.findById(userId).orElseGet(() -> {
            User user = new User();
            user.setId(userId);
            String lowerId = userId.toLowerCase();
            if (lowerId.contains("admin")) {
                user.setRole("admin");
                user.setApprovalStatus("approved");
            } else if (lowerId.contains("vendor") || lowerId.contains("stall")) {
                user.setRole("vendor");
                user.setApprovalStatus("approved");
            } else {
                user.setRole("customer");
            }
            user.setEmail(userId.contains("@") ? userId : userId + "@campus.edu");
            return userRepository.save(user);
        });
    public User updateProfile(String userId, com.campuseatery.dto.UserProfileDto dto) {
        User user = getMe(userId);
        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getMobileNumber() != null) user.setMobileNumber(dto.getMobileNumber());
        if (dto.getCollegeName() != null) user.setCollegeName(dto.getCollegeName());
        if (dto.getCollegeLocation() != null) user.setCollegeLocation(dto.getCollegeLocation());
        if (dto.getStudyBranch() != null) user.setStudyBranch(dto.getStudyBranch());
        if (dto.getYearOfStudy() != null) user.setYearOfStudy(dto.getYearOfStudy());
        if (dto.getRegistrationNumber() != null) user.setRegistrationNumber(dto.getRegistrationNumber());
        return userRepository.save(user);
    }

    public void processVendorRequest(String clerkId, VendorRequestDto dto) {
        User user = userRepository.findById(clerkId).orElse(new User());
        if (user.getId() == null) {
            user.setId(clerkId);
            user.setEmail(clerkId + "@placeholder.com");
            user.setRole("customer");
        }

        user.setMobileNumber(dto.getMobileNumber());
        user.setCollegeLocation(dto.getCollegeLocation());
        user.setApprovalStatus("pending_approval");
        userRepository.save(user);

        Stall stall = stallRepository.findByVendorId(clerkId);
        if (stall == null) {
            stall = new Stall();
            stall.setVendorId(clerkId);
        }
        stall.setName(dto.getStallName());
        stall.setFssaiLicense(dto.getFssaiLicense());
        stall.setCollegeLocation(dto.getCollegeLocation());
        stall.setDescription(dto.getWhatTheySell());
        stall.setIsActive(false);
        stallRepository.save(stall);

        String clerkSecretKey = System.getenv("CLERK_SECRET_KEY");
        if (clerkSecretKey != null && !clerkSecretKey.isEmpty()) {
            try {
                org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.set("Authorization", "Bearer " + clerkSecretKey);
                headers.set("Content-Type", "application/json");

                String payload = "{ \"public_metadata\": { \"role\": \"customer\", \"approval_status\": \"pending_approval\" } }";
                org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(payload, headers);
                
                restTemplate.exchange(
                    "https://api.clerk.com/v1/users/" + clerkId + "/metadata", 
                    org.springframework.http.HttpMethod.PATCH, 
                    entity, 
                    String.class
                );
            } catch (Exception e) {
                System.err.println("Failed to update Clerk metadata: " + e.getMessage());
            }
        }
    }
}
