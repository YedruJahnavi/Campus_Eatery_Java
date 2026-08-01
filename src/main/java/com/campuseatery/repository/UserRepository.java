package com.campuseatery.repository;

import com.campuseatery.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String email);
    java.util.List<User> findByApprovalStatus(String status);
}
