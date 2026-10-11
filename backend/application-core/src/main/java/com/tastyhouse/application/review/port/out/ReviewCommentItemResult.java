package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;

public record ReviewCommentItemResult(
    Long id,
    Long reviewId,
    Long memberId,
    String memberNickname,
    String memberProfileImageUrl,
    String content,
    LocalDateTime createdAt
) {
}
