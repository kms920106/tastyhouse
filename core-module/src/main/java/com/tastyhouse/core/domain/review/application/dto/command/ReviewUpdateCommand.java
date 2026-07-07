package com.tastyhouse.core.domain.review.application.dto.command;

import java.util.List;

import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

public record ReviewUpdateCommand(
    ReviewId reviewId,
    Long memberId,
    Integer tasteRating,
    Integer amountRating,
    Integer priceRating,
    String content,
    List<Long> uploadedFileIds,
    List<String> tags
) {

    public static ReviewUpdateCommand of(
        ReviewId reviewId,
        Long memberId,
        Integer tasteRating,
        Integer amountRating,
        Integer priceRating,
        String content,
        List<Long> uploadedFileIds,
        List<String> tags
    ) {
        return new ReviewUpdateCommand(
            reviewId, memberId, tasteRating, amountRating, priceRating, content, uploadedFileIds, tags
        );
    }
}
