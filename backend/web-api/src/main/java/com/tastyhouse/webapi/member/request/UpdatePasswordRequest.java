package com.tastyhouse.webapi.member.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 변경 요청")
public record UpdatePasswordRequest(
    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @Schema(description = "새 비밀번호 (8~20자)", example = "newPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    String newPassword,

    @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
    @Schema(description = "새 비밀번호 확인", example = "newPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    String newPasswordConfirm
) {
}
