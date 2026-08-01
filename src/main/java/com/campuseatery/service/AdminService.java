package com.campuseatery.service;

import com.campuseatery.dto.AdminStatsDto;
import com.campuseatery.model.Order;
import com.campuseatery.model.Stall;
import com.campuseatery.model.User;
import com.campuseatery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final StallRepository stallRepository;
    private final MenuItemRepository menuItemRepository;
    private final ReviewRepository reviewRepository;

    public void verifyAdmin(String adminId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null || !"admin".equals(admin.getRole())) {
            throw new SecurityException("Forbidden: Admins only");
        }
    }

    public AdminStatsDto getStats() {
        AdminStatsDto stats = new AdminStatsDto();
        stats.setTotalUsers(userRepository.count());
        
        List<User> users = userRepository.findAll();
        stats.setTotalCustomers(users.stream().filter(u -> "customer".equals(u.getRole()) || "student".equals(u.getRole())).count());
        stats.setTotalVendors(users.stream().filter(u -> "vendor".equals(u.getRole())).count());
        stats.setTotalOrders(orderRepository.count());
        
        double revenue = orderRepository.findAll().stream()
            .mapToDouble(Order::getGrandTotal)
            .sum();
        stats.setTotalRevenue(revenue);
        
        return stats;
    }

    public List<User> getUsers() {
        List<User> users = userRepository.findAll();
        users.sort((u1, u2) -> {
            if (u1.getCreatedAt() == null || u2.getCreatedAt() == null) return 0;
            return u2.getCreatedAt().compareTo(u1.getCreatedAt());
        });
        return users;
    }

    public User updateUserStatus(String id, Boolean isActive) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setIsActive(isActive);
        return userRepository.save(user);
    }

    public List<Map<String, Object>> getVendorRequests() {
        List<User> pendingUsers = userRepository.findByApprovalStatus("pending_approval");

        List<Stall> stalls = stallRepository.findAll();

        return pendingUsers.stream().map(user -> {
            Stall userStall = stalls.stream()
                .filter(s -> s.getVendorId().equals(user.getId()))
                .findFirst()
                .orElse(new Stall());

            Map<String, Object> map = new HashMap<>();
            map.put("user", user);
            map.put("stall_name", userStall.getName());
            map.put("fssai_license", userStall.getFssaiLicense());
            map.put("description", userStall.getDescription());
            map.put("college_location", userStall.getCollegeLocation());
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void approveVendor(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole("vendor");
        user.setApprovalStatus("approved");
        userRepository.save(user);

        Stall stall = stallRepository.findByVendorId(user.getId());
        if (stall != null) {
            stall.setIsActive(true);
            stallRepository.save(stall);
        }

        // TODO: Sync updated role and status to Clerk
        // clerkClient.users.updateUserMetadata(user._id, { publicMetadata: { role: 'vendor', approval_status: 'approved', onboardingComplete: true } });
    }

    @Transactional
    public Map<String, Long> clearDemoData() {
        // Simple manual implementation. In a real app we would use custom repository queries like deleteByIsDemoTrue()
        
        long deletedReviews = reviewRepository.countByIsDemoTrue();
        reviewRepository.deleteByIsDemoTrue();
        
        long deletedOrders = orderRepository.countByIsDemoTrue();
        orderRepository.deleteByIsDemoTrue();
        
        long deletedMenuItems = menuItemRepository.countByIsDemoTrue();
        menuItemRepository.deleteByIsDemoTrue();
        
        long deletedStalls = stallRepository.countByIsDemoTrue();
        stallRepository.deleteByIsDemoTrue();

        Map<String, Long> result = new HashMap<>();
        result.put("reviews", deletedReviews);
        result.put("orders", deletedOrders);
        result.put("menuItems", deletedMenuItems);
        result.put("stalls", deletedStalls);
        return result;
    }
}
