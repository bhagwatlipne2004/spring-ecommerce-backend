
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
import com.bhagwat.springcommerce.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User user;
    private Product product;
    private Review review;
    private ReviewResponse response;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        product = new Product();
        product.setId(10L);

        review = Review.builder()
                .id(100L)
                .rating(4)
                .comment("Good product")
                .user(user)
                .product(product)
                .build();

        response = new ReviewResponse(
                100L,
                4,
                "Good product",
                "Test User",
                LocalDateTime.now()
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createReviewSuccessfully() {
        var request = new CreateReviewRequest(5, "Excellent");

        when(productRepository.findById(10L))
                .thenReturn(Optional.of(product));
        when(orderRepository.existsByUserAndItemsProduct(user, product))
                .thenReturn(true);
        when(reviewRepository.existsByUserAndProduct(user, product))
                .thenReturn(false);
        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewMapper.toResponse(any(Review.class)))
                .thenReturn(response);

        ReviewResponse result = reviewService.createReview(10L, request);

        assertEquals(response, result);
        verify(reviewRepository).save(argThat(saved ->
                saved.getRating() == 5
                        && saved.getComment().equals("Excellent")
                        && saved.getUser().equals(user)
                        && saved.getProduct().equals(product)
        ));
        verify(reviewMapper).toResponse(any(Review.class));
    }

    @Test
    void createReviewThrowsWhenProductDoesNotExist() {
        when(productRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reviewService.createReview(
                        10L, new CreateReviewRequest(5, "Excellent")));

        verifyNoInteractions(orderRepository, reviewMapper);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReviewThrowsWhenProductWasNotPurchased() {
        when(productRepository.findById(10L))
                .thenReturn(Optional.of(product));
        when(orderRepository.existsByUserAndItemsProduct(user, product))
                .thenReturn(false);

        assertThrows(ProductNotPurchasedException.class,
                () -> reviewService.createReview(
                        10L, new CreateReviewRequest(5, "Excellent")));

        verify(reviewRepository, never()).save(any());
        verifyNoInteractions(reviewMapper);
    }

    @Test
    void createReviewThrowsWhenReviewAlreadyExists() {
        when(productRepository.findById(10L))
                .thenReturn(Optional.of(product));
        when(orderRepository.existsByUserAndItemsProduct(user, product))
                .thenReturn(true);
        when(reviewRepository.existsByUserAndProduct(user, product))
                .thenReturn(true);

        assertThrows(DuplicateReviewException.class,
                () -> reviewService.createReview(
                        10L, new CreateReviewRequest(5, "Excellent")));

        verify(reviewRepository, never()).save(any());
        verifyNoInteractions(reviewMapper);
    }

    @Test
    void getReviewsByProductSuccessfully() {
        when(productRepository.findById(10L))
                .thenReturn(Optional.of(product));
        when(reviewRepository.findByProduct(product))
                .thenReturn(List.of(review));
        when(reviewMapper.toResponse(review)).thenReturn(response);

        List<ReviewResponse> result =
                reviewService.getReviewsByProduct(10L);

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));
        verify(reviewMapper).toResponse(review);
    }

    @Test
    void getReviewsByProductReturnsEmptyListWhenNoReviewsExist() {
        when(productRepository.findById(10L))
                .thenReturn(Optional.of(product));
        when(reviewRepository.findByProduct(product))
                .thenReturn(List.of());

        List<ReviewResponse> result =
                reviewService.getReviewsByProduct(10L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(reviewMapper);
    }

    @Test
    void getReviewsByProductThrowsWhenProductDoesNotExist() {
        when(productRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reviewService.getReviewsByProduct(10L));

        verifyNoInteractions(reviewRepository, reviewMapper);
    }

    @Test
    void updateReviewSuccessfully() {
        var request = new UpdateReviewRequest(5, "Updated comment");

        when(reviewRepository.findById(100L))
                .thenReturn(Optional.of(review));
        when(reviewRepository.save(review)).thenReturn(review);
        when(reviewMapper.toResponse(review)).thenReturn(response);

        ReviewResponse result = reviewService.updateReview(100L, request);

        assertEquals(response, result);
        assertEquals(5, review.getRating());
        assertEquals("Updated comment", review.getComment());
        verify(reviewRepository).save(review);
        verify(reviewMapper).toResponse(review);
    }

    @Test
    void updateReviewThrowsWhenReviewDoesNotExist() {
        when(reviewRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class,
                () -> reviewService.updateReview(
                        100L, new UpdateReviewRequest(5, "Updated")));

        verify(reviewRepository, never()).save(any());
        verifyNoInteractions(reviewMapper);
    }

    @Test
    void updateReviewThrowsWhenReviewBelongsToAnotherUser() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        review.setUser(anotherUser);

        when(reviewRepository.findById(100L))
                .thenReturn(Optional.of(review));

        assertThrows(ReviewNotFoundException.class,
                () -> reviewService.updateReview(
                        100L, new UpdateReviewRequest(5, "Updated")));

        verify(reviewRepository, never()).save(any());
        verifyNoInteractions(reviewMapper);
    }

    @Test
    void deleteReviewSuccessfully() {
        when(reviewRepository.findById(100L))
                .thenReturn(Optional.of(review));

        assertDoesNotThrow(() -> reviewService.deleteReview(100L));

        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReviewThrowsWhenReviewDoesNotExist() {
        when(reviewRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class,
                () -> reviewService.deleteReview(100L));

        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void deleteReviewThrowsWhenReviewBelongsToAnotherUser() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        review.setUser(anotherUser);

        when(reviewRepository.findById(100L))
                .thenReturn(Optional.of(review));

        assertThrows(ReviewNotFoundException.class,
                () -> reviewService.deleteReview(100L));

        verify(reviewRepository, never()).delete(any());
    }
}
