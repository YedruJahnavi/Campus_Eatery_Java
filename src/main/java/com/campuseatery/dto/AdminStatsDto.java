package com.campuseatery.dto;

import lombok.Data;

@Data
public class AdminStatsDto {
    private long totalUsers;
    private long totalCustomers;
    private long totalVendors;
    private long totalOrders;
    private double totalRevenue;
}
