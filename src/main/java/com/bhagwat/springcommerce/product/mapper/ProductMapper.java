package com.bhagwat.springcommerce.product.mapper;

import com.bhagwat.springcommerce.category.dto.CategorySummary;
import com.bhagwat.springcommerce.category.entity.Category;
import com.bhagwat.springcommerce.product.dto.ProductRequest;
import com.bhagwat.springcommerce.product.dto.ProductResponse;
import com.bhagwat.springcommerce.product.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request, Category category) {
        Product product = new Product();
        product.setName(request.name());
        product.setActive(request.active());
        product.setDescription(request.description());
        product.setImageUrl(request.imageUrl());
        product.setStockQuantity(request.stockQuantity());
        product.setPrice(request.price());
        product.setCategory(category);

        return product;
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getActive(),
                toCategorySummary(product.getCategory())
        );
    }

    private CategorySummary toCategorySummary(Category category) {
        return new CategorySummary(
                category.getId(),
                category.getName()
        );
    }
}
