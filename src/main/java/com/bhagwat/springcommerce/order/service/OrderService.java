package com.bhagwat.springcommerce.order.service;

import com.bhagwat.springcommerce.order.dto.OrderResponse;
import com.bhagwat.springcommerce.order.entity.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder();

    List<OrderResponse> getMyOrders();

    OrderResponse getOrder(Long orderId);

    OrderResponse cancelOrder(Long orderId);

    OrderResponse updateOrderStatus(Long orderId,
                                    OrderStatus status);
}