package com.tastyhouse.core.domain.review.application.dto.command;

import java.util.List;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record ReviewCreateCommand(
    Long shopId,
    Long productId,
    MemberId memberId,
    Long orderProductId,
    Long orderId,
    Integer tasteRating,
    Integer amountRating,
    Integer priceRating,
    String content,
    List<Long> uploadedFileIds,
    List<String> tags
) {

    public static ReviewCreateCommand of(
        Long shopId,
        Long productId,
        MemberId memberId,
        Long orderProductId,
        Long orderId,
        Integer tasteRating,
        Integer amountRating,
        Integer priceRating,
        String content,
        List<Long> uploadedFileIds,
        List<String> tags
    ) {
        return new ReviewCreateCommand(
            shopId, productId, memberId, orderProductId, orderId,
            tasteRating, amountRating, priceRating, content, uploadedFileIds, tags
        );
    }
}
