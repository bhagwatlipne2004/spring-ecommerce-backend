package com.bhagwat.springcommerce.category.dto;

import java.time.LocalDateTime;

public record CategoryResponse(

        Long id,

        String name,

        String description,

        String imageUrl,

        Boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
