package com.tastyhouse.webapplication.policy.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "정책 상세 조회 응답")
public record PolicyDetailResponse(
    @Schema(description = "정책 문서 ID", example = "1")
    Long id,

    @Schema(description = "정책 유형", example = "TERMS_OF_SERVICE")
    String type,

    @Schema(description = "버전", example = "1.0.0")
    String version,

    @Schema(description = "제목", example = "이용약관")
    String title,

    @Schema(description = "본문 내용", example = "제1조(목적) 본 약관은...")
    String content,

    @Schema(description = "현재 유효 버전 여부", example = "true")
    boolean current,

    @Schema(description = "필수 동의 여부", example = "true")
    boolean mandatory,

    @Schema(description = "시행 일시", example = "2026-01-01T00:00:00")
    LocalDateTime effectiveDate,

    @Schema(description = "생성 일시", example = "2025-12-01T10:00:00")
    LocalDateTime createdAt,

    @Schema(description = "수정 일시", example = "2025-12-15T09:30:00")
    LocalDateTime updatedAt
) {
    public static PolicyDetailResponse from(
        Long id,
        String type,
        String version,
        String title,
        String content,
        boolean current,
        boolean mandatory,
        LocalDateTime effectiveDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new PolicyDetailResponse(
            id,
            type,
            version,
            title,
            content,
            current,
            mandatory,
            effectiveDate,
            createdAt,
            updatedAt
        );
    }
}
