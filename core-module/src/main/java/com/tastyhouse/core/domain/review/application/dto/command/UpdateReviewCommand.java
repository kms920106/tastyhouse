package com.tastyhouse.core.domain.review.application.dto.command;

import java.util.List;

import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

public record UpdateReviewCommand(
    ReviewId reviewId,
    Long memberId,
    Integer tasteRating,
    Integer amountRating,
    Integer priceRating,
    String content,
    List<Long> uploadedFileIds,
    List<String> tags
) {

    public static UpdateReviewCommand of(
        ReviewId reviewId,
        Long memberId,
        Integer tasteRating,
        Integer amountRating,
        Integer priceRating,
        String content,
        List<Long> uploadedFileIds,
        List<String> tags
    ) {
        return new UpdateReviewCommand(
            reviewId, memberId, tasteRating, amountRating, priceRating, content, uploadedFileIds, tags
        );
    }
}
