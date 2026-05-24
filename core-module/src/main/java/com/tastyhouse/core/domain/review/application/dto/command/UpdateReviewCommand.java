package com.tastyhouse.core.domain.review.application.dto.command;

import java.util.List;

public record UpdateReviewCommand(
    Long reviewId,
    Long memberId,
    Integer tasteRating,
    Integer amountRating,
    Integer priceRating,
    String content,
    List<Long> uploadedFileIds,
    List<String> tags
) {
}
