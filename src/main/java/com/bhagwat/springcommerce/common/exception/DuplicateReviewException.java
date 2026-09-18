package com.bhagwat.springcommerce.common.exception;

public class DuplicateReviewException extends RuntimeException {

    public DuplicateReviewException(Long productId) {
        super("You have already reviewed product with id : " + productId);
    }
}