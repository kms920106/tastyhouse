package com.tastyhouse.webapi.auth.request;

import com.tastyhouse.core.entity.user.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "카카오 소셜 회원가입 요청")
public record KakaoSignUpRequest(

    @Schema(description = "카카오 인가 코드 (소셜 계정 연동에 사용)", example = "abc123")
    @NotBlank(message = "인가 코드를 입력해주세요.")
    String code,

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
    @NotNull(message = "생년월일을 입력해주세요.")
    Integer birthDate,

    @Schema(description = "휴대폰번호 (010XXXXXXXX 형식)", example = "01012345678")
    @Pattern(regexp = "^\\d{10,11}$", message = "휴대폰번호는 10~11자리 숫자여야 합니다.")
    String phoneNumber,

    @Schema(description = "SMS 휴대폰 인증 토큰 (휴대폰번호 입력 시 필수)")
    String phoneVerifyToken,

    @Schema(description = "푸시 알림 수신 동의", example = "true")
    Boolean pushNotificationEnabled,

    @Schema(description = "마케팅 정보 수신 동의", example = "false")
    Boolean marketingInfoEnabled,

    @Schema(description = "이벤트 정보 수신 동의", example = "false")
    Boolean eventInfoEnabled,

    @Schema(description = "추천인 닉네임 (선택)", example = "맛집탐험가")
    @Size(min = 2, max = 20, message = "추천인 닉네임은 2~20자로 입력해주세요.")
    String referrerNickname
) {}
