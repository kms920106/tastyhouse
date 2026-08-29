package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 임시 휴무 응답")
public record ShopTemporaryClosureResponse(
    @Schema(description = "임시 휴무 ID", example = "1")
    Long id,

    @Schema(description = "임시 휴무 시작일", example = "2026-08-01")
    LocalDate startDate,

    @Schema(description = "임시 휴무 종료일", example = "2026-08-03")
    LocalDate endDate
) {
    public static ShopTemporaryClosureResponse from(
        Long id,
        LocalDate startDate,
        LocalDate endDate
    ) {
        return new ShopTemporaryClosureResponse(
            id,
            startDate,
            endDate
        );
    }
}
