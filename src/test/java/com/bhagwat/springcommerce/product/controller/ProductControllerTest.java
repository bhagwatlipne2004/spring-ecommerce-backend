
package com.bhagwat.springcommerce.product.controller;

import com.bhagwat.springcommerce.category.dto.CategorySummary;
import com.bhagwat.springcommerce.common.dto.PageResponse;
import com.bhagwat.springcommerce.product.dto.ProductRequest;
import com.bhagwat.springcommerce.product.dto.ProductResponse;
import com.bhagwat.springcommerce.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest {

    private ProductService productService;
    private MockMvc mockMvc;

    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductController(productService))
                .build();

        productResponse = new ProductResponse(
                1L,
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("499.99"),
                10,
                "https://example.com/keyboard.png",
                true,
                new CategorySummary(2L, "Electronics")
        );
    }

    @Test
    void createProductShouldReturnCreated() throws Exception {
        when(productService.createProduct(any(ProductRequest.class)))
                .thenReturn(productResponse);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Keyboard",
                                  "description": "Mechanical keyboard",
                                  "price": 499.99,
                                  "stockQuantity": 10,
                                  "imageUrl": "https://example.com/keyboard.png",
                                  "active": true,
                                  "categoryId": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.price").value(499.99))
                .andExpect(jsonPath("$.category.name").value("Electronics"));

        verify(productService).createProduct(any(ProductRequest.class));
    }

    @Test
    void getAllProductsShouldUseDefaultParameters() throws Exception {
        PageResponse<ProductResponse> pageResponse = mock(PageResponse.class);

        when(productService.getAllProducts(
                0, 10, "id", "asc", null, null, null
        )).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk());

        verify(productService).getAllProducts(
                0, 10, "id", "asc", null, null, null
        );
    }

    @Test
    void getAllProductsShouldPassCustomFiltersAndPagination() throws Exception {
        PageResponse<ProductResponse> pageResponse = mock(PageResponse.class);

        when(productService.getAllProducts(
                2, 5, "price", "desc", 3L, "keyboard", true
        )).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/products")
                        .param("page", "2")
                        .param("size", "5")
                        .param("sortBy", "price")
                        .param("direction", "desc")
                        .param("categoryId", "3")
                        .param("keyword", "keyboard")
                        .param("active", "true"))
                .andExpect(status().isOk());

        verify(productService).getAllProducts(
                2, 5, "price", "desc", 3L, "keyboard", true
        );
    }

    @Test
    void getProductByIdShouldReturnProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Keyboard"));

        verify(productService).getProductById(1L);
    }

    @Test
    void updateProductShouldReturnUpdatedProduct() throws Exception {
        when(productService.updateProductById(
                any(ProductRequest.class), eq(1L)
        )).thenReturn(productResponse);

        mockMvc.perform(put("/api/v1/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Keyboard",
                                  "description": "Mechanical keyboard",
                                  "price": 499.99,
                                  "stockQuantity": 10,
                                  "imageUrl": "https://example.com/keyboard.png",
                                  "active": true,
                                  "categoryId": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Keyboard"));

        verify(productService).updateProductById(
                any(ProductRequest.class), eq(1L)
        );
    }

    @Test
    void deleteProductShouldReturnNoContent() throws Exception {
        doNothing().when(productService).deleteById(1L);

        mockMvc.perform(delete("/api/v1/products/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(productService).deleteById(1L);
    }
}
