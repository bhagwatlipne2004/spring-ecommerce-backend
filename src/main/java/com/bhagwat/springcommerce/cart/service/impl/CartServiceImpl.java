package com.bhagwat.springcommerce.cart.service.impl;

import com.bhagwat.springcommerce.cart.dto.AddToCartRequest;
import com.bhagwat.springcommerce.cart.dto.CartResponse;
import com.bhagwat.springcommerce.cart.dto.UpdateCartItemRequest;
import com.bhagwat.springcommerce.cart.entity.Cart;
import com.bhagwat.springcommerce.cart.entity.CartItem;
import com.bhagwat.springcommerce.cart.mapper.CartMapper;
import com.bhagwat.springcommerce.cart.repository.CartItemRepository;
import com.bhagwat.springcommerce.cart.repository.CartRepository;
import com.bhagwat.springcommerce.cart.service.CartService;
import com.bhagwat.springcommerce.common.exception.CartItemNotFoundException;
import com.bhagwat.springcommerce.common.exception.CartNotFoundException;
import com.bhagwat.springcommerce.common.exception.InsufficientStockException;
import com.bhagwat.springcommerce.common.exception.ResourceNotFoundException;
import com.bhagwat.springcommerce.product.entity.Product;
import com.bhagwat.springcommerce.product.repository.ProductRepository;
import com.bhagwat.springcommerce.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final ProductRepository productRepository;

    private final CartMapper cartMapper;

    private Cart getCurrentUserCart() {

        User user = getCurrentUser();

        return cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new CartNotFoundException(user.getId()));
    }

    private void validateStock(Product product,
                               Integer quantity) {

        if (quantity > product.getStockQuantity()) {
            throw new InsufficientStockException(product.getName());
        }
    }

    @Override
    public CartResponse addToCart(AddToCartRequest request) {
        User user = getCurrentUser();

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product",
                        "id",
                        request.productId()));

        Cart cart = getOrCreateCart(user);

        Optional<CartItem> existingItem = cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {

            CartItem cartItem = existingItem.get();

            int newQuantity = cartItem.getQuantity() + request.quantity();

            if (newQuantity > product.getStockQuantity()) {
                throw new InsufficientStockException(product.getName());
            }

            cartItem.setQuantity(newQuantity);

        } else {

            if (request.quantity() > product.getStockQuantity()) {
                throw new InsufficientStockException(product.getName());
            }

            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.quantity())
                    .build();

            cart.getItems().add(cartItem);
        }

        Cart savedCart = cartRepository.save(cart);

        return cartMapper.toCartResponse(savedCart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart() {

        User user = getCurrentUser();

        Cart cart = getCurrentUserCart();

        return cartMapper.toCartResponse(cart);
    }

    @Override
    public CartResponse updateQuantity(Long cartItemId,
                                       UpdateCartItemRequest request) {

        User user = getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new CartNotFoundException(user.getId()));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new CartItemNotFoundException(cartItemId));

        // Security check
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new CartItemNotFoundException(cartItemId);
        }

        if (request.quantity() > cartItem.getProduct().getStockQuantity()) {
            throw new InsufficientStockException(
                    cartItem.getProduct().getName());
        }

        cartItem.setQuantity(request.quantity());

        cartItemRepository.save(cartItem);

        return cartMapper.toCartResponse(cart);
    }

    @Override
    public void removeItem(Long cartItemId) {

        User user = getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new CartNotFoundException(user.getId()));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new CartItemNotFoundException(cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new CartItemNotFoundException(cartItemId);
        }

        cartItemRepository.delete(cartItem);
    }

    @Override
    public void clearCart() {

        User user = getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new CartNotFoundException(user.getId()));

        cart.getItems().clear();

        cartRepository.save(cart);
    }

    // Helper Methods

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return (User) authentication.getPrincipal();
    }

    private Cart getOrCreateCart(User user) {

        return cartRepository.findByUser(user)
                .orElseGet(() -> {

                    Cart cart = Cart.builder()
                            .user(user)
                            .build();

                    return cartRepository.save(cart);
                });
    }
}