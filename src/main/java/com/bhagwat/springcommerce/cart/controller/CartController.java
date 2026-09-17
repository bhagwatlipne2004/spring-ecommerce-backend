package com.bhagwat.springcommerce.cart.controller;

import com.bhagwat.springcommerce.cart.dto.AddToCartRequest;
import com.bhagwat.springcommerce.cart.dto.CartResponse;
import com.bhagwat.springcommerce.cart.dto.UpdateCartItemRequest;
import com.bhagwat.springcommerce.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request) {

        CartResponse response = cartService.addToCart(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {

        CartResponse response = cartService.getCart();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateQuantity(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        return ResponseEntity.ok(
                cartService.updateQuantity(cartItemId, request)
        );
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long cartItemId) {

        cartService.removeItem(cartItemId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {

        cartService.clearCart();

        return ResponseEntity.noContent().build();
    }
}