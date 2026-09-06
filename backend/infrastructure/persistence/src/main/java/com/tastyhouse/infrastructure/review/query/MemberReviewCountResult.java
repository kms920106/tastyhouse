package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;

public record MemberReviewCountResult(
    Long memberId,
    Long reviewCount,
    LocalDateTime lastReviewAt
) {
}
