package com.campuseatery.dto;

import lombok.Data;

@Data
public class MenuItemDto {
    private String name;
    private String description;
    private Integer pricePaise;
    private String category;
    private Boolean isAvailable;
    private String imageUrl;
}
