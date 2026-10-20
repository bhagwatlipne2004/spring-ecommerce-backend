
package com.bhagwat.springcommerce.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.bhagwat.springcommerce.product.service.ProductService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.bhagwat.springcommerce.product.dto.ProductResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bhagwat.springcommerce.category.dto.CategorySummary;
import com.bhagwat.springcommerce.product.dto.ProductResponse;
import com.bhagwat.springcommerce.product.service.ProductService;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void unauthenticatedUserCannotAccessCart() throws Exception {
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedUserCannotCreateOrder() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedUserCannotCreateProduct() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserCannotCreateProduct() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .with(user("customer").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }


    @Test
    void adminCanCreateProduct() throws Exception {
        ProductResponse response = new ProductResponse(
                1L,
                "Test Product",
                "Security integration test",
                new BigDecimal("99.99"),
                10,
                "https://example.com/test.jpg",
                true,
                new CategorySummary(1L, "Electronics")
        );

        when(productService.createProduct(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Test Product",
                          "description": "Security integration test",
                          "price": 99.99,
                          "stockQuantity": 10,
                          "imageUrl": "https://example.com/test.jpg",
                          "active": true,
                          "categoryId": 1
                        }
                        """))
                .andExpect(status().isCreated());
    }
}
