package com.tastyhouse.webapi.auth.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tastyhouse.core.domain.member.domain.model.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 소셜 로그인 계정 연동 시 회원가입 폼 자동 매핑용 공통 프로필.
 * 소셜 플랫폼마다 제공 가능한 필드가 다르므로 미제공 항목은 null로 반환된다.
 *
 * 플랫폼별 제공 필드:
 * - 카카오: providerId, email, nickname, profileImageUrl, name(동의 시), phoneNumber(동의 시), gender(동의 시)
 * - 네이버: providerId, email, nickname, profileImageUrl, name, phoneNumber, gender, birthYear, birthMonth, birthDay
 * - 페이스북: providerId, email, name, profileImageUrl
 * - 애플: providerId, email
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "소셜 프로필 정보. 플랫폼마다 제공 가능한 필드가 달라 미제공 항목은 null로 반환됩니다.")
public record SocialProfile(

    @Schema(description = "소셜 계정 식별자 (providerId)")
    String providerId,

    @Schema(description = "이메일. 회원가입 아이디 자동 매핑용")
    String email,

    @Schema(description = "닉네임. 카카오·네이버에서 제공")
    String nickname,

    @Schema(description = "프로필 이미지 URL. 카카오·네이버·페이스북에서 제공")
    String profileImageUrl,

    @Schema(description = "실명. 카카오(동의 시)·네이버·페이스북에서 제공. 회원가입 이름 자동 매핑용")
    String name,

    @Schema(description = "휴대폰번호. 카카오(동의 시)·네이버에서 제공")
    String phoneNumber,

    @Schema(description = "성별. MALE 또는 FEMALE. 카카오(동의 시)·네이버에서 제공")
    Gender gender,

    @Schema(description = "출생 연도 (예: \"1990\"). 네이버에서 제공")
    String birthYear,

    @Schema(description = "출생 월 (예: \"1\", \"12\"). 네이버에서 제공")
    String birthMonth,

    @Schema(description = "출생 일 (예: \"5\", \"31\"). 네이버에서 제공")
    String birthDay
) {}
