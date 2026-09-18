package com.bhagwat.springcommerce.review.dto;

import jakarta.validation.constraints.*;

public record CreateReviewRequest(

        @NotNull
        @Min(1)
        @Max(5)
        Integer rating,

        @NotBlank
        @Size(max = 1000)
        String comment

) {
}