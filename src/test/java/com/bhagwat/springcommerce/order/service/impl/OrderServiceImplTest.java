
package com.bhagwat.springcommerce.order.service.impl;

import com.bhagwat.springcommerce.cart.repository.CartRepository;
import com.bhagwat.springcommerce.common.exception.OrderNotFoundException;
import com.bhagwat.springcommerce.order.dto.OrderResponse;
import com.bhagwat.springcommerce.order.entity.Order;
import com.bhagwat.springcommerce.order.entity.OrderItem;
import com.bhagwat.springcommerce.order.entity.OrderStatus;
import com.bhagwat.springcommerce.order.mapper.OrderMapper;
import com.bhagwat.springcommerce.order.repository.OrderItemRepository;
import com.bhagwat.springcommerce.order.repository.OrderRepository;
import com.bhagwat.springcommerce.product.repository.ProductRepository;
import com.bhagwat.springcommerce.user.entity.User;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bhagwat.springcommerce.cart.entity.Cart;
import com.bhagwat.springcommerce.cart.entity.CartItem;
import com.bhagwat.springcommerce.common.exception.InsufficientStockException;
import com.bhagwat.springcommerce.product.entity.Product;
import java.util.ArrayList;

import com.bhagwat.springcommerce.common.exception.CartNotFoundException;
import com.bhagwat.springcommerce.common.exception.EmptyCartException;
import com.bhagwat.springcommerce.common.exception.OrderAlreadyCancelledException;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User userA;
    private User userB;
    private Order orderB;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        userA = new User();
        userA.setId(1L);

        userB = new User();
        userB.setId(2L);

        orderB = Order.builder()
                .id(200L)
                .user(userB)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("99.99"))
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userA,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getOrderShouldRejectAnotherUsersOrder() {
        when(orderRepository.findById(200L))
                .thenReturn(Optional.of(orderB));

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrder(200L)
        );

        verify(orderMapper, never()).toOrderResponse(any());
    }

    @Test
    void cancelOrderShouldRejectAnotherUsersOrder() {
        when(orderRepository.findById(200L))
                .thenReturn(Optional.of(orderB));

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.cancelOrder(200L)
        );

        assertEquals(OrderStatus.PENDING, orderB.getStatus());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getMyOrdersShouldQueryOnlyAuthenticatedUsersOrders() {
        when(orderRepository.findByUserOrderByCreatedAtDesc(userA))
                .thenReturn(List.of());

        List<OrderResponse> result = orderService.getMyOrders();

        assertNotNull(result);

        verify(orderRepository)
                .findByUserOrderByCreatedAtDesc(userA);
        verify(orderRepository, never()).findById(anyLong());
    }

    @Test
    void updateOrderStatusShouldRejectMissingOrder() {
        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.updateOrderStatus(
                        999L, OrderStatus.CONFIRMED
                )
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatusShouldAllowPendingToConfirmed() {
        when(orderRepository.findById(200L))
                .thenReturn(Optional.of(orderB));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.updateOrderStatus(200L, OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, orderB.getStatus());
        verify(orderRepository).save(orderB);
    }

    @Test
    void updateOrderStatusShouldRejectPendingToShipped() {
        when(orderRepository.findById(200L))
                .thenReturn(Optional.of(orderB));

        assertThrows(
                IllegalStateException.class,
                () -> orderService.updateOrderStatus(200L, OrderStatus.SHIPPED)
        );

        assertEquals(OrderStatus.PENDING, orderB.getStatus());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatusShouldRejectTransitionFromCancelled() {
        orderB.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(200L))
                .thenReturn(Optional.of(orderB));

        assertThrows(
                IllegalStateException.class,
                () -> orderService.updateOrderStatus(200L, OrderStatus.CONFIRMED)
        );

        assertEquals(OrderStatus.CANCELLED, orderB.getStatus());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatusShouldRejectTransitionFromDelivered() {
        orderB.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(200L))
                .thenReturn(Optional.of(orderB));

        assertThrows(
                IllegalStateException.class,
                () -> orderService.updateOrderStatus(200L, OrderStatus.CONFIRMED)
        );

        assertEquals(OrderStatus.DELIVERED, orderB.getStatus());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrderShouldDeductStockExactlyOnce() {
        Product product = Product.builder()
                .id(10L)
                .name("Keyboard")
                .price(new BigDecimal("500.00"))
                .stockQuantity(10)
                .build();

        Cart cart = Cart.builder()
                .id(1L)
                .user(userA)
                .build();

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(2)
                .build();

        cart.getItems().add(cartItem);

        when(cartRepository.findByUser(userA))
                .thenReturn(Optional.of(cart));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.placeOrder();

        assertEquals(8, product.getStockQuantity());
        assertEquals(0, cart.getItems().size());

        verify(orderRepository, times(2)).save(any(Order.class));
        verify(cartRepository).save(cart);
    }

    @Test
    void placeOrderShouldRejectInsufficientStock() {
        Product product = Product.builder()
                .id(10L)
                .name("Keyboard")
                .price(new BigDecimal("500.00"))
                .stockQuantity(1)
                .build();

        Cart cart = Cart.builder()
                .id(1L)
                .user(userA)
                .build();

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(2)
                .build();

        cart.getItems().add(cartItem);

        when(cartRepository.findByUser(userA))
                .thenReturn(Optional.of(cart));

        assertThrows(
                InsufficientStockException.class,
                () -> orderService.placeOrder()
        );

        assertEquals(1, product.getStockQuantity());
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void cancelOrderShouldRestoreProductStock() {
        Product product = Product.builder()
                .id(10L)
                .name("Keyboard")
                .price(new BigDecimal("500.00"))
                .stockQuantity(8)
                .build();

        Order order = Order.builder()
                .id(200L)
                .user(userA)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("1000.00"))
                .build();

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(2)
                .price(new BigDecimal("500.00"))
                .subtotal(new BigDecimal("1000.00"))
                .build();

        order.getItems().add(orderItem);

        when(orderRepository.findById(200L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.cancelOrder(200L);

        assertEquals(10, product.getStockQuantity());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        verify(orderRepository).save(order);
    }


    @Test
    void getOrderShouldThrowWhenOrderDoesNotExist() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrder(999L)
        );

        verify(orderMapper, never()).toOrderResponse(any());
    }

    @Test
    void getOrderShouldReturnOrderForAuthenticatedUser() {
        Order ownOrder = Order.builder()
                .id(100L)
                .user(userA)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .build();

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(ownOrder));

        orderService.getOrder(100L);

        verify(orderMapper).toOrderResponse(ownOrder);
    }

    @Test
    void placeOrderShouldThrowWhenCartDoesNotExist() {
        when(cartRepository.findByUser(userA))
                .thenReturn(Optional.empty());

        assertThrows(
                CartNotFoundException.class,
                () -> orderService.placeOrder()
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrderShouldRejectEmptyCart() {
        Cart cart = Cart.builder()
                .id(1L)
                .user(userA)
                .build();

        when(cartRepository.findByUser(userA))
                .thenReturn(Optional.of(cart));

        assertThrows(
                EmptyCartException.class,
                () -> orderService.placeOrder()
        );

        verify(orderRepository, never()).save(any(Order.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void cancelOrderShouldRejectShippedOrder() {
        Order order = Order.builder()
                .id(300L)
                .user(userA)
                .status(OrderStatus.SHIPPED)
                .totalAmount(new BigDecimal("50.00"))
                .build();

        when(orderRepository.findById(300L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalStateException.class,
                () -> orderService.cancelOrder(300L)
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrderShouldRejectDeliveredOrder() {
        Order order = Order.builder()
                .id(301L)
                .user(userA)
                .status(OrderStatus.DELIVERED)
                .totalAmount(new BigDecimal("50.00"))
                .build();

        when(orderRepository.findById(301L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalStateException.class,
                () -> orderService.cancelOrder(301L)
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrderShouldRejectAlreadyCancelledOrder() {
        Order order = Order.builder()
                .id(302L)
                .user(userA)
                .status(OrderStatus.CANCELLED)
                .totalAmount(new BigDecimal("50.00"))
                .build();

        when(orderRepository.findById(302L))
                .thenReturn(Optional.of(order));

        assertThrows(
                OrderAlreadyCancelledException.class,
                () -> orderService.cancelOrder(302L)
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatusShouldAllowConfirmedToShipped() {
        orderB.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(200L))
                .thenReturn(Optional.of(orderB));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.updateOrderStatus(200L, OrderStatus.SHIPPED);

        assertEquals(OrderStatus.SHIPPED, orderB.getStatus());
        verify(orderRepository).save(orderB);
        verify(orderMapper).toOrderResponse(orderB);
    }

    @Test
    void updateOrderStatusShouldAllowConfirmedToCancelled() {
        orderB.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(200L))
                .thenReturn(Optional.of(orderB));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.updateOrderStatus(200L, OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, orderB.getStatus());
        verify(orderRepository).save(orderB);
    }

    @Test
    void updateOrderStatusShouldAllowShippedToDelivered() {
        orderB.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(200L))
                .thenReturn(Optional.of(orderB));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.updateOrderStatus(200L, OrderStatus.DELIVERED);

        assertEquals(OrderStatus.DELIVERED, orderB.getStatus());
        verify(orderRepository).save(orderB);
    }

    @Test
    void updateOrderStatusShouldRejectConfirmedToDelivered() {
        orderB.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(200L))
                .thenReturn(Optional.of(orderB));

        assertThrows(
                IllegalStateException.class,
                () -> orderService.updateOrderStatus(200L, OrderStatus.DELIVERED)
        );

        assertEquals(OrderStatus.CONFIRMED, orderB.getStatus());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatusShouldRejectShippedToConfirmed() {
        orderB.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(200L))
                .thenReturn(Optional.of(orderB));

        assertThrows(
                IllegalStateException.class,
                () -> orderService.updateOrderStatus(200L, OrderStatus.CONFIRMED)
        );

        assertEquals(OrderStatus.SHIPPED, orderB.getStatus());
        verify(orderRepository, never()).save(any(Order.class));
    }

}
