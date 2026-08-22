package com.campuseatery.service;

import com.campuseatery.dto.OrderWithReviewDto;
import com.campuseatery.model.*;
import com.campuseatery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ReviewRepository reviewRepository;
    private final MenuItemRepository menuItemRepository;
    private final DeliveryAddressRepository addressRepository;
    private final StallRepository stallRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate simpMessagingTemplate;

    public List<OrderWithReviewDto> getStudentOrders(String studentId) {
        List<Order> orders = orderRepository.findByStudentId(studentId);
        // Sort descending by created_at
        orders.sort((o1, o2) -> {
            if (o1.getCreatedAt() == null || o2.getCreatedAt() == null) return 0;
            return o2.getCreatedAt().compareTo(o1.getCreatedAt());
        });

        List<String> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        
        Set<String> reviewedOrderIds = new HashSet<>();
        if (!orderIds.isEmpty()) {
            List<Review> reviews = reviewRepository.findByOrderIdIn(orderIds);
            for (Review r : reviews) {
                reviewedOrderIds.add(r.getOrderId());
            }
        }

        return orders.stream()
                .map(order -> new OrderWithReviewDto(order, reviewedOrderIds.contains(order.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public String checkout(String studentId) {
        Cart cart = cartRepository.findByUserId(studentId).orElse(null);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Get stall_id from first item
        Cart.CartItem firstItem = cart.getItems().get(0);
        MenuItem menuItem = menuItemRepository.findById(firstItem.getMenuItemId()).orElse(null);
        if (menuItem == null || menuItem.getStallId() == null) {
            throw new IllegalArgumentException("Menu item no longer exists or stall is unavailable");
        }
        String stallId = menuItem.getStallId();

        List<DeliveryAddress> addresses = addressRepository.findByUserId(studentId);
        if (addresses.isEmpty()) {
            throw new IllegalArgumentException("Please provide a delivery address before checkout.");
        }
        DeliveryAddress address = addresses.get(0);

        Double foodTotal = cart.getTotal();
        Double gst = Math.floor(foodTotal * 0.05);
        Double grandTotal = foodTotal + gst;

        List<Order.OrderItem> orderItems = cart.getItems().stream().map(item -> {
            Order.OrderItem oi = new Order.OrderItem();
            oi.setMenuItemId(item.getMenuItemId());
            oi.setName(item.getName());
            oi.setQuantity(item.getQuantity());
            oi.setUnitPricePaise(item.getPrice());
            return oi;
        }).collect(Collectors.toList());

        Order order = new Order();
        order.setStudentId(studentId);
        order.setStallId(stallId);
        order.setItems(orderItems);
        order.setFoodTotal(foodTotal);
        order.setDeliveryFee(0.0);
        order.setPlatformFee(0.0);
        order.setGst(gst);
        order.setGrandTotal(grandTotal);
        order.setStatus("PLACED");
        order.setIdempotencyKey(UUID.randomUUID().toString());
        order.setDeliveryCode(String.valueOf((int)(Math.random() * 9000) + 1000));
        order.setDeliveryAddressId(address.getId());

        Order savedOrder = orderRepository.save(order);

        Stall stall = stallRepository.findById(stallId).orElse(null);
        if (stall != null && stall.getVendorId() != null) {
            simpMessagingTemplate.convertAndSend("/topic/orders/" + stall.getVendorId(), savedOrder);
        }

        cart.setItems(new ArrayList<>());
        cart.setTotal(0.0);
        cartRepository.save(cart);

        return savedOrder.getId();
    }

    public List<Order> getVendorOrders(String vendorId) {
        Stall stall = stallRepository.findByVendorId(vendorId);
        if (stall == null) {
            throw new SecurityException("Forbidden: Not a vendor or no stall found");
        }

        List<Order> orders = orderRepository.findByStallId(stall.getId());
        orders.sort((o1, o2) -> {
            if (o1.getCreatedAt() == null || o2.getCreatedAt() == null) return 0;
            return o2.getCreatedAt().compareTo(o1.getCreatedAt());
        });
        return orders;
    }

    public Order updateOrderStatus(String vendorId, String orderId, String status) {
        Stall stall = stallRepository.findByVendorId(vendorId);
        if (stall == null) {
            throw new SecurityException("Forbidden: Not a vendor");
        }

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        if (!order.getStallId().equals(stall.getId())) {
            throw new SecurityException("Forbidden: Order does not belong to your stall");
        }

        order.setStatus(normalizeStatus(status));
        Order updatedOrder = orderRepository.save(order);

        simpMessagingTemplate.convertAndSend("/topic/user_" + order.getStudentId(), updatedOrder);
        simpMessagingTemplate.convertAndSend("/topic/orders/" + vendorId, updatedOrder);

        return updatedOrder;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Order status is required");
        }
        String normalized = status.trim().toUpperCase();
        if (!List.of("PLACED", "ACCEPTED", "PREPARING", "READY_FOR_PICKUP", "DELIVERED", "CANCELLED", "COMPLETED").contains(normalized)) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
        return normalized;
    }

    public List<Order> getAvailableDeliveries() {
        return orderRepository.findByStatus("READY_FOR_PICKUP").stream()
                .filter(order -> order.getRiderId() == null)
                .collect(Collectors.toList());
    }

    public List<Order> getMyDeliveries(String riderId) {
        return orderRepository.findByRiderId(riderId);
    }

    public Order acceptDelivery(String orderId, String riderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getRiderId() != null && !order.getRiderId().equals(riderId)) {
            throw new IllegalArgumentException("Order already assigned to another rider");
        }
        order.setRiderId(riderId);
        return orderRepository.save(order);
    }

    public Order completeDelivery(String orderId, String riderId, String deliveryCode) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!riderId.equals(order.getRiderId())) {
            throw new IllegalArgumentException("Not your assigned delivery");
        }
        if (deliveryCode == null || !deliveryCode.equals(order.getDeliveryCode())) {
            throw new IllegalArgumentException("Invalid delivery code");
        }
        order.setStatus("DELIVERED"); // or COMPLETED based on your flow
        Order updatedOrder = orderRepository.save(order);
        simpMessagingTemplate.convertAndSend("/topic/user_" + order.getStudentId(), updatedOrder);
        return updatedOrder;
    }
}
