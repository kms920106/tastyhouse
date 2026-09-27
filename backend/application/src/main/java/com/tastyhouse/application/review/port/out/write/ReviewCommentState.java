package com.tastyhouse.application.review.port.out.write;

import java.time.LocalDateTime;

public record ReviewCommentState(
    Long id,
    Long reviewId,
    Long memberId,
    String content,
    boolean hidden,
    LocalDateTime createdAt
) {
}
