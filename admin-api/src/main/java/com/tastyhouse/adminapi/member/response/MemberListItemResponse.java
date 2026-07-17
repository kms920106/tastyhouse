package com.tastyhouse.adminapi.member.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 목록 항목 응답")
public record MemberListItemResponse(
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

    @Schema(description = "회원 등급", example = "GOURMET")
    String memberGrade,

    @Schema(description = "회원 상태", example = "ACTIVE")
    String memberStatus,

    @Schema(description = "프로필 이미지 저장 경로 (미설정 시 null)", example = "members/profile/1/abc.jpg")
    String profileImageFilePath,

    @Schema(description = "가입일시", example = "2025-03-01T10:00:00")
    LocalDateTime createdAt
) {

    public static MemberListItemResponse from(
        Long id,
        String username,
        String nickname,
        String fullName,
        String phoneNumber,
        String gender,
        String memberGrade,
        String memberStatus,
        String profileImageFilePath,
        LocalDateTime createdAt
    ) {
        return new MemberListItemResponse(
            id,
            username,
            nickname,
            fullName,
            phoneNumber,
            gender,
            memberGrade,
            memberStatus,
            profileImageFilePath,
            createdAt
        );
    }
}
