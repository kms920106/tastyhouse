package com.tastyhouse.application.member.follow.port.out;

import com.tastyhouse.domain.member.model.MemberGrade;

public record FollowMemberResult(
    Long memberId,
    String nickname,
    MemberGrade memberGrade,
    String profileImageUrl,
    boolean following
) {
}
