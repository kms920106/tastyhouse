package com.tastyhouse.core.domain.review.application.dto.result;

import java.time.LocalDateTime;

public record ReviewCommentResult(
    Long id,
    Long reviewId,
    Long memberId,
    String content,
    Boolean isHidden,
    LocalDateTime createdAt
) {
}
