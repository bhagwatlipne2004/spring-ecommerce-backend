
package com.bhagwat.springcommerce.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    private static final String PATH = "/api/test";

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(PATH);
    }

    private void assertErrorResponse(
            ResponseEntity<ApiErrorResponse> response,
            HttpStatus expectedStatus,
            String expectedMessage) {

        assertEquals(expectedStatus, response.getStatusCode());
        assertNotNull(response.getBody());

        ApiErrorResponse body = response.getBody();

        assertNotNull(body.timestamp());
        assertEquals(expectedStatus.value(), body.status());
        assertEquals(expectedStatus.getReasonPhrase(), body.error());
        assertEquals(expectedMessage, body.message());
        assertEquals(PATH, body.path());
    }

    @Test
    void handleCategoryAlreadyExistsException() {
        var exception = new CategoryAlreadyExistsException("Electronics");

        var response =
                handler.handleCategoryAlreadyExistsException(exception, request);

        assertErrorResponse(response, HttpStatus.CONFLICT,
                "Category Electronics already exists.");
    }

    @Test
    void handleResourceNotFoundException() {
        var exception =
                new ResourceNotFoundException("Product", "id", 10L);

        var response =
                handler.handleResourceNotFoundException(exception, request);

        assertErrorResponse(response, HttpStatus.NOT_FOUND,
                "Product with id 10 not found.");
    }

    @Test
    void handleProductAlreadyExistsException() {
        var exception = new ProductAlreadyExistsException("Laptop");

        var response =
                handler.handleProductAlreadyExistsException(exception, request);

        assertErrorResponse(response, HttpStatus.CONFLICT,
                "Product Laptop already exists.");
    }

    @Test
    void handleUserAlreadyExistsException() {
        var exception = new UserAlreadyExistsException("test@example.com");

        var response =
                handler.handleUserAlreadyExistsException(exception, request);

        assertErrorResponse(response, HttpStatus.CONFLICT,
                "User with test@example.com already exists");
    }

    @Test
    void handleInvalidCredentialsException() {
        var exception = new InvalidCredentialsException();

        var response =
                handler.handleInvalidCredentialsException(exception, request);

        assertErrorResponse(response, HttpStatus.CONFLICT,
                "Invalid Credentials");
    }

    @Test
    void handleCartNotFoundException() {
        var exception = new CartNotFoundException(5L);

        var response =
                handler.handleCartNotFoundException(exception, request);

        assertErrorResponse(response, HttpStatus.NOT_FOUND,
                "Cart not found for user id : 5");
    }

    @Test
    void handleCartItemNotFoundException() {
        var exception = new CartItemNotFoundException(12L);

        var response =
                handler.handleCartItemNotFoundException(exception, request);

        assertErrorResponse(response, HttpStatus.NOT_FOUND,
                "Cart item not found with id : 12");
    }

    @Test
    void handleInsufficientStockException() {
        var exception = new InsufficientStockException("Laptop");

        var response =
                handler.handleInsufficientStockException(exception, request);

        assertErrorResponse(response, HttpStatus.BAD_REQUEST,
                "Insufficient stock for product : Laptop");
    }

    @Test
    void handleReviewNotFoundException() {
        var exception = new ReviewNotFoundException(20L);

        var response =
                handler.handleReviewNotFoundException(exception, request);

        assertErrorResponse(response, HttpStatus.NOT_FOUND,
                "Review not found with id : 20");
    }

    @Test
    void handleDuplicateReviewException() {
        var exception = new DuplicateReviewException(15L);

        var response =
                handler.handleDuplicateReviewException(exception, request);

        assertErrorResponse(response, HttpStatus.CONFLICT,
                "You have already reviewed product with id : 15");
    }

    @Test
    void handleProductNotPurchasedException() {
        var exception = new ProductNotPurchasedException(30L);

        var response =
                handler.handleProductNotPurchasedException(exception, request);

        assertErrorResponse(response, HttpStatus.FORBIDDEN,
                "You can only review products you have purchased. Product id : 30");
    }
}
