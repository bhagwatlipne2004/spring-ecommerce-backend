package com.bhagwat.springcommerce.common.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String email) {
        super("User with " + email  + " already exists");
    }
}
