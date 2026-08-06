package com.bhagwat.springcommerce.product.dto;

import java.math.BigDecimal;

public record ProductRequest(

        String name,

        String description,

        BigDecimal price,

        Integer stockQuantity,

        String imageUrl,

        Boolean active,

        Long categoryId
) {
}
