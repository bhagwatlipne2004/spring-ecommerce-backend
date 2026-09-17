package com.bhagwat.springcommerce.cart.service;

import com.bhagwat.springcommerce.cart.dto.AddToCartRequest;
import com.bhagwat.springcommerce.cart.dto.CartResponse;
import com.bhagwat.springcommerce.cart.dto.UpdateCartItemRequest;

public interface CartService {

    CartResponse addToCart(AddToCartRequest request);

    CartResponse getCart();

    CartResponse updateQuantity(Long cartItemId,
                                UpdateCartItemRequest request);

    void removeItem(Long cartItemId);

    void clearCart();
}