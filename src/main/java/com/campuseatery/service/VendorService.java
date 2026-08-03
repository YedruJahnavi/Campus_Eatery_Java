package com.campuseatery.service;

import com.campuseatery.dto.MenuItemDto;
import com.campuseatery.model.MenuItem;
import com.campuseatery.model.Stall;
import com.campuseatery.repository.MenuItemRepository;
import com.campuseatery.repository.StallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final StallRepository stallRepository;
    private final MenuItemRepository menuItemRepository;

    private Stall getVendorStall(String vendorId) {
        Stall stall = stallRepository.findByVendorId(vendorId);
        if (stall == null) {
            stall = new Stall();
            stall.setVendorId(vendorId);
            stall.setName("Campus Stall");
            stall.setDescription("Fresh campus delicacies & snacks.");
            stall.setCollegeLocation("Main Campus");
            stall.setIsActive(true);
            stall = stallRepository.save(stall);
        }
        return stall;
    }

    public List<MenuItem> getMenuItems(String vendorId) {
        Stall stall = getVendorStall(vendorId);
        return menuItemRepository.findByStallId(stall.getId());
    }

    public MenuItem addMenuItem(String vendorId, MenuItemDto dto) {
        Stall stall = getVendorStall(vendorId);
        
        MenuItem item = new MenuItem();
        item.setStallId(stall.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        if (dto.getPricePaise() != null && dto.getPricePaise() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        item.setPricePaise(dto.getPricePaise());
        item.setCategory(dto.getCategory() != null ? dto.getCategory() : "General");
        item.setIsAvailable(dto.getIsAvailable() != null ? dto.getIsAvailable() : true);
        item.setImageUrl(dto.getImageUrl() != null ? dto.getImageUrl() : "");
        
        return menuItemRepository.save(item);
    }

    public MenuItem updateMenuItem(String vendorId, String itemId, MenuItemDto dto) {
        Stall stall = getVendorStall(vendorId);
        
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
                
        if (!item.getStallId().equals(stall.getId())) {
            throw new SecurityException("Unauthorized");
        }

        if (dto.getName() != null) item.setName(dto.getName());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        if (dto.getPricePaise() != null) {
            if (dto.getPricePaise() < 0) {
                throw new IllegalArgumentException("Price cannot be negative");
            }
            item.setPricePaise(dto.getPricePaise());
        }
        if (dto.getCategory() != null) item.setCategory(dto.getCategory());
        if (dto.getIsAvailable() != null) item.setIsAvailable(dto.getIsAvailable());
        if (dto.getImageUrl() != null) item.setImageUrl(dto.getImageUrl());

        return menuItemRepository.save(item);
    }

    public void deleteMenuItem(String vendorId, String itemId) {
        Stall stall = getVendorStall(vendorId);
        
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
                
        if (!item.getStallId().equals(stall.getId())) {
            throw new SecurityException("Unauthorized");
        }
        
        menuItemRepository.delete(item);
    }
}
