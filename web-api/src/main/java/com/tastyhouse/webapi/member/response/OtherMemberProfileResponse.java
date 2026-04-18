package com.tastyhouse.webapi.member.response;

import com.tastyhouse.core.entity.user.MemberGrade;
import io.swagger.v3.oas.annotations.media.Schema;

public record OtherMemberProfileResponse(
    @Schema(description = "회원 ID", example = "1")
    Long id,

    @Schema(description = "닉네임", example = "맛집탐험가")
    String nickname,

    @Schema(description = "회원 등급", example = "NEWCOMER")
    MemberGrade memberGrade,

    @Schema(description = "상태 메시지", example = "오늘도 맛있는 하루!")
    String statusMessage,

    @Schema(description = "프로필 이미지 URL")
    String profileImageUrl,

    @Schema(description = "내가 이 회원을 팔로우 중인지 여부", example = "false")
    boolean isFollowing
) {
    public static OtherMemberProfileResponse from(
    Long id,
    String nickname,
    MemberGrade memberGrade,
    String statusMessage,
    String profileImageUrl,
    boolean isFollowing
    ) {
    return new OtherMemberProfileResponse(
        id,
        nickname,
        memberGrade,
        statusMessage,
        profileImageUrl,
        isFollowing
    );
    }
}
