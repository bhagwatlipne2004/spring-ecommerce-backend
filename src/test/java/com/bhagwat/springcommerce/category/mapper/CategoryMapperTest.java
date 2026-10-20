
package com.bhagwat.springcommerce.category.mapper;

import com.bhagwat.springcommerce.category.dto.CategoryRequest;
import com.bhagwat.springcommerce.category.dto.CategoryResponse;
import com.bhagwat.springcommerce.category.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CategoryMapperTest {

    private CategoryMapper categoryMapper;

    @BeforeEach
    void setUp() {
        categoryMapper = new CategoryMapper();
    }

    @Test
    void toEntityShouldMapAllFieldsAndDefaultActiveToTrueWhenNull() {
        CategoryRequest request = new CategoryRequest(
                "Electronics",
                "Electronic products",
                "https://example.com/electronics.png",
                null
        );

        Category result = categoryMapper.toEntity(request);

        assertEquals("Electronics", result.getName());
        assertEquals("Electronic products", result.getDescription());
        assertEquals(
                "https://example.com/electronics.png",
                result.getImageUrl()
        );
        assertTrue(result.getActive());
        assertNull(result.getId());
    }

    @Test
    void toEntityShouldPreserveActiveTrue() {
        CategoryRequest request = new CategoryRequest(
                "Books",
                "Books and novels",
                "https://example.com/books.png",
                true
        );

        Category result = categoryMapper.toEntity(request);

        assertTrue(result.getActive());
    }

    @Test
    void toEntityShouldPreserveActiveFalse() {
        CategoryRequest request = new CategoryRequest(
                "Archived",
                "Inactive category",
                null,
                false
        );

        Category result = categoryMapper.toEntity(request);

        assertEquals("Archived", result.getName());
        assertNull(result.getImageUrl());
        assertFalse(result.getActive());
    }

    @Test
    void toResponseShouldMapAllFields() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setDescription("Electronic products");
        category.setImageUrl("https://example.com/electronics.png");
        category.setActive(true);

        CategoryResponse response = categoryMapper.toResponse(category);

        assertEquals(1L, response.id());
        assertEquals("Electronics", response.name());
        assertEquals("Electronic products", response.description());
        assertEquals(
                "https://example.com/electronics.png",
                response.imageUrl()
        );
        assertTrue(response.active());
        assertEquals(category.getCreatedAt(), response.createdAt());
        assertEquals(category.getUpdatedAt(), response.updatedAt());
    }

    @Test
    void toResponseShouldPreserveInactiveCategoryAndNullFields() {
        Category category = new Category();
        category.setId(2L);
        category.setName("Old Products");
        category.setActive(false);

        CategoryResponse response = categoryMapper.toResponse(category);

        assertEquals(2L, response.id());
        assertEquals("Old Products", response.name());
        assertNull(response.description());
        assertNull(response.imageUrl());
        assertFalse(response.active());
        assertNull(response.createdAt());
        assertNull(response.updatedAt());
    }
}
