package com.bhagwat.springcommerce.review.dto;

import java.time.LocalDateTime;

public record ReviewResponse(

        Long id,

        Integer rating,

        String comment,

        String userName,

        LocalDateTime createdAt

) {
}