package com.bhagwat.springcommerce.product.specification;

import com.bhagwat.springcommerce.product.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public class ProductSpecification {

    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, criteriabuilder) ->
                criteriabuilder.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<Product> isActive(Boolean active) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("active"), active);
    }
}
