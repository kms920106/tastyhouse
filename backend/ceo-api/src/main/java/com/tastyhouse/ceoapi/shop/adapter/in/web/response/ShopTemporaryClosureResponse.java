package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopTemporaryClosureResult;

@Schema(description = "가게 임시 휴무 응답")
public record ShopTemporaryClosureResponse(
    @Schema(description = "임시 휴무 ID", example = "1")
    Long id,

    @Schema(description = "임시 휴무 시작일", example = "2026-08-01")
    LocalDate startDate,

    @Schema(description = "임시 휴무 종료일", example = "2026-08-03")
    LocalDate endDate
) {
    public static ShopTemporaryClosureResponse from(ShopTemporaryClosureResult result) {
        return new ShopTemporaryClosureResponse(
            result.id(),
            result.startDate(),
            result.endDate()
        );
    }
}
