package com.fooddelivery.repository;

import com.fooddelivery.model.DeliveryAgent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryAgentRepository extends JpaRepository<DeliveryAgent, Long> {

    List<DeliveryAgent> findByRatingGreaterThanEqual(Double rating);

    // ✅ DATABASE-INDEPENDENT random agent
    @Query("SELECT d FROM DeliveryAgent d ORDER BY function('RAND')")
    List<DeliveryAgent> findRandomAgent(Pageable pageable);
}