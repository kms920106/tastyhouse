package com.tastyhouse.domain.member.port;

import com.tastyhouse.domain.member.vo.MemberId;

public record MemberReviewCount(
    MemberId memberId,
    Long reviewCount
) {
    public static MemberReviewCount of(
        MemberId memberId,
        Long reviewCount
    ) {
        return new MemberReviewCount(
            memberId,
            reviewCount
        );
    }
}
