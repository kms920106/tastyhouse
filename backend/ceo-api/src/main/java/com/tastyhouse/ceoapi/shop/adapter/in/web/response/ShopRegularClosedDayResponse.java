package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopClosedDayResult;

@Schema(description = "가게 정기 휴무일 응답")
public record ShopRegularClosedDayResponse(
    @Schema(description = "휴무일 ID", example = "1")
    Long id,

    @Schema(description = "정기 휴무 유형", example = "EVERY_WEEK_MONDAY")
    String closedDayType,

    @Schema(description = "정기 휴무 유형 설명", example = "매주 월요일")
    String description
) {
    public static ShopRegularClosedDayResponse from(ShopClosedDayResult result) {
        return new ShopRegularClosedDayResponse(
            result.id(),
            result.closedDayType().name(),
            result.closedDayType().getDescription()
        );
    }
}
