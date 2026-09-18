package com.bhagwat.springcommerce.common.exception;

public class OrderAlreadyDeliveredException extends RuntimeException {

    public OrderAlreadyDeliveredException(Long orderId) {
        super("Order " + orderId + " has already been delivered.");
    }
}