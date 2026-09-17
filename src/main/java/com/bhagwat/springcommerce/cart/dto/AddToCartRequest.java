package com.bhagwat.springcommerce.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(

        @NotNull
        Long productId,

        @NotNull
        @Min(1)
        Integer quantity

) {
}