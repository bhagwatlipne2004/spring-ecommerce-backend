package com.bhagwat.springcommerce.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName,String fieldName,Long fieldValue) {
        super(resourceName + " with " + fieldName + " " + fieldValue + " not found.");
    }
}
