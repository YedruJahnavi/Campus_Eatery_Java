package com.campuseatery.service;

import com.campuseatery.model.MenuItem;
import com.campuseatery.model.Order;
import com.campuseatery.model.Stall;
import com.campuseatery.repository.MenuItemRepository;
import com.campuseatery.repository.OrderRepository;
import com.campuseatery.repository.StallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiService {

    private final StallRepository stallRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;

    public List<Map<String, Object>> getRecommendations(String userId, int limit) {
        // Simplified recommendation logic in Java memory
        List<Order> orders = orderRepository.findByStatus("delivered");

        Map<String, Integer> itemScores = new HashMap<>();

        // First try user specific
        orders.stream()
            .filter(o -> userId.equals(o.getStudentId()))
            .flatMap(o -> o.getItems().stream())
            .forEach(item -> itemScores.put(item.getMenuItemId(), itemScores.getOrDefault(item.getMenuItemId(), 0) + item.getQuantity()));

        // Fallback to globally popular
        if (itemScores.isEmpty()) {
            orders.stream()
                .flatMap(o -> o.getItems().stream())
                .forEach(item -> itemScores.put(item.getMenuItemId(), itemScores.getOrDefault(item.getMenuItemId(), 0) + item.getQuantity()));
        }

        List<Map.Entry<String, Integer>> sortedItems = itemScores.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toList());

        List<Map<String, Object>> recommendations = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sortedItems) {
            MenuItem item = menuItemRepository.findById(entry.getKey()).orElse(null);
            if (item != null) {
                Map<String, Object> rec = new HashMap<>();
                rec.put("item_id", item.getId());
                rec.put("name", item.getName());
                rec.put("price", item.getPricePaise());
                rec.put("score", entry.getValue());
                rec.put("image_url", item.getImageUrl());
                recommendations.add(rec);
            }
        }
        return recommendations;
    }

    public List<Map<String, Object>> getVendors(String search) {
        List<Stall> stalls = stallRepository.findByIsActiveTrue();

        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            stalls = stalls.stream()
                .filter(s -> s.getName().toLowerCase().contains(searchLower))
                .collect(Collectors.toList());
        }

        List<MenuItem> allMenuItems = menuItemRepository.findByIsAvailableTrue();

        return stalls.stream().map(stall -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", stall.getId());
            map.put("name", stall.getName());
            map.put("description", stall.getDescription());
            map.put("collegeLocation", stall.getCollegeLocation());
            map.put("imageUrl", stall.getImageUrl());
            map.put("rating", stall.getRating());
            
            List<MenuItem> stallItems = allMenuItems.stream()
                .filter(m -> m.getStallId().equals(stall.getId()))
                .collect(Collectors.toList());
            map.put("menuItems", stallItems);
            
            return map;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getVendorById(String id) {
        Stall stall = stallRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Stall not found"));
        
        List<MenuItem> menuItems = menuItemRepository.findByStallId(id).stream()
            .filter(m -> Boolean.TRUE.equals(m.getIsAvailable()))
            .collect(Collectors.toList());
            
        Map<String, Object> map = new HashMap<>();
        map.put("id", stall.getId());
        map.put("name", stall.getName());
        map.put("description", stall.getDescription());
        map.put("collegeLocation", stall.getCollegeLocation());
        map.put("imageUrl", stall.getImageUrl());
        map.put("rating", stall.getRating());
        map.put("menuItems", menuItems);
        return map;
    }
}
