package com.bhagwat.springcommerce.common.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String productName) {
        super("Insufficient stock for product : " + productName);
    }
}