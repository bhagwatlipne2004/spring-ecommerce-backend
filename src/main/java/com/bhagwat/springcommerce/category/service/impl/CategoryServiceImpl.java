package com.bhagwat.springcommerce.category.service.impl;

import com.bhagwat.springcommerce.category.dto.CategoryRequest;
import com.bhagwat.springcommerce.category.dto.CategoryResponse;
import com.bhagwat.springcommerce.category.entity.Category;
import com.bhagwat.springcommerce.category.mapper.CategoryMapper;
import com.bhagwat.springcommerce.category.repository.CategoryRepository;
import com.bhagwat.springcommerce.category.service.CategoryService;
import com.bhagwat.springcommerce.common.exception.CategoryAlreadyExistsException;
import com.bhagwat.springcommerce.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        if (categoryRepository.findByName(categoryRequest.name()).isPresent()) {
            throw new CategoryAlreadyExistsException(categoryRequest.name());
        }
        Category category = categoryMapper.toEntity(categoryRequest);
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    public Page<CategoryResponse> getAllCategories(int page, int size) {

        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Category> categories = categoryRepository.findAll(pageRequest);

        return categories.map(categoryMapper::toResponse);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse updateCategoryById(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        //  Check if another category already has this name
        Optional<Category> existingCategory = categoryRepository.findByName(request.name());

        if(existingCategory.isPresent()
                && !existingCategory.get().getId().equals(category.getId())) {
            throw new CategoryAlreadyExistsException(request.name());
        }

        //  Update fields
        category.setName(request.name());
        category.setDescription(request.description());
        category.setImageUrl(request.imageUrl());
        category.setActive(request.active());
        categoryRepository.save(category);

        return categoryMapper.toResponse(category);
    }

    @Override
    public void deleteCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Category","id",id));
        categoryRepository.delete(category);
    }
}
