package com.fooddelivery.service;

import com.fooddelivery.model.Payment;
import com.fooddelivery.model.Order;
import com.fooddelivery.repository.PaymentRepository;
import com.fooddelivery.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

@Service
@Transactional
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    public Payment processPayment(Payment payment) {
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        payment.setTransactionId(generateTransactionId());

        // Simulate payment processing
        if (validatePayment(payment)) {
            payment.setPaymentStatus("success");
            orderService.updateOrderStatus(order.getId(), "confirmed");
        } else {
            payment.setPaymentStatus("failed");
        }

        payment.setCreatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    private boolean validatePayment(Payment payment) {
        String method = payment.getPaymentMethod();
        if (method == null) return false;

        // Accept: cod, card, wallet, upi
        return method.equalsIgnoreCase("cod") ||
                method.equalsIgnoreCase("card") ||
                method.equalsIgnoreCase("wallet") ||
                method.equalsIgnoreCase("upi");
    }

    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }

    public Optional<Payment> getLatestPayment(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}