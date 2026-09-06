package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewSubmitResultView(
    Long reviewId,
    Long productId,
    Double tasteRating,
    Double amountRating,
    Double priceRating,
    Double totalRating,
    String content,
    List<String> imageUrls,
    List<String> tags,
    LocalDateTime createdAt
) {
}
