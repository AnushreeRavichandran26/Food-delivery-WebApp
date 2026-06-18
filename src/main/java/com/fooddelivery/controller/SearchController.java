package com.fooddelivery.controller;

import com.fooddelivery.model.MenuItem;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchController {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "type", defaultValue = "all") String type,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "minRating", required = false) Double minRating) {

        List<Restaurant> matchedRestaurants = new ArrayList<>();
        List<MenuItem> matchedMenuItems = new ArrayList<>();

        String searchKeyword = (q != null) ? q.trim() : "";

        // 1. Search Restaurants (if type is all or restaurant)
        if ("all".equalsIgnoreCase(type) || "restaurant".equalsIgnoreCase(type)) {
            matchedRestaurants = restaurantRepository.searchRestaurants(searchKeyword, minRating);
        }

        // 2. Search Menu Items (if type is all or dish)
        if ("all".equalsIgnoreCase(type) || "dish".equalsIgnoreCase(type)) {
            matchedMenuItems = menuItemRepository.searchMenuItems(searchKeyword, minPrice, maxPrice, minRating);

            // Populate transient restaurantName field on each found MenuItem using a batch query
            if (!matchedMenuItems.isEmpty()) {
                Set<Long> restaurantIds = matchedMenuItems.stream()
                        .map(MenuItem::getRestaurantId)
                        .collect(Collectors.toSet());
                List<Restaurant> restaurants = restaurantRepository.findAllById(restaurantIds);
                Map<Long, String> restaurantNameMap = restaurants.stream()
                        .collect(Collectors.toMap(Restaurant::getId, Restaurant::getName));
                for (MenuItem item : matchedMenuItems) {
                    item.setRestaurantName(restaurantNameMap.getOrDefault(item.getRestaurantId(), "Unknown Restaurant"));
                }
            }
        }

        return ResponseEntity.ok(new SearchResponse(matchedRestaurants, matchedMenuItems));
    }

    public static class SearchResponse {
        private List<Restaurant> restaurants;
        private List<MenuItem> menuItems;

        public SearchResponse(List<Restaurant> restaurants, List<MenuItem> menuItems) {
            this.restaurants = restaurants;
            this.menuItems = menuItems;
        }

        public List<Restaurant> getRestaurants() {
            return restaurants;
        }

        public void setRestaurants(List<Restaurant> restaurants) {
            this.restaurants = restaurants;
        }

        public List<MenuItem> getMenuItems() {
            return menuItems;
        }

        public void setMenuItems(List<MenuItem> menuItems) {
            this.menuItems = menuItems;
        }
    }
}
