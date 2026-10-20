
package com.bhagwat.springcommerce.product.mapper;

import com.bhagwat.springcommerce.category.dto.CategorySummary;
import com.bhagwat.springcommerce.category.entity.Category;
import com.bhagwat.springcommerce.product.dto.ProductRequest;
import com.bhagwat.springcommerce.product.dto.ProductResponse;
import com.bhagwat.springcommerce.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {

    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        productMapper = new ProductMapper();
    }

    @Test
    void toEntityShouldMapAllFieldsAndDefaultActiveToTrue() {
        Category category = new Category();
        category.setId(10L);
        category.setName("Electronics");

        ProductRequest request = new ProductRequest(
                "Laptop",
                "Gaming laptop",
                new BigDecimal("79999.99"),
                15,
                "https://example.com/laptop.jpg",
                null,
                10L
        );

        Product product = productMapper.toEntity(request, category);

        assertEquals("Laptop", product.getName());
        assertEquals("Gaming laptop", product.getDescription());
        assertEquals(new BigDecimal("79999.99"), product.getPrice());
        assertEquals(15, product.getStockQuantity());
        assertEquals("https://example.com/laptop.jpg", product.getImageUrl());
        assertTrue(product.getActive());
        assertSame(category, product.getCategory());
    }

    @Test
    void toEntityShouldPreserveExplicitActiveValue() {
        Category category = new Category();
        category.setId(10L);
        category.setName("Electronics");

        ProductRequest request = new ProductRequest(
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("2499.00"),
                20,
                "https://example.com/keyboard.jpg",
                false,
                10L
        );

        Product product = productMapper.toEntity(request, category);

        assertEquals("Keyboard", product.getName());
        assertFalse(product.getActive());
        assertSame(category, product.getCategory());
    }

    @Test
    void toResponseShouldMapProductAndCategorySummary() {
        Category category = new Category();
        category.setId(10L);
        category.setName("Electronics");

        Product product = new Product();
        product.setId(100L);
        product.setName("Laptop");
        product.setDescription("Gaming laptop");
        product.setPrice(new BigDecimal("79999.99"));
        product.setStockQuantity(15);
        product.setImageUrl("https://example.com/laptop.jpg");
        product.setActive(true);
        product.setCategory(category);

        ProductResponse response = productMapper.toResponse(product);

        assertEquals(100L, response.id());
        assertEquals("Laptop", response.name());
        assertEquals("Gaming laptop", response.description());
        assertEquals(new BigDecimal("79999.99"), response.price());
        assertEquals(15, response.stockQuantity());
        assertEquals("https://example.com/laptop.jpg", response.imageUrl());
        assertTrue(response.active());

        assertNotNull(response.category());
        assertEquals(10L, response.category().id());
        assertEquals("Electronics", response.category().name());
    }

    @Test
    void toResponseShouldPreserveInactiveProduct() {
        Category category = new Category();
        category.setId(20L);
        category.setName("Accessories");

        Product product = new Product();
        product.setId(200L);
        product.setName("Mouse");
        product.setDescription("Wireless mouse");
        product.setPrice(new BigDecimal("999.00"));
        product.setStockQuantity(0);
        product.setImageUrl(null);
        product.setActive(false);
        product.setCategory(category);

        ProductResponse response = productMapper.toResponse(product);

        assertEquals(200L, response.id());
        assertFalse(response.active());
        assertEquals(0, response.stockQuantity());
        assertNull(response.imageUrl());
        assertEquals(
                new CategorySummary(20L, "Accessories"),
                response.category()
        );
    }
}
