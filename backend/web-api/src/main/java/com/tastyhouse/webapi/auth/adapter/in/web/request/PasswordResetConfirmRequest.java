package com.tastyhouse.webapi.auth.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "비밀번호 재설정 요청")
public record PasswordResetConfirmRequest(

    @Schema(description = "비밀번호 재설정 토큰 (인증코드 확인 후 발급)")
    @NotBlank(message = "비밀번호 재설정 토큰을 입력해주세요.")
    String passwordResetToken,

    @Schema(description = "새 비밀번호 (영문, 숫자, 특수문자 포함 8~20자)", example = "NewPassword1!")
    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Pattern(
    regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,20}$",
    message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8~20자로 입력해주세요."
    )
    String newPassword,

    @Schema(description = "새 비밀번호 확인", example = "NewPassword1!")
    @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
    String newPasswordConfirm
) {}
