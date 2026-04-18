package com.tastyhouse.webapi.rank.response;

import com.tastyhouse.core.entity.rank.dto.MemberRankDto;
import com.tastyhouse.core.entity.user.MemberGrade;

public record MyRankResponse(
    Long memberId,
    String nickname,
    String profileImageUrl,
    Integer reviewCount,
    Integer rankNo,
    MemberGrade grade
) {
    public static MyRankResponse from(MemberRankDto dto) {
    if (dto == null) {
        return null;
    }
    return new MyRankResponse(
        dto.memberId(),
        dto.nickname(),
        dto.profileImageUrl(),
        dto.reviewCount(),
        dto.rankNo(),
        dto.grade()
    );
    }
}
