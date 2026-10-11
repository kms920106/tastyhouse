package com.tastyhouse.application.member.follow.port.out;

public record FollowMemberResult(
    Long memberId,
    String nickname,
    String memberGrade,
    String profileImageUrl,
    boolean following
) {
}
