package com.booknest.review.controller;

import com.booknest.review.dto.ReviewRequest;
import com.booknest.review.dto.ReviewResponse;
import com.booknest.review.service.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewController Unit Tests (Pure Mockito)")
class ReviewControllerTest {

    @Mock private ReviewServiceImpl reviewService;

    @InjectMocks
    private ReviewController reviewController;

    private ReviewResponse sampleReview;

    @BeforeEach
    void setUp() {
        sampleReview = new ReviewResponse();
        sampleReview.setReviewId(1L);
        sampleReview.setBookId(100L);
        sampleReview.setUserId(10L);
        sampleReview.setRating(5);
        sampleReview.setComment("Great book!");
    }

    @Test
    @DisplayName("addReview: returns 201")
    void addReview_success() {
        ReviewRequest req = new ReviewRequest();
        req.setBookId(100L);
        req.setRating(5);

        when(reviewService.addReview(eq(10L), anyString(), any(ReviewRequest.class))).thenReturn(sampleReview);

        ResponseEntity<ReviewResponse> response = reviewController.addReview(10L, "test@booknest.com", req);

        assertThat(response.getStatusCodeValue()).isEqualTo(201);
        assertThat(response.getBody().getReviewId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getBookReviews: returns list")
    void getBookReviews_success() {
        when(reviewService.getReviewsByBook(100L)).thenReturn(List.of(sampleReview));

        ResponseEntity<List<ReviewResponse>> response = reviewController.getBookReviews(100L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
    }


    @Test
    @DisplayName("deleteReview: returns 204")
    void deleteReview_success() {
        doNothing().when(reviewService).deleteReview(1L, 10L);

        ResponseEntity<Void> response = reviewController.deleteReview(1L, 10L);

        assertThat(response.getStatusCodeValue()).isEqualTo(204);
    }
}
