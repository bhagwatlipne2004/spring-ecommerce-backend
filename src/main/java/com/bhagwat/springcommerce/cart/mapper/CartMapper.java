package com.bhagwat.springcommerce.cart.mapper;

import com.bhagwat.springcommerce.cart.dto.CartItemResponse;
import com.bhagwat.springcommerce.cart.dto.CartResponse;
import com.bhagwat.springcommerce.cart.entity.Cart;
import com.bhagwat.springcommerce.cart.entity.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CartMapper {

    public CartItemResponse toCartItemResponse(CartItem item) {

        BigDecimal subtotal = item.getProduct()
                .getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                subtotal
        );
    }

    public CartResponse toCartResponse(Cart cart) {

        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(this::toCartItemResponse)
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                items,
                total
        );
    }
}