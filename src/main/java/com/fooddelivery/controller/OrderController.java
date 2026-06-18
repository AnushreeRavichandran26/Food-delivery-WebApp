package com.fooddelivery.controller;

import com.fooddelivery.model.Order;
import com.fooddelivery.service.OrderService;
import com.fooddelivery.service.OrderStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderStreamService orderStreamService;

    @Autowired
    private com.fooddelivery.service.UserService userService;

    private boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        try {
            com.fooddelivery.model.User user = userService.getUserById(userId);
            return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        try {
            Order newOrder = orderService.createOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to create order: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (!isAdmin(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Admin role required");
        }
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Order not found");
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(
            @PathVariable String status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (!isAdmin(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Admin role required");
        }
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (!isAdmin(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Admin role required");
        }
        try {
            Order updatedOrder = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to update order: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/assign-agent")
    public ResponseEntity<?> assignDeliveryAgent(
            @PathVariable Long id,
            @RequestParam String agentName,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (!isAdmin(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Admin role required");
        }
        try {
            Order updatedOrder = orderService.assignDeliveryAgent(id, agentName);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to assign agent: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, @RequestParam(required = false) String reason) {
        try {
            Order cancelledOrder = orderService.cancelOrder(id, reason);
            return ResponseEntity.ok(cancelledOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot cancel order: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrderPost(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return cancelOrder(id, reason);
    }

    @GetMapping(value = "/stream/user/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUserOrders(@PathVariable Long userId) {
        return orderStreamService.registerUserEmitter(userId);
    }

    @GetMapping(value = "/stream/admin", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<?> streamAdminOrders(@RequestParam(value = "userId", required = false) Long userId) {
        if (!isAdmin(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(orderStreamService.registerAdminEmitter());
    }

    @GetMapping("/restaurant/{restaurantId}/analytics")
    public ResponseEntity<?> getRestaurantAnalytics(
            @PathVariable Long restaurantId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (!isAdmin(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Admin role required");
        }
        try {
            Map<String, Object> stats = orderService.getRestaurantAnalytics(restaurantId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to retrieve analytics: " + e.getMessage());
        }
    }
}
