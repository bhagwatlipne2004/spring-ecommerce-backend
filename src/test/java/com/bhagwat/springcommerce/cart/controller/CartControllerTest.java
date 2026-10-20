
package com.bhagwat.springcommerce.cart.controller;

import com.bhagwat.springcommerce.cart.dto.AddToCartRequest;
import com.bhagwat.springcommerce.cart.dto.CartItemResponse;
import com.bhagwat.springcommerce.cart.dto.CartResponse;
import com.bhagwat.springcommerce.cart.dto.UpdateCartItemRequest;
import com.bhagwat.springcommerce.cart.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CartControllerTest {

    private CartService cartService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        cartService = mock(CartService.class);
        objectMapper = new ObjectMapper();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new CartController(cartService))
                .setValidator(validator)
                .build();
    }

    private CartResponse sampleCart() {
        CartItemResponse item = new CartItemResponse(
                100L,
                10L,
                "Mechanical Keyboard",
                new BigDecimal("1500.00"),
                2,
                new BigDecimal("3000.00")
        );

        return new CartResponse(
                1L,
                List.of(item),
                new BigDecimal("3000.00")
        );
    }

    @Test
    void addToCart_shouldReturnCreatedAndCartDetails() throws Exception {
        when(cartService.addToCart(any(AddToCartRequest.class)))
                .thenReturn(sampleCart());

        AddToCartRequest request = new AddToCartRequest(10L, 2);

        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.items[0].productId").value(10))
                .andExpect(jsonPath("$.items[0].productName")
                        .value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.totalAmount").value(3000.00));

        verify(cartService).addToCart(any(AddToCartRequest.class));
    }

    @Test
    void addToCart_whenProductIdMissing_shouldReturnBadRequest()
            throws Exception {
        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity": 2}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cartService);
    }

    @Test
    void addToCart_whenQuantityIsZero_shouldReturnBadRequest()
            throws Exception {
        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId": 10, "quantity": 0}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cartService);
    }

    @Test
    void getCart_shouldReturnCartDetails() throws Exception {
        when(cartService.getCart()).thenReturn(sampleCart());

        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.totalAmount").value(3000.00));

        verify(cartService).getCart();
    }

    @Test
    void updateQuantity_shouldReturnUpdatedCart() throws Exception {
        when(cartService.updateQuantity(
                eq(100L), any(UpdateCartItemRequest.class)))
                .thenReturn(sampleCart());

        UpdateCartItemRequest request = new UpdateCartItemRequest(2);

        mockMvc.perform(put("/api/v1/cart/items/{cartItemId}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        verify(cartService).updateQuantity(
                eq(100L), any(UpdateCartItemRequest.class));
    }

    @Test
    void updateQuantity_whenQuantityIsZero_shouldReturnBadRequest()
            throws Exception {
        mockMvc.perform(put("/api/v1/cart/items/{cartItemId}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity": 0}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cartService);
    }

    @Test
    void removeItem_shouldReturnNoContent() throws Exception {
        doNothing().when(cartService).removeItem(100L);

        mockMvc.perform(delete("/api/v1/cart/items/{cartItemId}", 100L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(cartService).removeItem(100L);
    }

    @Test
    void clearCart_shouldReturnNoContent() throws Exception {
        doNothing().when(cartService).clearCart();

        mockMvc.perform(delete("/api/v1/cart/clear"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(cartService).clearCart();
    }
}
