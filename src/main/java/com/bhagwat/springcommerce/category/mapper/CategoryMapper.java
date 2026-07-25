package com.bhagwat.springcommerce.category.mapper;

import com.bhagwat.springcommerce.category.dto.CategoryRequest;
import com.bhagwat.springcommerce.category.dto.CategoryResponse;
import com.bhagwat.springcommerce.category.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequest request) {
        Category category = new Category();

        category.setName(request.name());
        category.setDescription(request.description());
        category.setImageUrl(request.imageUrl());
        category.setActive(request.active() == null || request.active());

        return category;
    }

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getImageUrl(),
                category.getActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    };
}
