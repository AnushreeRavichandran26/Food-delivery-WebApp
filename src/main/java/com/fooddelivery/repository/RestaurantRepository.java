package com.fooddelivery.repository;

import com.fooddelivery.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findByName(String name);
    List<Restaurant> findByCuisine(String cuisine);
    List<Restaurant> findByRatingGreaterThanEqual(Double rating);

    @Query("SELECT r FROM Restaurant r WHERE " +
           "(:q IS NULL OR :q = '' OR LOWER(r.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(r.cuisine) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "AND (:minRating IS NULL OR r.rating >= :minRating)")
    List<Restaurant> searchRestaurants(@Param("q") String q, @Param("minRating") Double minRating);
}