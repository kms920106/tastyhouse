package com.tastyhouse.application.follow.port.out;

public record FollowMemberSearchResult(
    Long memberId,
    String nickname,
    String grade,
    String profileImageUrl,
    boolean following
) {
}
