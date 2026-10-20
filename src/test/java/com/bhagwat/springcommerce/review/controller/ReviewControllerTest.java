
package com.bhagwat.springcommerce.review.controller;

import com.bhagwat.springcommerce.review.dto.CreateReviewRequest;
import com.bhagwat.springcommerce.review.dto.ReviewResponse;
import com.bhagwat.springcommerce.review.dto.UpdateReviewRequest;
import com.bhagwat.springcommerce.review.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReviewControllerTest {

    private ReviewService reviewService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reviewService = mock(ReviewService.class);
        objectMapper = new ObjectMapper().findAndRegisterModules();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ReviewController(reviewService))
                .setValidator(validator)
                .build();
    }

    private ReviewResponse sampleReview() {
        return new ReviewResponse(
                1L,
                5,
                "Excellent product",
                "Test User",
                LocalDateTime.of(2026, 10, 20, 10, 30)
        );
    }

    @Test
    void createReview_shouldReturnCreated() throws Exception {
        when(reviewService.createReview(eq(10L), any(CreateReviewRequest.class)))
                .thenReturn(sampleReview());

        CreateReviewRequest request =
                new CreateReviewRequest(5, "Excellent product");

        mockMvc.perform(post("/api/v1/products/{productId}/reviews", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Excellent product"))
                .andExpect(jsonPath("$.userName").value("Test User"));

        verify(reviewService).createReview(
                eq(10L), any(CreateReviewRequest.class));
    }

    @Test
    void createReview_whenRatingIsTooHigh_shouldReturnBadRequest()
            throws Exception {
        mockMvc.perform(post("/api/v1/products/{productId}/reviews", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rating": 6, "comment": "Excellent product"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewService);
    }

    @Test
    void getReviews_shouldReturnReviewsForProduct() throws Exception {
        when(reviewService.getReviewsByProduct(10L))
                .thenReturn(List.of(sampleReview()));

        mockMvc.perform(get("/api/v1/products/{productId}/reviews", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].userName").value("Test User"));

        verify(reviewService).getReviewsByProduct(10L);
    }

    @Test
    void getReviews_whenNoReviews_shouldReturnEmptyArray() throws Exception {
        when(reviewService.getReviewsByProduct(10L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/products/{productId}/reviews", 10L))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(reviewService).getReviewsByProduct(10L);
    }

    @Test
    void updateReview_shouldReturnUpdatedReview() throws Exception {
        when(reviewService.updateReview(eq(1L), any(UpdateReviewRequest.class)))
                .thenReturn(sampleReview());

        UpdateReviewRequest request =
                new UpdateReviewRequest(5, "Excellent product");

        mockMvc.perform(put("/api/v1/reviews/{reviewId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(5));

        verify(reviewService).updateReview(
                eq(1L), any(UpdateReviewRequest.class));
    }

    @Test
    void updateReview_whenCommentIsBlank_shouldReturnBadRequest()
            throws Exception {
        mockMvc.perform(put("/api/v1/reviews/{reviewId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rating": 4, "comment": " "}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewService);
    }

    @Test
    void deleteReview_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/reviews/{reviewId}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(reviewService).deleteReview(1L);
    }
}
