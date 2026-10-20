
package com.bhagwat.springcommerce.category.controller;

import com.bhagwat.springcommerce.category.dto.CategoryRequest;
import com.bhagwat.springcommerce.category.dto.CategoryResponse;
import com.bhagwat.springcommerce.category.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private LocalValidatorFactoryBean validator;

    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(categoryController)
                .setValidator(validator)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        categoryResponse = new CategoryResponse(
                1L,
                "Electronics",
                "Electronic products",
                "https://example.com/electronics.jpg",
                true,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 2, 10, 0)
        );
    }

    @Test
    void createCategoryShouldReturnCreated() throws Exception {
        CategoryRequest request = new CategoryRequest(
                "Electronics", "Electronic products",
                "https://example.com/electronics.jpg", true
        );

        when(categoryService.createCategory(any(CategoryRequest.class)))
                .thenReturn(categoryResponse);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.description").value("Electronic products"))
                .andExpect(jsonPath("$.active").value(true));

        verify(categoryService).createCategory(any(CategoryRequest.class));
    }

    @Test
    void createCategoryShouldRejectBlankName() throws Exception {
        CategoryRequest request = new CategoryRequest(
                " ", "Electronic products", null, true
        );

        mockMvc.perform(post("/api/v1/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }

    @Test
    void createCategoryShouldRejectNameLongerThan100Characters()
            throws Exception {
        CategoryRequest request = new CategoryRequest(
                "A".repeat(101), "Description", null, true
        );

        mockMvc.perform(post("/api/v1/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }

    @Test
    void getAllCategoriesShouldUseDefaultPaginationParameters()
            throws Exception {
        Page<CategoryResponse> page = new PageImpl<>(
                List.of(categoryResponse),
                PageRequest.of(0, 10),
                1
        );

        when(categoryService.getAllCategories(0, 10, "id", "asc"))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Electronics"));

        verify(categoryService).getAllCategories(0, 10, "id", "asc");
    }

    @Test
    void getAllCategoriesShouldAcceptCustomPaginationParameters()
            throws Exception {
        Page<CategoryResponse> page = new PageImpl<>(
                List.of(categoryResponse),
                PageRequest.of(1, 5),
                6
        );

        when(categoryService.getAllCategories(1, 5, "name", "desc"))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/categories")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "name")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Electronics"));

        verify(categoryService).getAllCategories(1, 5, "name", "desc");
    }

    @Test
    void getCategoryByIdShouldReturnCategory() throws Exception {
        when(categoryService.getCategoryById(1L))
                .thenReturn(categoryResponse);

        mockMvc.perform(get("/api/v1/categories/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));

        verify(categoryService).getCategoryById(1L);
    }

    @Test
    void updateCategoryShouldReturnUpdatedCategory() throws Exception {
        CategoryRequest request = new CategoryRequest(
                "Updated Electronics", "Updated description", null, true
        );

        CategoryResponse updatedResponse = new CategoryResponse(
                1L, "Updated Electronics", "Updated description",
                null, true,
                categoryResponse.createdAt(), categoryResponse.updatedAt()
        );

        when(categoryService.updateCategoryById(
                eq(1L), any(CategoryRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/categories/{id}", 1)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Electronics"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        verify(categoryService).updateCategoryById(
                eq(1L), any(CategoryRequest.class));
    }

    @Test
    void updateCategoryShouldRejectBlankName() throws Exception {
        CategoryRequest request = new CategoryRequest(
                "", "Description", null, true
        );

        mockMvc.perform(put("/api/v1/categories/{id}", 1)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }

    @Test
    void deleteCategoryShouldReturnNoContent() throws Exception {
        doNothing().when(categoryService).deleteCategoryById(1L);

        mockMvc.perform(delete("/api/v1/categories/{id}", 1))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(categoryService).deleteCategoryById(1L);
    }

    @Test
    void searchByKeywordShouldReturnMatchingCategories() throws Exception {
        when(categoryService.findByNameContainingIgnoreCase("elect"))
                .thenReturn(List.of(categoryResponse));

        mockMvc.perform(get("/api/v1/categories/search")
                        .param("keyword", "elect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Electronics"));

        verify(categoryService).findByNameContainingIgnoreCase("elect");
    }

    @Test
    void searchByKeywordShouldReturnEmptyListWhenNoMatches()
            throws Exception {
        when(categoryService.findByNameContainingIgnoreCase("unknown"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/categories/search")
                        .param("keyword", "unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(categoryService).findByNameContainingIgnoreCase("unknown");
    }
}
