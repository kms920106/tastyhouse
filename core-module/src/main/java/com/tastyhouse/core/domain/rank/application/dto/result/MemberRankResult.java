package com.tastyhouse.core.domain.rank.application.dto.result;

import com.querydsl.core.annotations.QueryProjection;
import com.tastyhouse.core.domain.member.domain.model.MemberGrade;

public record MemberRankResult(
    Long memberId,
    String nickname,
    String profileImageUrl,
    Integer reviewCount,
    Integer rankNo,
    MemberGrade grade
) {
    @QueryProjection
    public MemberRankResult {
    }
}
