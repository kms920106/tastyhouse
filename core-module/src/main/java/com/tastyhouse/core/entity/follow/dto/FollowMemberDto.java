package com.tastyhouse.core.entity.follow.dto;

import com.tastyhouse.core.entity.user.MemberGrade;

public record FollowMemberDto(
    Long memberId,
    String nickname,
    MemberGrade memberGrade,
    Long profileImageFileId,
    boolean isFollowing
) {
}
