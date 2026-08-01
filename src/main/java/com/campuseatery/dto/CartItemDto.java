package com.campuseatery.dto;

import lombok.Data;

@Data
public class CartItemDto {
    private String menuItemId;
    private Integer quantity;
}
