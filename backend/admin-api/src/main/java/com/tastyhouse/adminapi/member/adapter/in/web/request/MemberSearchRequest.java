package com.tastyhouse.adminapi.member.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 검색 요청")
public record MemberSearchRequest(
    @Schema(description = "닉네임 (부분 일치 검색)", example = "홍길동")
    String nickname,

    @Schema(description = "아이디 (부분 일치 검색)", example = "hong123")
    String username,

    @Schema(description = "휴대폰번호 (부분 일치 검색)", example = "01012345678")
    String phone,

    @Schema(description = "회원 상태 (미지정 시 전체)", example = "ACTIVE", allowableValues = {"ACTIVE", "SUSPENDED", "DELETED"})
    String status,

    @Schema(description = "회원 등급 (미지정 시 전체)", example = "GOURMET", allowableValues = {"NEWCOMER", "ACTIVE", "INSIDER", "GOURMET", "TEHA"})
    String grade
) {
}
