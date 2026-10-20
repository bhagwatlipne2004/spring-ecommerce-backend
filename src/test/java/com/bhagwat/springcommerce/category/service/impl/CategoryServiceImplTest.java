
package com.bhagwat.springcommerce.category.service.impl;

import com.bhagwat.springcommerce.category.dto.CategoryRequest;
import com.bhagwat.springcommerce.category.dto.CategoryResponse;
import com.bhagwat.springcommerce.category.entity.Category;
import com.bhagwat.springcommerce.category.mapper.CategoryMapper;
import com.bhagwat.springcommerce.category.repository.CategoryRepository;
import com.bhagwat.springcommerce.common.exception.CategoryAlreadyExistsException;
import com.bhagwat.springcommerce.common.exception.ResourceNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    // ---------- CREATE CATEGORY ----------

    @Test
    void createCategory_shouldCreateCategory_whenNameIsUnique() {
        CategoryRequest request = new CategoryRequest(
                "Electronics", "Electronic products", "electronics.jpg", true);

        Category category = new Category();
        category.setName("Electronics");

        Category savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName("Electronics");

        CategoryResponse response = response(1L, "Electronics");

        when(categoryRepository.findByName("Electronics"))
                .thenReturn(Optional.empty());
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(savedCategory);
        when(categoryMapper.toResponse(savedCategory)).thenReturn(response);

        CategoryResponse result = categoryService.createCategory(request);

        assertEquals(response, result);
        verify(categoryRepository).save(category);
    }

    @Test
    void createCategory_shouldThrowException_whenNameAlreadyExists() {
        CategoryRequest request = new CategoryRequest(
                "Electronics", "Electronic products", "electronics.jpg", true);

        when(categoryRepository.findByName("Electronics"))
                .thenReturn(Optional.of(category(1L, "Electronics")));

        assertThrows(CategoryAlreadyExistsException.class,
                () -> categoryService.createCategory(request));

        verify(categoryRepository, never()).save(any(Category.class));
        verify(categoryMapper, never()).toEntity(any(CategoryRequest.class));
    }

    // ---------- GET ALL CATEGORIES ----------

    @Test
    void getAllCategories_shouldReturnPaginatedCategories() {
        Category category = category(1L, "Electronics");
        CategoryResponse response = response(1L, "Electronics");

        Page<Category> categoryPage = new PageImpl<>(
                List.of(category), PageRequest.of(0, 10), 1);

        when(categoryRepository.findAll(any(Pageable.class)))
                .thenReturn(categoryPage);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        Page<CategoryResponse> result =
                categoryService.getAllCategories(0, 10, "name", "asc");

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Electronics", result.getContent().get(0).name());
    }

    @Test
    void getAllCategories_shouldUseAscendingSort_whenDirectionIsAsc() {
        when(categoryRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        categoryService.getAllCategories(0, 10, "name", "asc");

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(categoryRepository).findAll(captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(Sort.Direction.ASC,
                pageable.getSort().getOrderFor("name").getDirection());
    }

    @Test
    void getAllCategories_shouldUseDescendingSort_whenDirectionIsNotAsc() {
        when(categoryRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        categoryService.getAllCategories(1, 5, "name", "desc");

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(categoryRepository).findAll(captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(1, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC,
                pageable.getSort().getOrderFor("name").getDirection());
    }

    // ---------- GET CATEGORY BY ID ----------

    @Test
    void getCategoryById_shouldReturnCategory_whenIdExists() {
        Category category = category(1L, "Electronics");
        CategoryResponse response = response(1L, "Electronics");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.getCategoryById(1L);

        assertEquals(response, result);
    }

    @Test
    void getCategoryById_shouldThrowException_whenIdDoesNotExist() {
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.getCategoryById(99L));

        verify(categoryMapper, never()).toResponse(any(Category.class));
    }

    // ---------- UPDATE CATEGORY ----------

    @Test
    void updateCategoryById_shouldUpdateCategory_whenRequestIsValid() {
        Category existing = category(1L, "Electronics");

        CategoryRequest request = new CategoryRequest(
                "Gadgets", "Electronic gadgets", "gadgets.jpg", true);

        CategoryResponse response = response(1L, "Gadgets");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(existing));
        when(categoryRepository.findByName("Gadgets"))
                .thenReturn(Optional.empty());
        when(categoryMapper.toResponse(existing)).thenReturn(response);

        CategoryResponse result = categoryService.updateCategoryById(1L, request);

        assertEquals("Gadgets", existing.getName());
        assertEquals("Electronic gadgets", existing.getDescription());
        assertEquals("gadgets.jpg", existing.getImageUrl());
        assertTrue(existing.getActive());

        assertEquals(response, result);
        verify(categoryRepository).save(existing);
    }

    @Test
    void updateCategoryById_shouldThrowException_whenCategoryDoesNotExist() {
        CategoryRequest request = new CategoryRequest(
                "Gadgets", "Electronic gadgets", "gadgets.jpg", true);

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategoryById(99L, request));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategoryById_shouldThrowException_whenAnotherCategoryHasSameName() {
        Category existing = category(1L, "Electronics");
        Category duplicate = category(2L, "Gadgets");

        CategoryRequest request = new CategoryRequest(
                "Gadgets", "Electronic gadgets", "gadgets.jpg", true);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(existing));
        when(categoryRepository.findByName("Gadgets"))
                .thenReturn(Optional.of(duplicate));

        assertThrows(CategoryAlreadyExistsException.class,
                () -> categoryService.updateCategoryById(1L, request));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategoryById_shouldAllowSameName_whenUpdatingSameCategory() {
        Category existing = category(1L, "Electronics");

        CategoryRequest request = new CategoryRequest(
                "Electronics", "Updated description", "updated.jpg", true);

        CategoryResponse response = response(1L, "Electronics");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(existing));
        when(categoryRepository.findByName("Electronics"))
                .thenReturn(Optional.of(existing));
        when(categoryMapper.toResponse(existing)).thenReturn(response);

        CategoryResponse result = categoryService.updateCategoryById(1L, request);

        assertEquals("Updated description", existing.getDescription());
        assertEquals("updated.jpg", existing.getImageUrl());
        assertEquals(response, result);
        verify(categoryRepository).save(existing);
    }

    // ---------- DELETE CATEGORY ----------

    @Test
    void deleteCategoryById_shouldDeleteCategory_whenIdExists() {
        Category existing = category(1L, "Electronics");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        categoryService.deleteCategoryById(1L);

        verify(categoryRepository).delete(existing);
    }

    @Test
    void deleteCategoryById_shouldThrowException_whenIdDoesNotExist() {
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.deleteCategoryById(99L));

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    // ---------- SEARCH CATEGORY ----------

    @Test
    void findByNameContainingIgnoreCase_shouldReturnMatchingCategories() {
        Category first = category(1L, "Electronics");
        Category second = category(2L, "Electronic Accessories");

        when(categoryRepository.findByNameContainingIgnoreCase("electronic"))
                .thenReturn(List.of(first, second));

        when(categoryMapper.toResponse(first))
                .thenReturn(response(1L, "Electronics"));
        when(categoryMapper.toResponse(second))
                .thenReturn(response(2L, "Electronic Accessories"));

        List<CategoryResponse> result =
                categoryService.findByNameContainingIgnoreCase("electronic");

        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0).name());
        assertEquals("Electronic Accessories", result.get(1).name());
    }

    @Test
    void findByNameContainingIgnoreCase_shouldReturnEmptyList_whenNoMatches() {
        when(categoryRepository.findByNameContainingIgnoreCase("unknown"))
                .thenReturn(List.of());

        List<CategoryResponse> result =
                categoryService.findByNameContainingIgnoreCase("unknown");

        assertTrue(result.isEmpty());
        verify(categoryMapper, never()).toResponse(any(Category.class));
    }

    // ---------- TEST DATA HELPERS ----------

    private Category category(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription("Category description");
        category.setImageUrl("category.jpg");
        category.setActive(true);
        return category;
    }

    private CategoryResponse response(Long id, String name) {
        return new CategoryResponse(
                id,
                name,
                "Category description",
                "category.jpg",
                true,
                null,
                null
        );
    }
}
