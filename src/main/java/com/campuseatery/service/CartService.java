package com.campuseatery.service;

import com.campuseatery.dto.CartItemDto;
import com.campuseatery.model.Cart;
import com.campuseatery.model.MenuItem;
import com.campuseatery.repository.CartRepository;
import com.campuseatery.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;

    public Cart getCart(String userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setItems(new ArrayList<>());
            newCart.setTotal(0.0);
            return newCart;
        });
    }

    public Cart addItem(String userId, CartItemDto dto) {
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new IllegalArgumentException("Invalid quantity");
        }

        Cart cart = getCart(userId);
        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }
        
        MenuItem menuItem = menuItemRepository.findById(dto.getMenuItemId())
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));

        if (!cart.getItems().isEmpty()) {
            MenuItem firstCartItem = menuItemRepository.findById(cart.getItems().get(0).getMenuItemId()).orElse(null);
            if (firstCartItem != null && !firstCartItem.getStallId().equals(menuItem.getStallId())) {
                throw new IllegalArgumentException("You can only order from one stall at a time. Please clear your cart first.");
            }
        }

        boolean itemExists = false;
        for (Cart.CartItem item : cart.getItems()) {
            if (item.getMenuItemId().equals(dto.getMenuItemId())) {
                item.setQuantity(item.getQuantity() + dto.getQuantity());
                itemExists = true;
                break;
            }
        }

        if (!itemExists) {
            Cart.CartItem newItem = new Cart.CartItem();
            newItem.setMenuItemId(dto.getMenuItemId());
            newItem.setQuantity(dto.getQuantity());
            
            double itemPrice = 0.0;
            if (menuItem.getPricePaise() != null) {
                itemPrice = (double) menuItem.getPricePaise();
            }
            newItem.setPrice(itemPrice);
            
            newItem.setName(menuItem.getName());
            cart.getItems().add(newItem);
        }

        calculateTotal(cart);
        return cartRepository.save(cart);
    }

    public Cart removeItem(String userId, String itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        cart.getItems().removeIf(item -> item.getMenuItemId().equals(itemId));
        calculateTotal(cart);
        
        return cartRepository.save(cart);
    }

    public Cart updateItem(String userId, String itemId, String action) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        for (int i = 0; i < cart.getItems().size(); i++) {
            Cart.CartItem item = cart.getItems().get(i);
            if (item.getMenuItemId().equals(itemId)) {
                if ("increment".equals(action)) {
                    item.setQuantity(item.getQuantity() + 1);
                } else if ("decrement".equals(action)) {
                    item.setQuantity(item.getQuantity() - 1);
                    if (item.getQuantity() <= 0) {
                        cart.getItems().remove(i);
                    }
                }
                break;
            }
        }

        calculateTotal(cart);
        return cartRepository.save(cart);
    }

    private void calculateTotal(Cart cart) {
        double total = 0;
        for (Cart.CartItem item : cart.getItems()) {
            total += item.getPrice() * item.getQuantity();
        }
        cart.setTotal(total);
    }
}
