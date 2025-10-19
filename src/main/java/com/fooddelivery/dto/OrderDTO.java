package com.fooddelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private Long userId;
    private Long restaurantId;
    private List<OrderItemDTO> items;
    private Double totalAmount;
    private Double tax;
    private Double deliveryFee;
    private String status;
    private String deliveryAddress;
    private String deliveryAgent;
    private String paymentMethod;
}