package com.bhagwat.springcommerce.common.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid Credentials");
    }
}
