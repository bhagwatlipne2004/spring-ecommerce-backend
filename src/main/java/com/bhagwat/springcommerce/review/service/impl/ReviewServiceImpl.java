package com.bhagwat.springcommerce.review.service.impl;

import com.bhagwat.springcommerce.common.exception.DuplicateReviewException;
import com.bhagwat.springcommerce.common.exception.ProductNotPurchasedException;
import com.bhagwat.springcommerce.common.exception.ResourceNotFoundException;
import com.bhagwat.springcommerce.common.exception.ReviewNotFoundException;
import com.bhagwat.springcommerce.order.repository.OrderRepository;
import com.bhagwat.springcommerce.product.entity.Product;
import com.bhagwat.springcommerce.product.repository.ProductRepository;
import com.bhagwat.springcommerce.review.dto.CreateReviewRequest;
import com.bhagwat.springcommerce.review.dto.ReviewResponse;
import com.bhagwat.springcommerce.review.dto.UpdateReviewRequest;
import com.bhagwat.springcommerce.review.entity.Review;
import com.bhagwat.springcommerce.review.mapper.ReviewMapper;
import com.bhagwat.springcommerce.review.repository.ReviewRepository;
import com.bhagwat.springcommerce.review.service.ReviewService;
import com.bhagwat.springcommerce.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    private final ProductRepository productRepository;

    private final OrderRepository orderRepository;

    private final ReviewMapper reviewMapper;


    @Override
    public ReviewResponse createReview(Long productId,
                                       CreateReviewRequest request) {

        User user = getCurrentUser();

        Product product = getProduct(productId);

        validatePurchase(user, product);

        if (reviewRepository.existsByUserAndProduct(user, product)) {
            throw new DuplicateReviewException(productId);
        }

        Review review = Review.builder()
                .rating(request.rating())
                .comment(request.comment())
                .user(user)
                .product(product)
                .build();

        Review savedReview = reviewRepository.save(review);

        return reviewMapper.toResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProduct(Long productId) {

        Product product = getProduct(productId);

        List<Review> reviews = reviewRepository.findByProduct(product);

        return reviews.stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Override
    public ReviewResponse updateReview(Long reviewId,
                                       UpdateReviewRequest request) {

        User user = getCurrentUser();

        Review review = getReview(reviewId);

        if (!review.getUser().getId().equals(user.getId())) {
            throw new ReviewNotFoundException(reviewId);
        }

        review.setRating(request.rating());
        review.setComment(request.comment());

        Review updatedReview = reviewRepository.save(review);

        return reviewMapper.toResponse(updatedReview);
    }

    @Override
    public void deleteReview(Long reviewId) {

        User user = getCurrentUser();

        Review review = getReview(reviewId);

        if (!review.getUser().getId().equals(user.getId())) {
            throw new ReviewNotFoundException(reviewId);
        }

        reviewRepository.delete(review);
    }

//  Helper Methods

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return (User) authentication.getPrincipal();
    }

    private Product getProduct(Long productId) {

        return productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product",
                                "id",
                                productId));
    }

    private Review getReview(Long reviewId) {

        return reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ReviewNotFoundException(reviewId));
    }

    private void validatePurchase(User user, Product product) {

        boolean purchased = orderRepository
                .existsByUserAndItemsProduct(user, product);

        if (!purchased) {
            throw new ProductNotPurchasedException(product.getId());
        }
    }
}