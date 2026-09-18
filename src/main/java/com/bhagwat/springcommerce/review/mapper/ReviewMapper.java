package com.bhagwat.springcommerce.review.mapper;

import com.bhagwat.springcommerce.review.dto.ReviewResponse;
import com.bhagwat.springcommerce.review.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {

        return new ReviewResponse(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getUser().getFirstName() + " " + review.getUser().getLastName(),
                review.getCreatedAt()
        );
    }
}