package com.tastyhouse.adminapi.member.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 상세 응답")
public record MemberDetailResponse(
    @Schema(description = "회원 ID", example = "1")
    Long id,

    @Schema(description = "로그인 아이디", example = "hong123")
    String username,

    @Schema(description = "닉네임", example = "홍길동")
    String nickname,

    @Schema(description = "실명", example = "홍길동")
    String fullName,

    @Schema(description = "휴대폰번호", example = "01012345678")
    String phoneNumber,

    @Schema(description = "성별", example = "MALE")
    String gender,

    @Schema(description = "생년월일 (YYYYMMDD)", example = "19900101")
    Integer birthDate,

    @Schema(description = "회원 등급", example = "GOURMET")
    String memberGrade,

    @Schema(description = "회원 상태", example = "ACTIVE")
    String memberStatus,

    @Schema(description = "상태 메시지 (자기소개, 미설정 시 null)", example = "안녕하세요")
    String statusMessage,

    @Schema(description = "프로필 이미지 URL (미설정 시 null)", example = "https://cdn.tastyhouse.com/members/profile/1/abc.jpg")
    String profileImageUrl,

    @Schema(description = "푸시 알림 동의 여부", example = "true")
    boolean pushNotificationEnabled,

    @Schema(description = "마케팅 정보 수신 동의 여부", example = "false")
    boolean marketingInfoEnabled,

    @Schema(description = "이벤트 정보 수신 동의 여부", example = "false")
    boolean eventInfoEnabled,

    @Schema(description = "가입일시", example = "2025-03-01T10:00:00")
    LocalDateTime createdAt
) {

    public static MemberDetailResponse from(
        Long id,
        String username,
        String nickname,
        String fullName,
        String phoneNumber,
        String gender,
        Integer birthDate,
        String memberGrade,
        String memberStatus,
        String statusMessage,
        String profileImageUrl,
        boolean pushNotificationEnabled,
        boolean marketingInfoEnabled,
        boolean eventInfoEnabled,
        LocalDateTime createdAt
    ) {
        return new MemberDetailResponse(
            id,
            username,
            nickname,
            fullName,
            phoneNumber,
            gender,
            birthDate,
            memberGrade,
            memberStatus,
            statusMessage,
            profileImageUrl,
            pushNotificationEnabled,
            marketingInfoEnabled,
            eventInfoEnabled,
            createdAt
        );
    }
}
