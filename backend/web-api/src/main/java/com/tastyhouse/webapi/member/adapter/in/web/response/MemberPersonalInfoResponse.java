package com.tastyhouse.webapi.member.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.member.port.out.MemberPersonalInfoResult;

@Schema(description = "개인정보 조회 응답")
public record MemberPersonalInfoResponse(
    @Schema(description = "아이디 (이메일)", example = "kimcs1234@naver.com")
    String email,

    @Schema(description = "이름", example = "김철수")
    String fullName,

    @Schema(description = "휴대폰번호", example = "01012345678")
    String phoneNumber,

    @Schema(description = "생년월일 (YYYYMMDD)", example = "20200717")
    Integer birthDate,

    @Schema(description = "성별 (MALE / FEMALE)", example = "FEMALE")
    String gender,

    @Schema(description = "푸시 알림 수신 동의", example = "false")
    boolean pushNotificationEnabled,

    @Schema(description = "마케팅 정보 수신 동의", example = "false")
    boolean marketingInfoEnabled,

    @Schema(description = "이벤트 정보 수신 동의", example = "false")
    boolean eventInfoEnabled
) {
    public static MemberPersonalInfoResponse from(MemberPersonalInfoResult result) {
        return new MemberPersonalInfoResponse(
            result.username(),
            result.fullName(),
            result.phoneNumber(),
            result.birthDate(),
            result.gender(),
            result.pushNotificationEnabled(),
            result.marketingInfoEnabled(),
            result.eventInfoEnabled()
        );
    }
}
