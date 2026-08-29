package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;

public record ReviewListItemResult(
    Long id,
    Long shopId,
    Long productId,
    Long memberId,
    String memberNickname,
    Double totalRating,
    String content,
    boolean hidden,
    boolean ownerOnly,
    LocalDateTime createdAt
) {
}
