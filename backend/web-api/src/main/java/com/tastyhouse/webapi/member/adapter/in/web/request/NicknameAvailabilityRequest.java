package com.tastyhouse.webapi.member.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "닉네임 중복확인 요청")
public record NicknameAvailabilityRequest(
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하여야 합니다.")
    @Schema(description = "확인할 닉네임 (1~20자)", example = "맛있는탐험가", requiredMode = Schema.RequiredMode.REQUIRED)
    String nickname
) {
}
