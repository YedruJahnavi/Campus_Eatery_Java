package com.campuseatery.dto;

import lombok.Data;

@Data
public class AddressDto {
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String pincode;
    private String label;
}
