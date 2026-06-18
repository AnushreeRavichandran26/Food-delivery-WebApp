package com.fooddelivery.service;

import com.fooddelivery.model.Order;
import com.fooddelivery.model.OrderItem;
import com.fooddelivery.model.DeliveryAgent;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.OrderItemRepository;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.DeliveryAgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Service
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private DeliveryAgentRepository deliveryAgentRepository;  // ✅ ADD THIS

    @Autowired
    private OrderStreamService orderStreamService;

    @Autowired
    private EmailService emailService;

    public Order createOrder(Order order) {
        System.out.println("Creating order for user: " + order.getUserId());

        // Validate user exists
        if (!userRepository.existsById(order.getUserId())) {
            throw new RuntimeException("User not found with ID: " + order.getUserId());
        }

        // Validate restaurant exists
        if (!restaurantRepository.existsById(order.getRestaurantId())) {
            throw new RuntimeException("Restaurant not found with ID: " + order.getRestaurantId());
        }

        // Validate order items belong to the same restaurant
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                if (item.getMenuItemId() != null) {
                    com.fooddelivery.model.MenuItem menuItem = menuItemRepository.findById(item.getMenuItemId())
                            .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + item.getMenuItemId()));
                    if (!menuItem.getRestaurantId().equals(order.getRestaurantId())) {
                        throw new RuntimeException("Order contains items from multiple restaurants or mismatched restaurant ID");
                    }
                }
            }
        }

        // Set order defaults
        if (order.getStatus() == null || order.getStatus().isEmpty()) {
            order.setStatus("pending");
        }
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setEstimatedDelivery(LocalTime.now().plusMinutes(45));
        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(45));

        // Assign random delivery agent
        try {
            List<DeliveryAgent> agents = deliveryAgentRepository.findAll();
            if (!agents.isEmpty()) {
                DeliveryAgent agent = agents.get((int) (Math.random() * agents.size()));
                order.setDeliveryAgent(agent.getName());
            }
        } catch (Exception e) {
            System.err.println("Failed to assign delivery agent: " + e.getMessage());
            // Continue without agent
        }

        // Save order first
        Order savedOrder = orderRepository.save(order);
        System.out.println("Order saved with ID: " + savedOrder.getId());

        // Save order items
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            System.out.println("Saving " + order.getItems().size() + " order items");
            for (OrderItem item : order.getItems()) {
                item.setOrderId(savedOrder.getId());
                item.setCreatedAt(LocalDateTime.now());
                if (item.getQuantity() == null || item.getQuantity() == 0) {
                    item.setQuantity(1);
                }
                OrderItem savedItem = orderItemRepository.save(item);
                System.out.println("Saved order item: " + savedItem.getId());
            }
        }

        // Notify user and admin about the new order
        try {
            Order fullOrder = getOrderById(savedOrder.getId());
            orderStreamService.sendNewOrderNotification(fullOrder);
            orderStreamService.sendOrderUpdate(fullOrder);
            sendOrderStatusEmail(fullOrder, fullOrder.getStatus());
        } catch (Exception e) {
            System.err.println("Failed to stream new order: " + e.getMessage());
        }

        populateRestaurantName(savedOrder);
        return savedOrder;
    }

    private void populateRestaurantName(Order order) {
        if (order != null && order.getRestaurantId() != null) {
            com.fooddelivery.model.Restaurant restaurant = restaurantRepository.findById(order.getRestaurantId()).orElse(null);
            if (restaurant != null) {
                order.setRestaurantName(restaurant.getName());
            }
        }
    }

    public Order getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        populateRestaurantName(order);

        // Load order items
        List<OrderItem> items = orderItemRepository.findByOrderId(id);

        // Load menu item details for each order item
        for (OrderItem item : items) {
            item.setMenuItem(menuItemRepository.findById(item.getMenuItemId()).orElse(null));
        }

        order.setItems(items);
        return order;
    }

    public List<Order> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // Load items for each order
        for (Order order : orders) {
            populateRestaurantName(order);
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            for (OrderItem item : items) {
                item.setMenuItem(menuItemRepository.findById(item.getMenuItemId()).orElse(null));
            }
            order.setItems(items);
        }

        return orders;
    }

    public List<Order> getOrdersByStatus(String status) {
        List<Order> orders = orderRepository.findByStatus(status);
        for (Order order : orders) {
            populateRestaurantName(order);
        }
        return orders;
    }

    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        Order updated = orderRepository.save(order);
        
        try {
            Order fullOrder = getOrderById(orderId);
            orderStreamService.sendOrderUpdate(fullOrder);
            sendOrderStatusEmail(fullOrder, status);
        } catch (Exception e) {
            System.err.println("Failed to stream status update: " + e.getMessage());
        }
        
        return updated;
    }

    public Order assignDeliveryAgent(Long orderId, String agentName) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setDeliveryAgent(agentName);
        order.setUpdatedAt(LocalDateTime.now());
        Order updated = orderRepository.save(order);
        
        try {
            Order fullOrder = getOrderById(orderId);
            orderStreamService.sendOrderUpdate(fullOrder);
        } catch (Exception e) {
            System.err.println("Failed to stream agent assignment: " + e.getMessage());
        }
        
        return updated;
    }

    public Order cancelOrder(Long orderId) {
        return cancelOrder(orderId, null);
    }

    public Order cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        String status = order.getStatus();
        if (status == null) {
            status = "pending";
        }
        status = status.toLowerCase();
        
        if (status.equals("out") || status.equals("delivered") || status.equals("completed") || status.equals("cancelled")) {
            throw new RuntimeException("Order is in a non-cancellable stage: " + order.getStatus());
        }
        
        order.setStatus("cancelled");
        order.setUpdatedAt(LocalDateTime.now());
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason != null && !reason.trim().isEmpty() ? reason.trim() : "Not specified");
        
        Order updated = orderRepository.save(order);
        
        try {
            Order fullOrder = getOrderById(orderId);
            orderStreamService.sendOrderUpdate(fullOrder);
            sendOrderStatusEmail(fullOrder, "cancelled");
        } catch (Exception e) {
            System.err.println("Failed to stream cancellation: " + e.getMessage());
        }
        
        return updated;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public double calculateTotalWithTax(double subtotal) {
        double tax = subtotal * 0.05;
        double deliveryFee = 50.0;
        return subtotal + tax + deliveryFee;
    }

    public Map<String, Object> getRestaurantAnalytics(Long restaurantId) {
        List<Order> orders = orderRepository.findByRestaurantId(restaurantId);
        int totalOrders = 0;

        double totalRevenue = 0.0;
        for (Order order : orders) {
            if (order.getStatus() != null && !order.getStatus().equalsIgnoreCase("cancelled")) {
                totalOrders++;
                totalRevenue += (order.getTotalAmount() != null ? order.getTotalAmount() : 0.0);
            }
        }

        List<OrderItem> items = orderItemRepository.findByRestaurantId(restaurantId);

        Map<String, Integer> dishQuantities = new HashMap<>();
        int totalDishesOrdered = 0;
        for (OrderItem item : items) {
            String name = item.getName();
            if (name == null || name.isEmpty() || "NULL".equalsIgnoreCase(name)) {
                if (item.getMenuItemId() != null) {
                    com.fooddelivery.model.MenuItem menuItem = menuItemRepository.findById(item.getMenuItemId()).orElse(null);
                    if (menuItem != null) {
                        name = menuItem.getName();
                    }
                }
            }
            if (name == null || name.isEmpty() || "NULL".equalsIgnoreCase(name)) {
                name = "Unknown Item";
            }
            int qty = item.getQuantity() != null ? item.getQuantity() : 1;
            dishQuantities.put(name, dishQuantities.getOrDefault(name, 0) + qty);
            totalDishesOrdered += qty;
        }

        List<Map<String, Object>> dishSummary = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : dishQuantities.entrySet()) {
            Map<String, Object> summary = new HashMap<>();
            summary.put("dishName", entry.getKey());
            summary.put("orderCount", entry.getValue());
            double percentage = totalDishesOrdered > 0 ? (entry.getValue() * 100.0 / totalDishesOrdered) : 0.0;
            summary.put("percentage", Math.round(percentage * 10.0) / 10.0); // 1 decimal place
            dishSummary.add(summary);
        }

        // Sort by orderCount descending
        dishSummary.sort((a, b) -> Integer.compare((Integer) b.get("orderCount"), (Integer) a.get("orderCount")));

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalOrders", totalOrders);
        analytics.put("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
        analytics.put("totalDishesOrdered", totalDishesOrdered);
        analytics.put("dishSummary", dishSummary);

        return analytics;
    }

    private void sendOrderStatusEmail(Order order, String status) {
        try {
            if (order == null || order.getUserId() == null) {
                return;
            }
            com.fooddelivery.model.User user = userRepository.findById(order.getUserId()).orElse(null);
            if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return;
            }

            String statusDesc = status;
            if (status != null) {
                String s = status.toLowerCase().trim();
                if (s.equals("pending")) {
                    statusDesc = "Placed";
                } else if (s.equals("confirmed")) {
                    statusDesc = "Confirmed";
                } else if (s.equals("preparing")) {
                    statusDesc = "Being Prepared";
                } else if (s.equals("out")) {
                    statusDesc = "Out for Delivery";
                } else if (s.equals("delivered")) {
                    statusDesc = "Delivered";
                } else if (s.equals("cancelled")) {
                    statusDesc = "Cancelled";
                } else {
                    statusDesc = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
                }
            }

            // Ensure restaurant name is populated
            if (order.getRestaurantName() == null || order.getRestaurantName().isEmpty()) {
                populateRestaurantName(order);
            }
            String restaurant = order.getRestaurantName() != null ? order.getRestaurantName() : "Selected Restaurant";

            String subject = "Order Status Update – Order #" + order.getId();
            
            // Format estimated delivery time nicely
            String estDelivery = "N/A";
            if (order.getEstimatedDeliveryTime() != null) {
                estDelivery = order.getEstimatedDeliveryTime().toString();
            } else if (order.getEstimatedDelivery() != null) {
                estDelivery = order.getEstimatedDelivery().toString();
            }

            String content = "Hello " + user.getName() + ",\n\n"
                    + "Your order #" + order.getId() + " from " + restaurant 
                    + " status is updated to " + statusDesc + ".\n\n"
                    + "Estimated Delivery: " + estDelivery + "\n\n"
                    + "Thank you,\nFood Delivery Team";

            emailService.sendEmail(user.getEmail(), subject, content);
        } catch (Exception e) {
            System.err.println("Failed to send order status notification email: " + e.getMessage());
        }
    }
}