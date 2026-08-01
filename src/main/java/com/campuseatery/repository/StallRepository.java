package com.campuseatery.repository;

import com.campuseatery.model.Stall;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StallRepository extends MongoRepository<Stall, String> {
    Stall findByVendorId(String vendorId);
    java.util.List<Stall> findByIsActiveTrue();
    void deleteByIsDemoTrue();
    long countByIsDemoTrue();
}
