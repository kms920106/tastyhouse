package com.tastyhouse.adminapi.rank.adapter.in.web.request;

import com.tastyhouse.adminapi.rank.application.port.in.RankPeriodCreateCommand;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "랭킹 기간 등록 요청")
public record RankPeriodCreateRequest(
    @NotNull(message = "시작일시는 필수입니다.")
    @Schema(description = "시작일시", example = "2026-08-01T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime startAt,

    @NotNull(message = "종료일시는 필수입니다.")
    @Schema(description = "종료일시", example = "2026-08-31T23:59:59", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime endAt,

    @Schema(description = "노출 여부 (미지정 시 노출)", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    boolean visible
) {

    public RankPeriodCreateCommand toCommand() {
        return new RankPeriodCreateCommand(startAt(), endAt(), visible());
    }
}
