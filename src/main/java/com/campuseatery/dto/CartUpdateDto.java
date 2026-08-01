package com.campuseatery.dto;

import lombok.Data;

@Data
public class CartUpdateDto {
    private String action; // "increment" or "decrement"
}
