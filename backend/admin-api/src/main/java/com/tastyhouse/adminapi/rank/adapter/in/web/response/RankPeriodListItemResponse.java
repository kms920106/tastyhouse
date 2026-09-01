package com.tastyhouse.adminapi.rank.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.rank.port.out.RankPeriodResult;

@Schema(description = "랭킹 기간 목록 항목 응답")
public record RankPeriodListItemResponse(
    @Schema(description = "기간 ID", example = "1")
    Long id,

    @Schema(description = "시작일시", example = "2026-08-01T00:00:00")
    LocalDateTime startAt,

    @Schema(description = "종료일시", example = "2026-08-31T23:59:59")
    LocalDateTime endAt,

    @Schema(description = "노출 여부", example = "true")
    boolean visible
) {

    public static RankPeriodListItemResponse from(RankPeriodResult result) {
        return new RankPeriodListItemResponse(
            result.id(),
            result.startAt(),
            result.endAt(),
            result.visible()
        );
    }
}
