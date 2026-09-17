package com.bhagwat.springcommerce.common.exception;

public class CartNotFoundException extends RuntimeException {

    public CartNotFoundException(Long userId) {
        super("Cart not found for user id : " + userId);
    }
}