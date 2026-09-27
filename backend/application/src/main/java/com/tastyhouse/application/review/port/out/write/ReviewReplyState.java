package com.tastyhouse.application.review.port.out.write;

import java.time.LocalDateTime;

public record ReviewReplyState(
    Long id,
    Long commentId,
    Long memberId,
    Long replyToMemberId,
    String content,
    boolean hidden,
    LocalDateTime createdAt
) {
}
