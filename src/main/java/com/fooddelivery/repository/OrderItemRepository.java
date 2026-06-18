package com.fooddelivery.repository;

import com.fooddelivery.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi FROM OrderItem oi, Order o WHERE oi.orderId = o.id AND o.restaurantId = :restaurantId AND LOWER(o.status) != 'cancelled'")
    List<OrderItem> findByRestaurantId(@Param("restaurantId") Long restaurantId);
}