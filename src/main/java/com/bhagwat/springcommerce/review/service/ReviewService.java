package com.bhagwat.springcommerce.review.service;

import com.bhagwat.springcommerce.review.dto.CreateReviewRequest;
import com.bhagwat.springcommerce.review.dto.ReviewResponse;
import com.bhagwat.springcommerce.review.dto.UpdateReviewRequest;

import java.util.List;

public interface ReviewService {

    ReviewResponse createReview(
            Long productId,
            CreateReviewRequest request
    );

    List<ReviewResponse> getReviewsByProduct(
            Long productId
    );

    ReviewResponse updateReview(
            Long reviewId,
            UpdateReviewRequest request
    );

    void deleteReview(Long reviewId);

}