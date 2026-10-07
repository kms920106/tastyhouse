package com.tastyhouse.application.follow.port.in;

public interface FollowStatusQueryUseCase {

    boolean isFollowing(Long viewerMemberId, Long targetMemberId);
}
