package com.bhagwat.springcommerce.product.dto;

import com.bhagwat.springcommerce.category.dto.CategorySummary;
import java.math.BigDecimal;

public record ProductResponse(

        Long id,

        String name,

        String description,

        BigDecimal price,

        Integer stockQuantity,

        String imageUrl,

        Boolean active,

        CategorySummary category

) {
}
