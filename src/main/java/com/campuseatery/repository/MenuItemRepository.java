package com.campuseatery.repository;

import com.campuseatery.model.MenuItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends MongoRepository<MenuItem, String> {
    List<MenuItem> findByStallId(String stallId);
    List<MenuItem> findByIsAvailableTrue();
    void deleteByIsDemoTrue();
    long countByIsDemoTrue();
}
