package com.tastyhouse.application.member.port.out;

public record MemberStatsResult(
    long reviewCount,
    long followingCount,
    long followerCount
) {
}
