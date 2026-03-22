package com.tastyhouse.webapi.auth.request;

import com.tastyhouse.core.entity.user.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record SignUpRequest(

    @Schema(description = "아이디 (이메일 형식)", example = "user@example.com")
    @NotBlank(message = "아이디를 입력해주세요.")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$",
        message = "유효한 이메일 형식으로 입력해주세요."
    )
    String username,

    @Schema(description = "비밀번호 (영문, 숫자, 특수문자 포함 8~20자)", example = "Password1!")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,20}$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8~20자로 입력해주세요."
    )
    String password,

    @Schema(description = "비밀번호 확인", example = "Password1!")
    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    String passwordConfirm,

    @Schema(description = "닉네임 (2~20자)", example = "맛집탐험가")
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자로 입력해주세요.")
    String nickname,

    @Schema(description = "이름", example = "홍길동")
    @NotBlank(message = "이름을 입력해주세요.")
    @Size(max = 100, message = "이름은 100자 이하로 입력해주세요.")
    String fullName,

    @Schema(description = "성별", example = "MALE")
    @NotNull(message = "성별을 선택해주세요.")
    Gender gender,

    @Schema(description = "생년월일 (YYYYMMDD 형식)", example = "19900101")
    Integer birthDate,

    @Schema(description = "휴대폰번호 (010XXXXXXXX 형식)", example = "01012345678")
    @Pattern(regexp = "01[0-9]{8,9}", message = "올바른 휴대폰번호 형식으로 입력해주세요.")
    String phoneNumber,

    @Schema(description = "마케팅 정보 수신 동의", example = "false")
    Boolean marketingInfoEnabled,

    @Schema(description = "이벤트 정보 수신 동의", example = "false")
    Boolean eventInfoEnabled,

    @Schema(description = "SMS 휴대폰 인증 토큰 (휴대폰번호 입력 시 필수)")
    String phoneVerifyToken,

    @Schema(description = "이메일 인증 토큰 (회원가입 시 필수)")
    @NotBlank(message = "이메일 인증 토큰을 입력해주세요.")
    String emailVerifyToken
) {}
