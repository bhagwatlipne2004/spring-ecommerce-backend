package com.bhagwat.springcommerce.order.dto;

import com.bhagwat.springcommerce.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(

        Long id,

        OrderStatus status,

        BigDecimal totalAmount,

        List<OrderItemResponse> items,

        LocalDateTime createdAt

) {
}