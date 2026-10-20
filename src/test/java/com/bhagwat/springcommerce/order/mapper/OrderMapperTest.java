
package com.bhagwat.springcommerce.order.mapper;

import com.bhagwat.springcommerce.order.dto.OrderItemResponse;
import com.bhagwat.springcommerce.order.dto.OrderResponse;
import com.bhagwat.springcommerce.order.entity.Order;
import com.bhagwat.springcommerce.order.entity.OrderItem;
import com.bhagwat.springcommerce.order.entity.OrderStatus;
import com.bhagwat.springcommerce.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {

    private OrderMapper orderMapper;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapper();
    }

    @Test
    void toOrderItemResponse_shouldMapAllFields() {
        Product product = Product.builder()
                .id(10L)
                .name("Keyboard")
                .build();

        OrderItem item = OrderItem.builder()
                .product(product)
                .quantity(2)
                .price(new BigDecimal("1500.00"))
                .subtotal(new BigDecimal("3000.00"))
                .build();

        OrderItemResponse response = orderMapper.toOrderItemResponse(item);

        assertEquals(10L, response.productId());
        assertEquals("Keyboard", response.productName());
        assertEquals(2, response.quantity());
        assertEquals(new BigDecimal("1500.00"), response.price());
        assertEquals(new BigDecimal("3000.00"), response.subtotal());
    }

    @Test
    void toOrderResponse_shouldMapOrderAndAllItems() {
        Product keyboard = Product.builder()
                .id(10L)
                .name("Keyboard")
                .build();

        Product mouse = Product.builder()
                .id(20L)
                .name("Mouse")
                .build();

        OrderItem keyboardItem = OrderItem.builder()
                .product(keyboard)
                .quantity(2)
                .price(new BigDecimal("1500.00"))
                .subtotal(new BigDecimal("3000.00"))
                .build();

        OrderItem mouseItem = OrderItem.builder()
                .product(mouse)
                .quantity(1)
                .price(new BigDecimal("500.00"))
                .subtotal(new BigDecimal("500.00"))
                .build();

        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 20, 10, 30);

        Order order = Order.builder()
                .id(100L)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("3500.00"))
                .items(List.of(keyboardItem, mouseItem))
                .build();

        OrderResponse response = orderMapper.toOrderResponse(order);

        assertEquals(100L, response.id());
        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals(new BigDecimal("3500.00"), response.totalAmount());

        assertEquals(2, response.items().size());

        assertEquals(10L, response.items().get(0).productId());
        assertEquals("Keyboard", response.items().get(0).productName());

        assertEquals(20L, response.items().get(1).productId());
        assertEquals("Mouse", response.items().get(1).productName());
    }

    @Test
    void toOrderResponse_shouldHandleEmptyItems() {
        Order order = Order.builder()
                .id(101L)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .items(List.of())
                .build();

        OrderResponse response = orderMapper.toOrderResponse(order);

        assertEquals(101L, response.id());
        assertEquals(BigDecimal.ZERO, response.totalAmount());
        assertNotNull(response.items());
        assertTrue(response.items().isEmpty());
    }
}
