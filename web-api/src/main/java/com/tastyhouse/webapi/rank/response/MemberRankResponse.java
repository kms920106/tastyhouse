package com.tastyhouse.webapi.rank.response;

import com.tastyhouse.core.entity.user.MemberGrade;

public record MemberRankResponse(
    Long memberId,
    String nickname,
    String profileImageUrl,
    Integer reviewCount,
    Integer rankNo,
    MemberGrade grade
) {
    public static MemberRankResponse of(
        Long memberId,
        String nickname,
        String profileImageUrl,
        Integer reviewCount,
        Integer rankNo,
        MemberGrade grade
    ) {
        return new MemberRankResponse(
            memberId,
            nickname,
            profileImageUrl,
            reviewCount,
            rankNo,
            grade
        );
    }
}
