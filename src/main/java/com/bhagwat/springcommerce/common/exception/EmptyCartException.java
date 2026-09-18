package com.bhagwat.springcommerce.common.exception;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException() {
        super("Cannot place order. Cart is empty.");
    }
}