package com.bhagwat.springcommerce.common.exception;

public class ProductAlreadyExistsException extends RuntimeException{

    public ProductAlreadyExistsException(String productName) {
        super("Product " + productName + " already exists.");
    }
}