package com.tastyhouse.application.member.follow.port.out.write;

public record MemberFollowState(
    Long id,
    Long followerId,
    Long followingId
) {
}
