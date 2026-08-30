package com.tastyhouse.webapi.member.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import com.tastyhouse.webapplication.member.port.in.MemberProfileUpdateCommand;

@Schema(description = "프로필 수정 요청")
public record UpdateProfileRequest(
    @Size(max = 50, message = "닉네임은 최대 50자까지 입력 가능합니다.")
    @Schema(description = "닉네임", example = "맛집탐험가")
    String nickname,

    @Size(max = 200, message = "상태메시지는 최대 200자까지 입력 가능합니다.")
    @Schema(description = "상태메시지", example = "오늘도 맛있는 하루!")
    String statusMessage,

    @Schema(description = "프로필 이미지 파일 ID", example = "42")
    Long profileImageFileId
) {

    /**
     * 인증 주체의 {@code memberId}를 주입받아 command로 변환한다.
     */
    public MemberProfileUpdateCommand toCommand(Long memberId) {
        return new MemberProfileUpdateCommand(
            memberId,
            nickname,
            statusMessage,
            profileImageFileId
        );
    }
}
