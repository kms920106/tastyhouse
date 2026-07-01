package com.tastyhouse.webapi.member.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.member.domain.model.MemberGrade;

public record MemberProfileResponse(
    @Schema(description = "닉네임", example = "맛집탐험가")
    String nickname,

    @Schema(description = "회원 등급", example = "NEWCOMER")
    MemberGrade memberGrade,

    @Schema(description = "상태 메시지", example = "오늘도 맛있는 하루!")
    String statusMessage,

    @Schema(description = "프로필 이미지 URL")
    String profileImageUrl
) {
    public static MemberProfileResponse from(
        String nickname,
        MemberGrade memberGrade,
        String statusMessage,
        String profileImageUrl
    ) {
        return new MemberProfileResponse(
            nickname,
            memberGrade,
            statusMessage,
            profileImageUrl
        );
    }
}
