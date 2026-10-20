
package com.bhagwat.springcommerce.order.controller;

import com.bhagwat.springcommerce.order.dto.OrderItemResponse;
import com.bhagwat.springcommerce.order.dto.OrderResponse;
import com.bhagwat.springcommerce.order.entity.OrderStatus;
import com.bhagwat.springcommerce.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest {

    private OrderService orderService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OrderController(orderService))
                .build();
    }

    private OrderResponse sampleOrder() {
        OrderItemResponse item = new OrderItemResponse(
                10L,
                "Mechanical Keyboard",
                2,
                new BigDecimal("1500.00"),
                new BigDecimal("3000.00")
        );

        return new OrderResponse(
                1L,
                OrderStatus.PENDING,
                new BigDecimal("3000.00"),
                List.of(item),
                LocalDateTime.of(2026, 10, 20, 10, 30)
        );
    }

    @Test
    void placeOrder_shouldReturnCreatedAndOrderDetails() throws Exception {
        when(orderService.placeOrder()).thenReturn(sampleOrder());

        mockMvc.perform(post("/api/v1/orders"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(3000.00))
                .andExpect(jsonPath("$.items[0].productId").value(10))
                .andExpect(jsonPath("$.items[0].productName")
                        .value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].subtotal").value(3000.00));

        verify(orderService).placeOrder();
    }

    @Test
    void getMyOrders_shouldReturnOkAndOrders() throws Exception {
        when(orderService.getMyOrders()).thenReturn(List.of(sampleOrder()));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(orderService).getMyOrders();
    }

    @Test
    void getMyOrders_whenNoOrders_shouldReturnEmptyArray() throws Exception {
        when(orderService.getMyOrders()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(orderService).getMyOrders();
    }

    @Test
    void getOrder_shouldReturnOrderById() throws Exception {
        when(orderService.getOrder(1L)).thenReturn(sampleOrder());

        mockMvc.perform(get("/api/v1/orders/{orderId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService).getOrder(1L);
    }

    @Test
    void cancelOrder_shouldReturnCancelledOrder() throws Exception {
        OrderResponse cancelledOrder = new OrderResponse(
                1L,
                OrderStatus.CANCELLED,
                new BigDecimal("3000.00"),
                sampleOrder().items(),
                sampleOrder().createdAt()
        );

        when(orderService.cancelOrder(1L)).thenReturn(cancelledOrder);

        mockMvc.perform(patch("/api/v1/orders/{orderId}/cancel", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(orderService).cancelOrder(1L);
    }

    @Test
    void updateOrderStatus_shouldReturnUpdatedStatus() throws Exception {
        OrderResponse shippedOrder = new OrderResponse(
                1L,
                OrderStatus.SHIPPED,
                new BigDecimal("3000.00"),
                sampleOrder().items(),
                sampleOrder().createdAt()
        );

        when(orderService.updateOrderStatus(1L, OrderStatus.SHIPPED))
                .thenReturn(shippedOrder);

        mockMvc.perform(patch("/api/v1/orders/{orderId}/status", 1L)
                        .param("status", "SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SHIPPED"));

        verify(orderService).updateOrderStatus(1L, OrderStatus.SHIPPED);
    }
}
