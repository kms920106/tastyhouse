package com.tastyhouse.webapplication.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "휴무일 정보")
public record ShopClosedDayItem(
    @Schema(description = "휴무일 타입", example = "EVERY_WEEK_MONDAY")
    String closedDayType,

    @Schema(description = "휴무일 설명", example = "매주 월요일")
    String description
) {
    public static ShopClosedDayItem from(
        String closedDayType,
        String description
    ) {
        return new ShopClosedDayItem(
            closedDayType,
            description
        );
    }
}
