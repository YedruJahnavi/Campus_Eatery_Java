package com.campuseatery.repository;

import com.campuseatery.model.RiderEarning;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiderEarningRepository extends MongoRepository<RiderEarning, String> {
    List<RiderEarning> findByRiderId(String riderId);
    Optional<RiderEarning> findByOrderId(String orderId);
}
