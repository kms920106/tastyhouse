package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopClosedDayResult;

@Schema(description = "휴무일 정보")
public record ShopClosedDayItem(
    @Schema(description = "휴무일 타입", example = "EVERY_WEEK_MONDAY")
    String closedDayType,

    @Schema(description = "휴무일 설명", example = "매주 월요일")
    String description
) {
    public static ShopClosedDayItem from(ShopClosedDayResult result) {
        return new ShopClosedDayItem(
            result.closedDayType().name(),
            result.closedDayType().getDescription()
        );
    }
}
