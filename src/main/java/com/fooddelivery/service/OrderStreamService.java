package com.fooddelivery.service;

import com.fooddelivery.model.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class OrderStreamService {

    // Manage multiple connections per user ID (e.g. multiple tabs)
    private final Map<Long, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();
    
    // Manage connections for administrative/simulator views
    private final List<SseEmitter> adminEmitters = new CopyOnWriteArrayList<>();

    /**
     * Registers a customer's SSE connection for real-time updates of their orders.
     */
    public SseEmitter registerUserEmitter(Long userId) {
        // Create an emitter with a 5 minute (300,000 ms) timeout
        SseEmitter emitter = new SseEmitter(300_000L);
        
        List<SseEmitter> emitters = userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
        emitters.add(emitter);

        emitter.onCompletion(() -> removeUserEmitter(userId, emitter));
        emitter.onTimeout(() -> removeUserEmitter(userId, emitter));
        emitter.onError((ex) -> removeUserEmitter(userId, emitter));

        // Send an initial connection event
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected successfully. Listening for order updates."));
        } catch (IOException e) {
            removeUserEmitter(userId, emitter);
        }

        return emitter;
    }

    /**
     * Registers an administrative/simulator SSE connection for notifications on all orders.
     */
    public SseEmitter registerAdminEmitter() {
        SseEmitter emitter = new SseEmitter(300_000L);
        adminEmitters.add(emitter);

        emitter.onCompletion(() -> removeAdminEmitter(emitter));
        emitter.onTimeout(() -> removeAdminEmitter(emitter));
        emitter.onError((ex) -> removeAdminEmitter(emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected successfully. Listening for all order events."));
        } catch (IOException e) {
            removeAdminEmitter(emitter);
        }

        return emitter;
    }

    /**
     * Push order update details to the specific user.
     */
    public void sendOrderUpdate(Order order) {
        if (order == null || order.getUserId() == null) return;
        
        List<SseEmitter> emitters = userEmitters.get(order.getUserId());
        if (emitters == null || emitters.isEmpty()) return;

        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("ORDER_UPDATED")
                        .data(order));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        
        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
        }
    }

    /**
     * Push notifications of a new order to the admin simulator.
     */
    public void sendNewOrderNotification(Order order) {
        if (order == null) return;

        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : adminEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("NEW_ORDER")
                        .data(order));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }

        if (!deadEmitters.isEmpty()) {
            adminEmitters.removeAll(deadEmitters);
        }
    }

    private void removeUserEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }

    private void removeAdminEmitter(SseEmitter emitter) {
        adminEmitters.remove(emitter);
    }
}
