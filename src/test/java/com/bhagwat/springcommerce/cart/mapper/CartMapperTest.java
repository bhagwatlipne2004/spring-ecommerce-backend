
package com.bhagwat.springcommerce.cart.mapper;

import com.bhagwat.springcommerce.cart.dto.CartItemResponse;
import com.bhagwat.springcommerce.cart.dto.CartResponse;
import com.bhagwat.springcommerce.cart.entity.Cart;
import com.bhagwat.springcommerce.cart.entity.CartItem;
import com.bhagwat.springcommerce.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CartMapperTest {

    private CartMapper cartMapper;

    @BeforeEach
    void setUp() {
        cartMapper = new CartMapper();
    }

    @Test
    void toCartItemResponseShouldMapFieldsAndCalculateSubtotal() {
        Product product = new Product();
        product.setId(10L);
        product.setName("Keyboard");
        product.setPrice(new BigDecimal("2500.00"));

        CartItem item = new CartItem();
        item.setId(1L);
        item.setProduct(product);
        item.setQuantity(3);

        CartItemResponse response = cartMapper.toCartItemResponse(item);

        assertEquals(1L, response.id());
        assertEquals(10L, response.productId());
        assertEquals("Keyboard", response.productName());
        assertEquals(new BigDecimal("2500.00"), response.price());
        assertEquals(3, response.quantity());
        assertEquals(new BigDecimal("7500.00"), response.subtotal());
    }

    @Test
    void toCartResponseShouldMapItemsAndCalculateTotal() {
        Product keyboard = new Product();
        keyboard.setId(10L);
        keyboard.setName("Keyboard");
        keyboard.setPrice(new BigDecimal("2500.00"));

        CartItem firstItem = new CartItem();
        firstItem.setId(1L);
        firstItem.setProduct(keyboard);
        firstItem.setQuantity(2);

        Product mouse = new Product();
        mouse.setId(20L);
        mouse.setName("Mouse");
        mouse.setPrice(new BigDecimal("800.00"));

        CartItem secondItem = new CartItem();
        secondItem.setId(2L);
        secondItem.setProduct(mouse);
        secondItem.setQuantity(3);

        Cart cart = new Cart();
        cart.setId(100L);
        cart.setItems(List.of(firstItem, secondItem));

        CartResponse response = cartMapper.toCartResponse(cart);

        assertEquals(100L, response.id());
        assertEquals(2, response.items().size());

        assertEquals("Keyboard", response.items().get(0).productName());
        assertEquals(new BigDecimal("5000.00"),
                response.items().get(0).subtotal());

        assertEquals("Mouse", response.items().get(1).productName());
        assertEquals(new BigDecimal("2400.00"),
                response.items().get(1).subtotal());

        assertEquals(new BigDecimal("7400.00"), response.totalAmount());
    }

    @Test
    void toCartResponseShouldReturnZeroTotalForEmptyCart() {
        Cart cart = new Cart();
        cart.setId(100L);
        cart.setItems(List.of());

        CartResponse response = cartMapper.toCartResponse(cart);

        assertEquals(100L, response.id());
        assertNotNull(response.items());
        assertTrue(response.items().isEmpty());
        assertEquals(BigDecimal.ZERO, response.totalAmount());
    }
}
