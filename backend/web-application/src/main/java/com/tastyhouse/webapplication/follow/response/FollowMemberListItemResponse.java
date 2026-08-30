package com.tastyhouse.webapplication.follow.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "팔로우 회원 목록 아이템 응답")
public record FollowMemberListItemResponse(
    @Schema(description = "회원 ID", example = "1")
    Long memberId,

    @Schema(description = "닉네임", example = "맛집탐험가")
    String nickname,

    @Schema(description = "회원 등급", example = "NEWCOMER")
    String grade,

    @Schema(description = "프로필 이미지 URL")
    String profileImageUrl,

    @Schema(description = "내가 팔로우 중인지 여부", example = "true")
    boolean following
) {
    public static FollowMemberListItemResponse of(
        Long memberId,
        String nickname,
        String grade,
        String profileImageUrl,
        boolean following
    ) {
        return new FollowMemberListItemResponse(
            memberId,
            nickname,
            grade,
            profileImageUrl,
            following
        );
    }
}
