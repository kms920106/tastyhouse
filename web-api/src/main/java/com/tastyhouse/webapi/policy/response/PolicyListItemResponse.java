package com.tastyhouse.webapi.policy.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "정책 목록 항목 응답")
public record PolicyListItemResponse(
    @Schema(description = "정책 문서 ID", example = "1")
    Long id,

    @Schema(description = "정책 유형", example = "TERMS_OF_SERVICE")
    String type,

    @Schema(description = "버전", example = "1.0.0")
    String version,

    @Schema(description = "제목", example = "이용약관")
    String title,

    @Schema(description = "현재 유효 버전 여부", example = "true")
    boolean current,

    @Schema(description = "시행 일시", example = "2026-01-01T00:00:00")
    LocalDateTime effectiveDate,

    @Schema(description = "생성 일시", example = "2025-12-01T10:00:00")
    LocalDateTime createdAt
) {
    public static PolicyListItemResponse from(
        Long id,
        String type,
        String version,
        String title,
        boolean current,
        LocalDateTime effectiveDate,
        LocalDateTime createdAt
    ) {
        return new PolicyListItemResponse(
            id,
            type,
            version,
            title,
            current,
            effectiveDate,
            createdAt
        );
    }
}
