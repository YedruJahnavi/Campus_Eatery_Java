package com.campuseatery.service;

import com.campuseatery.model.DeliveryAddress;
import com.campuseatery.repository.DeliveryAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryAddressService {

    private final DeliveryAddressRepository repository;

    public List<DeliveryAddress> getUserAddresses(String userId) {
        return repository.findByUserId(userId);
    }

    public DeliveryAddress addAddress(String userId, DeliveryAddress address) {
        address.setUserId(userId);
        
        // If this is their first address or explicitly marked as default, handle defaults
        List<DeliveryAddress> existing = repository.findByUserId(userId);
        if (existing.isEmpty()) {
            address.setDefault(true);
        } else if (address.isDefault()) {
            clearOtherDefaults(userId);
        }

        return repository.save(address);
    }

    public DeliveryAddress updateAddress(String userId, String addressId, DeliveryAddress updatedAddress) {
        DeliveryAddress existing = repository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        if (!existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        existing.setLabel(updatedAddress.getLabel());
        existing.setAddressLine(updatedAddress.getAddressLine());
        existing.setAddressLine1(updatedAddress.getAddressLine1());
        existing.setAddressLine2(updatedAddress.getAddressLine2());
        existing.setCity(updatedAddress.getCity());
        existing.setPincode(updatedAddress.getPincode());
        existing.setLat(updatedAddress.getLat());
        existing.setLon(updatedAddress.getLon());

        if (updatedAddress.isDefault() && !existing.isDefault()) {
            clearOtherDefaults(userId);
            existing.setDefault(true);
        }

        return repository.save(existing);
    }

    public void deleteAddress(String userId, String addressId) {
        DeliveryAddress existing = repository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        if (!existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }
        
        repository.delete(existing);
        
        // If they deleted the default address, make another one default if it exists
        if (existing.isDefault()) {
            List<DeliveryAddress> remaining = repository.findByUserId(userId);
            if (!remaining.isEmpty()) {
                DeliveryAddress nextDefault = remaining.get(0);
                nextDefault.setDefault(true);
                repository.save(nextDefault);
            }
        }
    }

    public void setDefaultAddress(String userId, String addressId) {
        DeliveryAddress existing = repository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        if (!existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        clearOtherDefaults(userId);
        existing.setDefault(true);
        repository.save(existing);
    }

    private void clearOtherDefaults(String userId) {
        List<DeliveryAddress> addresses = repository.findByUserId(userId);
        for (DeliveryAddress addr : addresses) {
            if (addr.isDefault()) {
                addr.setDefault(false);
                repository.save(addr);
            }
        }
    }
}
