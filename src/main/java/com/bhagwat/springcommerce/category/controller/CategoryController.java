package com.bhagwat.springcommerce.category.controller;

import com.bhagwat.springcommerce.category.dto.CategoryRequest;
import com.bhagwat.springcommerce.category.dto.CategoryResponse;
import com.bhagwat.springcommerce.category.entity.Category;
import com.bhagwat.springcommerce.category.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping()
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest categoryRequest)
    {
        CategoryResponse response = categoryService.createCategory(categoryRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping()
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size,
            @RequestParam (defaultValue = "id") String sortBy,
            @RequestParam (defaultValue = "asc") String direction
    ) {
        Page<CategoryResponse> response = categoryService.getAllCategories(page, size, sortBy, direction);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoriesById(@PathVariable Long id) {
        CategoryResponse categoryResponse = categoryService.getCategoryById(id);

        return ResponseEntity.status(HttpStatus.OK).body(categoryResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategoryById(@PathVariable Long id,
                                                               @Valid @RequestBody CategoryRequest request) {
        CategoryResponse categoryResponse = categoryService.updateCategoryById(id, request);

        return ResponseEntity.status(HttpStatus.OK).body(categoryResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryById(@PathVariable Long id) {
        categoryService.deleteCategoryById(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponse>> searchByKeyword(@RequestParam String keyword) {
        List<CategoryResponse> categoryResponse = categoryService.findByNameContainingIgnoreCase(keyword);

        return ResponseEntity.status(HttpStatus.OK).body(categoryResponse);
    }

}
