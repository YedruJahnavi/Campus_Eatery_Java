package com.campuseatery.repository;

import com.campuseatery.model.RiderProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiderProfileRepository extends MongoRepository<RiderProfile, String> {
    Optional<RiderProfile> findByUserId(String userId);
}
