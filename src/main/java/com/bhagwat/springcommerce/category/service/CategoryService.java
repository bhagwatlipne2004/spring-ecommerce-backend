package com.bhagwat.springcommerce.category.service;

import com.bhagwat.springcommerce.category.dto.CategoryRequest;
import com.bhagwat.springcommerce.category.dto.CategoryResponse;
import com.bhagwat.springcommerce.category.entity.Category;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

}
