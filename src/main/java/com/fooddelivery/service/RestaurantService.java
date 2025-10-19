package com.fooddelivery.service;

import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.MenuItem;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class RestaurantService {
    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    public Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
    }

    public List<Restaurant> getRestaurantsByCuisine(String cuisine) {
        return restaurantRepository.findByCuisine(cuisine);
    }

    public List<MenuItem> getRestaurantMenu(Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    public MenuItem addMenuItem(Long restaurantId, MenuItem item) {
        Restaurant restaurant = getRestaurantById(restaurantId);
        item.setRestaurantId(restaurant.getId());
        return menuItemRepository.save(item);
    }

    public Restaurant addRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    public Restaurant updateRestaurant(Long id, Restaurant details) {
        Restaurant restaurant = getRestaurantById(id);
        restaurant.setName(details.getName());
        restaurant.setCuisine(details.getCuisine());
        restaurant.setRating(details.getRating());
        restaurant.setImageUrl(details.getImageUrl());
        return restaurantRepository.save(restaurant);
    }
}
