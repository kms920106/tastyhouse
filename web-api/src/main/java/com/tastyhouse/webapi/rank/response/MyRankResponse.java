package com.tastyhouse.webapi.rank.response;

import com.tastyhouse.core.entity.user.MemberGrade;

public record MyRankResponse(
    Long memberId,
    String nickname,
    String profileImageUrl,
    Integer reviewCount,
    Integer rankNo,
    MemberGrade grade
) {
    public static MyRankResponse from(
        Long memberId,
        String nickname,
        String profileImageUrl,
        Integer reviewCount,
        Integer rankNo,
        MemberGrade grade
    ) {
        return new MyRankResponse(
            memberId,
            nickname,
            profileImageUrl,
            reviewCount,
            rankNo,
            grade
        );
    }
}
