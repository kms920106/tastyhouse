package com.tastyhouse.core.domain.review.application.dto.command;

import java.util.List;

public record CreateReviewCommand(
    Long shopId,
    Long productId,
    Long memberId,
    Long orderProductId,
    Long orderId,
    Integer tasteRating,
    Integer amountRating,
    Integer priceRating,
    String content,
    List<Long> uploadedFileIds,
    List<String> tags
) {

    public static CreateReviewCommand of(
        Long shopId,
        Long productId,
        Long memberId,
        Long orderProductId,
        Long orderId,
        Integer tasteRating,
        Integer amountRating,
        Integer priceRating,
        String content,
        List<Long> uploadedFileIds,
        List<String> tags
    ) {
        return new CreateReviewCommand(
            shopId, productId, memberId, orderProductId, orderId,
            tasteRating, amountRating, priceRating, content, uploadedFileIds, tags
        );
    }
}
