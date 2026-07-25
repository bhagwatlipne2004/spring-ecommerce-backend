package com.bhagwat.springcommerce.common.exception;

public class CategoryAlreadyExistsException extends RuntimeException{

    public CategoryAlreadyExistsException(String categoryName) {
        super("Category " + categoryName + " already exists.");
    }
}
