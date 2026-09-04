package com.tastyhouse.webapi.auth.adapter.in.web.request;

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
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,20}$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8~20자로 입력해주세요."
    )
    String password,

    @Schema(description = "닉네임 (2~20자)", example = "맛집탐험가")
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자로 입력해주세요.")
    String nickname,

    @Schema(description = "이름", example = "홍길동")
    @NotBlank(message = "이름을 입력해주세요.")
    @Size(max = 100, message = "이름은 100자 이하로 입력해주세요.")
    String fullName,

    @Schema(description = "성별", example = "MALE", allowableValues = {"MALE", "FEMALE"})
    @NotBlank(message = "성별을 선택해주세요.")
    String gender,

    @NotNull(message = "생년월일을 입력해주세요.")
    @Schema(description = "생년월일 (YYYYMMDD 형식)", example = "19900101")
    Integer birthDate,

    @NotNull(message = "휴대폰번호를 입력해주세요.")
    @Pattern(regexp = "^\\d{10,11}$", message = "휴대폰번호는 10~11자리 숫자여야 합니다.")
    @Schema(description = "휴대폰번호 (010XXXXXXXX 형식)", example = "01012345678")
    String phoneNumber,

    @Schema(description = "푸시 알림 수신 동의", example = "true")
    boolean pushNotificationEnabled,

    @Schema(description = "마케팅 정보 수신 동의", example = "false")
    boolean marketingInfoEnabled,

    @Schema(description = "이벤트 정보 수신 동의", example = "false")
    boolean eventInfoEnabled,

    @Schema(description = "SMS 휴대폰 인증 토큰 (휴대폰번호 입력 시 필수)")
    String smsVerifyToken,

    @Schema(description = "이메일 인증 토큰 (회원가입 시 필수)")
    @NotBlank(message = "이메일 인증 토큰을 입력해주세요.")
    String mailVerifyToken,

    @Schema(description = "추천인 닉네임 (선택)", example = "맛집탐험가")
    @Size(min = 2, max = 20, message = "추천인 닉네임은 2~20자로 입력해주세요.")
    String referrerNickname
) {

    public com.tastyhouse.application.auth.port.in.AuthSignUpCommand toCommand() {
        return new com.tastyhouse.application.auth.port.in.AuthSignUpCommand(
            username, password, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled,
            smsVerifyToken, mailVerifyToken, referrerNickname
        );
    }
}
