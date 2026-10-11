package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;

public record ReviewReplyItemResult(
    Long id,
    Long commentId,
    Long memberId,
    String memberNickname,
    String memberProfileImageUrl,
    Long replyToMemberId,
    String replyToMemberNickname,
    String content,
    LocalDateTime createdAt
) {
}
