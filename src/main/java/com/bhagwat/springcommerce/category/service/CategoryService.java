package com.bhagwat.springcommerce.category.service;

import com.bhagwat.springcommerce.category.dto.CategoryRequest;
import com.bhagwat.springcommerce.category.dto.CategoryResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    Page<CategoryResponse> getAllCategories(int page, int size);

    CategoryResponse getCategoryById(Long id);

    CategoryResponse updateCategoryById(Long id, CategoryRequest request);

    void deleteCategoryById(Long id);
}
