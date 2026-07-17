package com.tastyhouse.core.domain.member.follow.application.dto.result;

import com.tastyhouse.core.domain.member.domain.model.MemberGrade;

public record FollowMemberResult(
    Long memberId,
    String nickname,
    MemberGrade memberGrade,
    String profileImageFilePath,
    boolean following
) {
}
