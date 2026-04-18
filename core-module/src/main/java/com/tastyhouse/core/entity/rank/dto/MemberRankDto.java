package com.tastyhouse.core.entity.rank.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.tastyhouse.core.entity.user.MemberGrade;

public record MemberRankDto(
    Long memberId,
    String nickname,
    String profileImageUrl,
    Integer reviewCount,
    Integer rankNo,
    MemberGrade grade
) {
    @QueryProjection
    public MemberRankDto {
    }
}
