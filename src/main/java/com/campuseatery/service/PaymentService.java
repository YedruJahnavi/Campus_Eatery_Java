package com.campuseatery.service;

import com.campuseatery.model.*;
import com.campuseatery.repository.*;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
    private final DeliveryAddressRepository addressRepository;
    private final StallRepository stallRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate simpMessagingTemplate;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() throws RazorpayException {
        if (razorpayKeyId != null && !razorpayKeyId.isEmpty() && razorpayKeySecret != null && !razorpayKeySecret.isEmpty()) {
            this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        }
    }

    @Transactional
    public Map<String, Object> createRazorpayOrder(String studentId) throws RazorpayException {
        Cart cart = cartRepository.findByUserId(studentId).orElse(null);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

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
        Double grandTotal = foodTotal + gst; // Total in INR
        
        int amountInPaise = (int) Math.round(grandTotal * 100);
        if (amountInPaise < 100) {
            throw new IllegalArgumentException("Order amount must be at least ₹1.00");
        }

        // Create local order in CREATED/PENDING_PAYMENT state
        List<com.campuseatery.model.Order.OrderItem> orderItems = cart.getItems().stream().map(item -> {
            com.campuseatery.model.Order.OrderItem oi = new com.campuseatery.model.Order.OrderItem();
            oi.setMenuItemId(item.getMenuItemId());
            oi.setName(item.getName());
            oi.setQuantity(item.getQuantity());
            oi.setUnitPricePaise(item.getPrice());
            return oi;
        }).collect(Collectors.toList());

        com.campuseatery.model.Order localOrder = new com.campuseatery.model.Order();
        localOrder.setStudentId(studentId);
        localOrder.setStallId(stallId);
        localOrder.setItems(orderItems);
        localOrder.setFoodTotal(foodTotal);
        localOrder.setDeliveryFee(0.0);
        localOrder.setPlatformFee(0.0);
        localOrder.setGst(gst);
        localOrder.setGrandTotal(grandTotal);
        localOrder.setStatus("PENDING_PAYMENT");
        localOrder.setIdempotencyKey(UUID.randomUUID().toString());
        localOrder.setDeliveryCode(String.valueOf((int)(Math.random() * 9000) + 1000));
        localOrder.setDeliveryAddressId(address.getId());
        
        com.campuseatery.model.Order savedLocalOrder = orderRepository.save(localOrder);

        // Create Razorpay Order
        if (razorpayClient == null) {
            throw new IllegalStateException("Razorpay credentials not configured");
        }

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", savedLocalOrder.getId());

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        return Map.of(
            "order_id", razorpayOrder.get("id"),
            "amount", amountInPaise,
            "currency", "INR",
            "local_order_id", savedLocalOrder.getId(),
            "key", razorpayKeyId
        );
    }

    @Transactional
    public boolean verifySignature(String localOrderId, String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            com.campuseatery.model.Order localOrder = orderRepository.findById(localOrderId)
                    .orElseThrow(() -> new IllegalArgumentException("Local order not found"));

            if (isValid) {
                // Payment success
                localOrder.setStatus("PLACED");
                localOrder.setPaymentStatus("PAID");
                localOrder.setPaymentTransactionId(razorpayPaymentId);
                
                com.campuseatery.model.Order updatedOrder = orderRepository.save(localOrder);

                // Notify vendor
                Stall stall = stallRepository.findById(localOrder.getStallId()).orElse(null);
                if (stall != null && stall.getVendorId() != null) {
                    simpMessagingTemplate.convertAndSend("/topic/orders/" + stall.getVendorId(), updatedOrder);
                }

                // Clear cart
                Cart cart = cartRepository.findByUserId(localOrder.getStudentId()).orElse(null);
                if (cart != null) {
                    cart.setItems(new ArrayList<>());
                    cart.setTotal(0.0);
                    cartRepository.save(cart);
                }
                return true;
            } else {
                // Payment failed validation
                localOrder.setStatus("PAYMENT_FAILED");
                orderRepository.save(localOrder);
                return false;
            }
        } catch (RazorpayException e) {
            return false;
        }
    }
}
