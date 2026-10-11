package com.tastyhouse.application.member.port.out;

public record MemberReviewCount(
    Long memberId,
    Long reviewCount
) {

    public static MemberReviewCount of(
        Long memberId,
        Long reviewCount
    ) {
        return new MemberReviewCount(
            memberId,
            reviewCount
        );
    }
}
