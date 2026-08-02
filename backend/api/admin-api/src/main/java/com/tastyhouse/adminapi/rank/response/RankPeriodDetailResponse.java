package com.tastyhouse.adminapi.rank.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "랭킹 기간 상세 응답")
public record RankPeriodDetailResponse(
    @Schema(description = "기간 ID", example = "1")
    Long id,

    @Schema(description = "시작일시", example = "2026-08-01T00:00:00")
    LocalDateTime startAt,

    @Schema(description = "종료일시", example = "2026-08-31T23:59:59")
    LocalDateTime endAt,

    @Schema(description = "노출 여부", example = "true")
    boolean visible,

    @Schema(description = "생성일시", example = "2026-07-01T10:00:00")
    LocalDateTime createdAt,

    @Schema(description = "수정일시", example = "2026-07-01T10:00:00")
    LocalDateTime updatedAt
) {

    public static RankPeriodDetailResponse from(
        Long id,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean visible,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new RankPeriodDetailResponse(id, startAt, endAt, visible, createdAt, updatedAt);
    }
}
