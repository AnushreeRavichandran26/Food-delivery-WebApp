package com.fooddelivery.repository;

import com.fooddelivery.model.DeliveryAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeliveryAgentRepository extends JpaRepository<DeliveryAgent, Long> {
    List<DeliveryAgent> findByRatingGreaterThanEqual(Double rating);

    // ✅ ADD THIS: Get random delivery agent
    @Query(value = "SELECT * FROM delivery_agents ORDER BY RAND() LIMIT 1", nativeQuery = true)
    DeliveryAgent findRandomAgent();
}