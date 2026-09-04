package com.tastyhouse.adminapi.rank.adapter.in.web.request;

import com.tastyhouse.application.rank.port.in.RankPeriodUpdateCommand;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "랭킹 기간 수정 요청")
public record RankPeriodUpdateRequest(
    @NotNull(message = "시작일시는 필수입니다.")
    @Schema(description = "시작일시", example = "2026-08-01T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime startAt,

    @NotNull(message = "종료일시는 필수입니다.")
    @Schema(description = "종료일시", example = "2026-08-31T23:59:59", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime endAt,

    @Schema(description = "노출 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean visible
) {

    public RankPeriodUpdateCommand toCommand(Long rankPeriodId) {
        return new RankPeriodUpdateCommand(rankPeriodId, startAt(), endAt(), visible());
    }
}
