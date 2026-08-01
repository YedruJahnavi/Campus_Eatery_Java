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
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
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

        // TODO: In a real app, integrate Clerk SDK here to update publicMetadata
        // clerkClient.users.updateUserMetadata(clerkId, { publicMetadata: { approval_status: 'pending_approval' } });
    }
}
