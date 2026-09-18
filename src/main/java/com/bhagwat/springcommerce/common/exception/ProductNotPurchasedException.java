package com.bhagwat.springcommerce.common.exception;

public class ProductNotPurchasedException extends RuntimeException {

    public ProductNotPurchasedException(Long productId) {
        super("You can only review products you have purchased. Product id : " + productId);
    }
}