package com.tastyhouse.domain.rank.port;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;

public record MemberReviewCount(
    MemberId memberId,
    Long reviewCount,
    LocalDateTime lastReviewAt
) {
    public static MemberReviewCount of(
        MemberId memberId,
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
