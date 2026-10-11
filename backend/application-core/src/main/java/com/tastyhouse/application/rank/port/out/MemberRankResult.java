package com.tastyhouse.application.rank.port.out;

public record MemberRankResult(
    Long memberId,
    String nickname,
    String profileImageUrl,
    Integer reviewCount,
    Integer rankNo,
    String grade
) {
}
