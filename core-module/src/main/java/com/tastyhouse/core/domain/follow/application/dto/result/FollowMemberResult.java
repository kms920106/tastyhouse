package com.tastyhouse.core.domain.follow.application.dto.result;

import com.tastyhouse.core.entity.user.MemberGrade;

public record FollowMemberResult(
    Long memberId,
    String nickname,
    MemberGrade memberGrade,
    String profileImageFilePath,
    boolean isFollowing
) {
}
