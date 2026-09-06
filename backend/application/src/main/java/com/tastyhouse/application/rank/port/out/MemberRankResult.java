package com.tastyhouse.application.rank.port.out;

import com.tastyhouse.domain.member.model.MemberGrade;

public record MemberRankResult(
    Long memberId,
    String nickname,
    String profileImageUrl,
    Integer reviewCount,
    Integer rankNo,
    MemberGrade grade
) {
}
