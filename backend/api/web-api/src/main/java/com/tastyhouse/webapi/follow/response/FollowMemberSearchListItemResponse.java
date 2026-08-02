package com.tastyhouse.webapi.follow.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 검색 목록 아이템 응답")
public record FollowMemberSearchListItemResponse(
    @Schema(description = "회원 ID", example = "1")
    Long memberId,

    @Schema(description = "닉네임", example = "맛집탐험가")
    String nickname,

    @Schema(description = "회원 등급", example = "NEWCOMER")
    String grade,

    @Schema(description = "프로필 이미지 URL")
    String profileImageUrl,

    @Schema(description = "내가 팔로우 중인지 여부", example = "false")
    boolean following
) {
    public static FollowMemberSearchListItemResponse of(
        Long memberId,
        String nickname,
        String grade,
        String profileImageUrl,
        boolean isFollowing
    ) {
        return new FollowMemberSearchListItemResponse(
            memberId,
            nickname,
            grade,
            profileImageUrl,
            isFollowing
        );
    }
}
