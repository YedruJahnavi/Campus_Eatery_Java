package com.campuseatery.repository;

import com.campuseatery.model.DeliveryAddress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryAddressRepository extends MongoRepository<DeliveryAddress, String> {
    List<DeliveryAddress> findByUserId(String userId);
}
