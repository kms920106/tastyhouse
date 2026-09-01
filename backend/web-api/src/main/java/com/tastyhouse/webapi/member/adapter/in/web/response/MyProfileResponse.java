package com.tastyhouse.webapi.member.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;

@Schema(description = "내 프로필")
public record MyProfileResponse(
    @Schema(description = "회원 ID(PK)", example = "1")
    Long id,

    @Schema(description = "닉네임", example = "맛집탐험가")
    String nickname,

    @Schema(description = "회원 등급", example = "NEWCOMER")
    String grade,

    @Schema(description = "상태 메시지", example = "오늘도 맛있는 하루!")
    String statusMessage,

    @Schema(description = "프로필 이미지 URL")
    String profileImageUrl
) {
    public static MyProfileResponse from(MemberWithProfileImageResult result) {
        return new MyProfileResponse(
            result.id(),
            result.nickname(),
            result.memberGrade().name(),
            result.statusMessage(),
            result.profileImageUrl()
        );
    }
}
