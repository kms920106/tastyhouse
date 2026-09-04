package com.tastyhouse.webapi.follow.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.follow.port.out.FollowMemberSearchResult;

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
    public static FollowMemberSearchListItemResponse from(FollowMemberSearchResult result) {
        return new FollowMemberSearchListItemResponse(
            result.memberId(),
            result.nickname(),
            result.grade(),
            result.profileImageUrl(),
            result.following()
        );
    }
}
