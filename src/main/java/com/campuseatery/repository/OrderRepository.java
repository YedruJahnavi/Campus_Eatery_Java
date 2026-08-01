package com.campuseatery.repository;

import com.campuseatery.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByStudentId(String studentId);
    List<Order> findByStallId(String stallId);
    List<Order> findByRiderId(String riderId);
    List<Order> findByStatus(String status);
    void deleteByIsDemoTrue();
    long countByIsDemoTrue();
}
