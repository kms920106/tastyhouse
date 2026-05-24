package com.tastyhouse.webapi.rank.response;

import com.tastyhouse.core.domain.member.domain.model.MemberGrade;

public record MemberRankListItemResponse(
    Long memberId,
    String nickname,
    String profileImageUrl,
    Integer reviewCount,
    Integer rankNo,
    MemberGrade grade
) {
    public static MemberRankListItemResponse of(
        Long memberId,
        String nickname,
        String profileImageUrl,
        Integer reviewCount,
        Integer rankNo,
        MemberGrade grade
    ) {
        return new MemberRankListItemResponse(
            memberId,
            nickname,
            profileImageUrl,
            reviewCount,
            rankNo,
            grade
        );
    }
}
