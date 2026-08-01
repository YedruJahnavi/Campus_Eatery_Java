package com.campuseatery.repository;

import com.campuseatery.model.PayoutTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayoutTransactionRepository extends MongoRepository<PayoutTransaction, String> {
    List<PayoutTransaction> findByRiderId(String riderId);
}
