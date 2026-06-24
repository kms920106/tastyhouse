package com.tastyhouse.webapi.member.response;

import com.tastyhouse.core.domain.member.domain.model.Gender;
import com.tastyhouse.core.domain.member.domain.model.Member;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "개인정보 조회 응답")
public record PersonalInfoResponse(
    @Schema(description = "아이디 (이메일)", example = "kimcs1234@naver.com")
    String email,

    @Schema(description = "이름", example = "김철수")
    String fullName,

    @Schema(description = "휴대폰번호", example = "01012345678")
    String phoneNumber,

    @Schema(description = "생년월일 (YYYYMMDD)", example = "20200717")
    Integer birthDate,

    @Schema(description = "성별 (MALE / FEMALE)", example = "FEMALE")
    Gender gender,

    @Schema(description = "푸시 알림 수신 동의", example = "false")
    boolean pushNotificationEnabled,

    @Schema(description = "마케팅 정보 수신 동의", example = "false")
    boolean marketingInfoEnabled,

    @Schema(description = "이벤트 정보 수신 동의", example = "false")
    boolean eventInfoEnabled
) {
    public static PersonalInfoResponse from(Member member) {
        return new PersonalInfoResponse(
            member.getUsername(),
            member.getFullName(),
            member.getPhoneNumber().getValue(),
            member.getBirthDate(),
            member.getGender(),
            member.isPushNotificationEnabled(),
            member.isMarketingInfoEnabled(),
            member.isEventInfoEnabled()
        );
    }
}
