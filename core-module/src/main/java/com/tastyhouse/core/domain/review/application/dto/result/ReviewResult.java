package com.tastyhouse.core.domain.review.application.dto.result;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResult(
    Long id,
    Long productId,
    Double tasteRating,
    Double amountRating,
    Double priceRating,
    Double totalRating,
    String content,
    List<Long> uploadedFileIds,
    List<String> tags,
    LocalDateTime createdAt
) {
}
