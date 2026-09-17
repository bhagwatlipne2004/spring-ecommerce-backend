package com.bhagwat.springcommerce.common.exception;

public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException(Long cartItemId) {
        super("Cart item not found with id : " + cartItemId);
    }
}