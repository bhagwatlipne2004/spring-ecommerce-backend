package com.bhagwat.springcommerce.order.service.impl;

import com.bhagwat.springcommerce.cart.entity.Cart;
import com.bhagwat.springcommerce.cart.entity.CartItem;
import com.bhagwat.springcommerce.cart.repository.CartRepository;
import com.bhagwat.springcommerce.common.exception.*;
import com.bhagwat.springcommerce.order.dto.OrderResponse;
import com.bhagwat.springcommerce.order.entity.Order;
import com.bhagwat.springcommerce.order.entity.OrderItem;
import com.bhagwat.springcommerce.order.entity.OrderStatus;
import com.bhagwat.springcommerce.order.mapper.OrderMapper;
import com.bhagwat.springcommerce.order.repository.OrderItemRepository;
import com.bhagwat.springcommerce.order.repository.OrderRepository;
import com.bhagwat.springcommerce.order.service.OrderService;
import com.bhagwat.springcommerce.product.entity.Product;
import com.bhagwat.springcommerce.product.repository.ProductRepository;
import com.bhagwat.springcommerce.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final CartRepository cartRepository;

    private final ProductRepository productRepository;

    private final OrderMapper orderMapper;


    @Override
    public OrderResponse placeOrder() {

        User user = getCurrentUser();

        Cart cart = getCurrentUserCart();

        validateCart(cart);

        validateStock(cart);

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        order = orderRepository.save(order);

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .subtotal(subtotal)
                    .build();

            order.getItems().add(orderItem);

            totalAmount = totalAmount.add(subtotal);

            product.setStockQuantity(
                    product.getStockQuantity() - cartItem.getQuantity());

            product.setStockQuantity(
                    product.getStockQuantity() - cartItem.getQuantity());
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        // Clear the cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {

        User user = getCurrentUser();

        List<Order> orders = orderRepository
                .findByUserOrderByCreatedAtDesc(user);

        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {

        Order order = getOrderForCurrentUser(orderId);

        return orderMapper.toOrderResponse(order);
    }

    @Override
    public OrderResponse cancelOrder(Long orderId) {

        Order order = getOrderForCurrentUser(orderId);

        if (order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED) {

            throw new IllegalStateException(
                    "Order can no longer be cancelled."
            );
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderAlreadyCancelledException(orderId);
        }

        for (OrderItem item : order.getItems()) {

            Product product = item.getProduct();

            product.setStockQuantity(
                    product.getStockQuantity() + item.getQuantity()
            );
        }

        order.setStatus(OrderStatus.CANCELLED);

        Order savedOrder = orderRepository.save(order);

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId,
                                           OrderStatus status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(orderId));

        order.setStatus(status);

        validateStatusTransition(order.getStatus(), status);

        order.setStatus(status);
        return orderMapper.toOrderResponse(order);
    }

    // Helper Methods

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return (User) authentication.getPrincipal();
    }

    private Cart getCurrentUserCart() {

        User user = getCurrentUser();

        return cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new CartNotFoundException(user.getId()));
    }

    private Order getOrderForCurrentUser(Long orderId) {

        User user = getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new OrderNotFoundException(orderId);
        }

        return order;
    }

    private void validateCart(Cart cart) {

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException();
        }
    }

    private void validateStock(Cart cart) {

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            if (cartItem.getQuantity() > product.getStockQuantity()) {
                throw new InsufficientStockException(product.getName());
            }
        }
    }

    private void validateStatusTransition(OrderStatus current,
                                          OrderStatus next) {

        if (current == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cancelled orders cannot change status.");
        }

        if (current == OrderStatus.DELIVERED) {
            throw new IllegalStateException(
                    "Delivered orders cannot change status.");
        }

        switch (current) {

            case PENDING -> {
                if (next != OrderStatus.CONFIRMED
                        && next != OrderStatus.CANCELLED) {
                    throw new IllegalStateException(
                            "Invalid status transition.");
                }
            }

            case CONFIRMED -> {
                if (next != OrderStatus.SHIPPED
                        && next != OrderStatus.CANCELLED) {
                    throw new IllegalStateException(
                            "Invalid status transition.");
                }
            }

            case SHIPPED -> {
                if (next != OrderStatus.DELIVERED) {
                    throw new IllegalStateException(
                            "Invalid status transition.");
                }
            }
        }
    }
}