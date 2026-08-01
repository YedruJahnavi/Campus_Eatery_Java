package com.campuseatery.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "menu_items")
public class MenuItem {
    @Id
    private String id;
    
    private String stallId; // Reference to Stall
    private String name;
    private String description;
    private Integer pricePaise;
    private String category;
    private Boolean isAvailable = true;
    private Boolean isDemo = false;
    private String imageUrl;
}
