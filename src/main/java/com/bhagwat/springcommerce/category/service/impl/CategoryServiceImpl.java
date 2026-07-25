package com.bhagwat.springcommerce.category.service.impl;

import com.bhagwat.springcommerce.category.dto.CategoryRequest;
import com.bhagwat.springcommerce.category.dto.CategoryResponse;
import com.bhagwat.springcommerce.category.entity.Category;
import com.bhagwat.springcommerce.category.mapper.CategoryMapper;
import com.bhagwat.springcommerce.category.repository.CategoryRepository;
import com.bhagwat.springcommerce.category.service.CategoryService;
import com.bhagwat.springcommerce.common.exception.CategoryAlreadyExistsException;
import org.springframework.stereotype.Service;

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
}
