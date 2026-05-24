package com.tastyhouse.core.domain.review.application.dto.result;

import java.time.LocalDateTime;

public record ReviewReplyResult(
    Long id,
    Long commentId,
    Long memberId,
    Long replyToMemberId,
    String content,
    Boolean isHidden,
    LocalDateTime createdAt
) {
}
