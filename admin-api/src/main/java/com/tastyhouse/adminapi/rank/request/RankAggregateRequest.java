package com.tastyhouse.adminapi.rank.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "랭킹 수동 집계 요청")
public record RankAggregateRequest(
    @Schema(description = "랭킹 타입 (미지정 시 전체 타입 집계)", allowableValues = {"ALL", "MONTHLY", "WEEKLY"}, example = "WEEKLY")
    String type,

    @Schema(description = "집계 기준일 (type 지정 시에만 사용, 미지정 시 오늘)", example = "2026-07-18")
    LocalDate baseDate,

    @Schema(description = "집계 상위 개수 (type 지정 시에만 사용)", example = "10")
    Integer limit
) {

    public RankAggregateRequest {
        if (limit == null) {
            limit = 10;
        }
    }
}
