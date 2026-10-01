package com.tastyhouse.application.rank.port.out;

import java.time.LocalDateTime;

public record MemberReviewCount(
    Long memberId,
    Long reviewCount,
    LocalDateTime lastReviewAt
) {

    public static MemberReviewCount of(
        Long memberId,
        Long reviewCount,
        LocalDateTime lastReviewAt
    ) {
        return new MemberReviewCount(
            memberId,
            reviewCount,
            lastReviewAt
        );
    }
}
