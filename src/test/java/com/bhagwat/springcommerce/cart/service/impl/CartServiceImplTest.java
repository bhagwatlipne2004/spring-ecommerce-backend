
package com.bhagwat.springcommerce.cart.service.impl;

import com.bhagwat.springcommerce.cart.entity.Cart;
import com.bhagwat.springcommerce.cart.entity.CartItem;
import com.bhagwat.springcommerce.cart.mapper.CartMapper;
import com.bhagwat.springcommerce.cart.repository.CartItemRepository;
import com.bhagwat.springcommerce.cart.repository.CartRepository;
import com.bhagwat.springcommerce.cart.dto.UpdateCartItemRequest;
import com.bhagwat.springcommerce.common.exception.CartItemNotFoundException;
import com.bhagwat.springcommerce.common.exception.InsufficientStockException;
import com.bhagwat.springcommerce.product.repository.ProductRepository;
import com.bhagwat.springcommerce.user.entity.User;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.bhagwat.springcommerce.cart.dto.AddToCartRequest;
import com.bhagwat.springcommerce.cart.dto.CartResponse;
import com.bhagwat.springcommerce.common.exception.CartNotFoundException;
import com.bhagwat.springcommerce.common.exception.ResourceNotFoundException;
import com.bhagwat.springcommerce.product.entity.Product;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private User userA;
    private Cart cartA;
    private Cart cartB;
    private CartItem itemB;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        userA = new User();
        userA.setId(1L);

        User userB = new User();
        userB.setId(2L);

        cartA = Cart.builder()
                .id(10L)
                .user(userA)
                .build();

        cartB = Cart.builder()
                .id(20L)
                .user(userB)
                .build();

        itemB = CartItem.builder()
                .id(100L)
                .cart(cartB)
                .quantity(2)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userA,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateQuantityShouldRejectAnotherUsersCartItem() {
        when(cartRepository.findByUser(userA))
                .thenReturn(Optional.of(cartA));

        when(cartItemRepository.findById(100L))
                .thenReturn(Optional.of(itemB));

        assertThrows(
                CartItemNotFoundException.class,
                () -> cartService.updateQuantity(
                        100L,
                        new UpdateCartItemRequest(5)
                )
        );

        assertEquals(2, itemB.getQuantity());

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void removeItemShouldRejectAnotherUsersCartItem() {
        when(cartRepository.findByUser(userA))
                .thenReturn(Optional.of(cartA));

        when(cartItemRepository.findById(100L))
                .thenReturn(Optional.of(itemB));

        assertThrows(
                CartItemNotFoundException.class,
                () -> cartService.removeItem(100L)
        );

        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }


    @Test
    void getCartShouldReturnCartResponseWhenCartExists() {
        CartResponse expected = new CartResponse(
                10L,
                List.of(),
                BigDecimal.ZERO
        );

        when(cartRepository.findByUser(userA))
                .thenReturn(Optional.of(cartA));
        when(cartMapper.toCartResponse(cartA))
                .thenReturn(expected);

        CartResponse actual = cartService.getCart();

        assertEquals(expected, actual);
        verify(cartMapper).toCartResponse(cartA);
    }

    @Test
    void getCartShouldThrowWhenCartDoesNotExist() {
        when(cartRepository.findByUser(userA))
                .thenReturn(Optional.empty());

        assertThrows(
                CartNotFoundException.class,
                () -> cartService.getCart()
        );

        verify(cartMapper, never()).toCartResponse(any(Cart.class));
    }

    @Test
    void addToCartShouldThrowWhenProductDoesNotExist() {
        AddToCartRequest request = new AddToCartRequest(999L, 1);

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addToCart(request)
        );

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void clearCartShouldClearItemsAndSaveCart() {
        CartItem existingItem = CartItem.builder()
                .id(101L)
                .cart(cartA)
                .quantity(2)
                .build();

        cartA.getItems().add(existingItem);

        when(cartRepository.findByUser(userA))
                .thenReturn(Optional.of(cartA));
        when(cartRepository.save(cartA))
                .thenReturn(cartA);

        cartService.clearCart();

        assertEquals(0, cartA.getItems().size());
        verify(cartRepository).save(cartA);
    }

    @Test
    void addToCartShouldAddNewProductToCart() {
        Product product = Product.builder()
                .id(50L)
                .name("Keyboard")
                .price(new BigDecimal("1500.00"))
                .stockQuantity(10)
                .build();

        AddToCartRequest request = new AddToCartRequest(50L, 2);
        CartResponse expected = new CartResponse(10L, List.of(), BigDecimal.ZERO);

        when(productRepository.findById(50L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUser(userA)).thenReturn(Optional.of(cartA));
        when(cartRepository.save(cartA)).thenReturn(cartA);
        when(cartMapper.toCartResponse(cartA)).thenReturn(expected);

        CartResponse actual = cartService.addToCart(request);

        assertEquals(expected, actual);
        assertEquals(1, cartA.getItems().size());
        assertEquals(2, cartA.getItems().get(0).getQuantity());
        assertEquals(product, cartA.getItems().get(0).getProduct());

        verify(cartRepository).save(cartA);
        verify(cartMapper).toCartResponse(cartA);
    }

    @Test
    void addToCartShouldIncreaseQuantityWhenProductAlreadyExists() {
        Product product = Product.builder()
                .id(50L)
                .name("Keyboard")
                .price(new BigDecimal("1500.00"))
                .stockQuantity(10)
                .build();

        CartItem existingItem = CartItem.builder()
                .id(101L)
                .cart(cartA)
                .product(product)
                .quantity(2)
                .build();

        cartA.getItems().add(existingItem);

        AddToCartRequest request = new AddToCartRequest(50L, 3);
        CartResponse expected = new CartResponse(10L, List.of(), BigDecimal.ZERO);

        when(productRepository.findById(50L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUser(userA)).thenReturn(Optional.of(cartA));
        when(cartRepository.save(cartA)).thenReturn(cartA);
        when(cartMapper.toCartResponse(cartA)).thenReturn(expected);

        CartResponse actual = cartService.addToCart(request);

        assertEquals(expected, actual);
        assertEquals(1, cartA.getItems().size());
        assertEquals(5, existingItem.getQuantity());

        verify(cartRepository).save(cartA);
    }

    @Test
    void addToCartShouldThrowWhenQuantityExceedsStock() {
        Product product = Product.builder()
                .id(50L)
                .name("Keyboard")
                .price(new BigDecimal("1500.00"))
                .stockQuantity(5)
                .build();

        AddToCartRequest request = new AddToCartRequest(50L, 6);

        when(productRepository.findById(50L))
                .thenReturn(Optional.of(product));
        when(cartRepository.findByUser(userA))
                .thenReturn(Optional.of(cartA));

        assertThrows(
                InsufficientStockException.class,
                () -> cartService.addToCart(request)
        );

        assertEquals(0, cartA.getItems().size());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void addToCartShouldThrowWhenExistingQuantityPlusNewQuantityExceedsStock() {
        Product product = Product.builder()
                .id(50L)
                .name("Keyboard")
                .price(new BigDecimal("1500.00"))
                .stockQuantity(5)
                .build();

        CartItem existingItem = CartItem.builder()
                .id(101L)
                .cart(cartA)
                .product(product)
                .quantity(3)
                .build();

        cartA.getItems().add(existingItem);

        AddToCartRequest request = new AddToCartRequest(50L, 3);

        when(productRepository.findById(50L))
                .thenReturn(Optional.of(product));
        when(cartRepository.findByUser(userA))
                .thenReturn(Optional.of(cartA));

        assertThrows(
                InsufficientStockException.class,
                () -> cartService.addToCart(request)
        );

        assertEquals(3, existingItem.getQuantity());
        assertEquals(1, cartA.getItems().size());

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void addToCartShouldRejectQuantityIncreaseBeyondStock() {
        Product product = Product.builder()
                .id(50L)
                .name("Keyboard")
                .price(new BigDecimal("1500.00"))
                .stockQuantity(5)
                .build();

        CartItem existingItem = CartItem.builder()
                .id(101L)
                .cart(cartA)
                .product(product)
                .quantity(3)
                .build();

        cartA.getItems().add(existingItem);

        when(productRepository.findById(50L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUser(userA)).thenReturn(Optional.of(cartA));

        assertThrows(
                InsufficientStockException.class,
                () -> cartService.addToCart(new AddToCartRequest(50L, 3))
        );

        assertEquals(3, existingItem.getQuantity());
        assertEquals(1, cartA.getItems().size());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void addToCartShouldCreateCartWhenUserHasNoCart() {
        Product product = Product.builder()
                .id(50L)
                .name("Keyboard")
                .price(new BigDecimal("1500.00"))
                .stockQuantity(10)
                .build();

        Cart newCart = Cart.builder()
                .id(30L)
                .user(userA)
                .build();

        when(productRepository.findById(50L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUser(userA))
                .thenReturn(Optional.empty(), Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);
        when(cartMapper.toCartResponse(newCart))
                .thenReturn(new CartResponse(30L, List.of(), BigDecimal.ZERO));

        CartResponse actual = cartService.addToCart(new AddToCartRequest(50L, 2));

        assertEquals(30L, actual.id());
        assertEquals(1, newCart.getItems().size());
        assertEquals(2, newCart.getItems().get(0).getQuantity());

        verify(cartRepository, times(2)).save(any(Cart.class));
    }

    @Test
    void updateQuantityShouldUpdateItemWhenRequestIsValid() {
        Product product = Product.builder()
                .id(50L)
                .name("Keyboard")
                .stockQuantity(10)
                .build();

        CartItem item = CartItem.builder()
                .id(101L)
                .cart(cartA)
                .product(product)
                .quantity(2)
                .build();

        when(cartRepository.findByUser(userA)).thenReturn(Optional.of(cartA));
        when(cartItemRepository.findById(101L)).thenReturn(Optional.of(item));
        when(cartMapper.toCartResponse(cartA))
                .thenReturn(new CartResponse(10L, List.of(), BigDecimal.ZERO));

        cartService.updateQuantity(101L, new UpdateCartItemRequest(5));

        assertEquals(5, item.getQuantity());
        verify(cartItemRepository).save(item);
    }

    @Test
    void updateQuantityShouldRejectQuantityBeyondStock() {
        Product product = Product.builder()
                .id(50L)
                .name("Keyboard")
                .stockQuantity(5)
                .build();

        CartItem item = CartItem.builder()
                .id(101L)
                .cart(cartA)
                .product(product)
                .quantity(2)
                .build();

        when(cartRepository.findByUser(userA)).thenReturn(Optional.of(cartA));
        when(cartItemRepository.findById(101L)).thenReturn(Optional.of(item));

        assertThrows(
                InsufficientStockException.class,
                () -> cartService.updateQuantity(101L, new UpdateCartItemRequest(6))
        );

        assertEquals(2, item.getQuantity());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void removeItemShouldDeleteUsersOwnCartItem() {
        CartItem item = CartItem.builder()
                .id(101L)
                .cart(cartA)
                .quantity(2)
                .build();

        when(cartRepository.findByUser(userA)).thenReturn(Optional.of(cartA));
        when(cartItemRepository.findById(101L)).thenReturn(Optional.of(item));

        cartService.removeItem(101L);

        verify(cartItemRepository).delete(item);
    }

    @Test
    void updateQuantityShouldThrowWhenCartDoesNotExist() {
        when(cartRepository.findByUser(userA)).thenReturn(Optional.empty());

        assertThrows(
                CartNotFoundException.class,
                () -> cartService.updateQuantity(101L, new UpdateCartItemRequest(5))
        );

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void removeItemShouldThrowWhenItemDoesNotExist() {
        when(cartRepository.findByUser(userA)).thenReturn(Optional.of(cartA));
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                CartItemNotFoundException.class,
                () -> cartService.removeItem(999L)
        );

        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }
}
