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

        return savedOrder;
    }

    public Order getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

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
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            for (OrderItem item : items) {
                item.setMenuItem(menuItemRepository.findById(item.getMenuItemId()).orElse(null));
            }
            order.setItems(items);
        }

        return orders;
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public Order assignDeliveryAgent(Long orderId, String agentName) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setDeliveryAgent(agentName);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getStatus().equals("pending")) {
            throw new RuntimeException("Can only cancel pending orders");
        }
        order.setStatus("cancelled");
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public double calculateTotalWithTax(double subtotal) {
        double tax = subtotal * 0.05;
        double deliveryFee = 50.0;
        return subtotal + tax + deliveryFee;
    }
}