package com.tastyhouse.webapi.follow.response;

import com.tastyhouse.core.entity.follow.dto.FollowMemberDto;
import com.tastyhouse.core.entity.user.MemberGrade;
import io.swagger.v3.oas.annotations.media.Schema;

public record FollowMemberResponse(
    @Schema(description = "회원 ID", example = "1")
    Long memberId,

    @Schema(description = "닉네임", example = "맛집탐험가")
    String nickname,

    @Schema(description = "회원 등급", example = "NEWCOMER")
    MemberGrade memberGrade,

    @Schema(description = "프로필 이미지 URL")
    String profileImageUrl,

    @Schema(description = "내가 팔로우 중인지 여부", example = "true")
    boolean following
) {
    public static FollowMemberResponse of(FollowMemberDto dto, String profileImageUrl) {
    return new FollowMemberResponse(
        dto.memberId(),
        dto.nickname(),
        dto.memberGrade(),
        profileImageUrl,
        dto.isFollowing()
    );
    }
}
