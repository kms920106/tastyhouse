package com.tastyhouse.adminapplication.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 정기 휴무일 응답")
public record ShopClosedDayResponse(
    @Schema(description = "휴무일 ID", example = "1")
    Long id,

    @Schema(description = "정기 휴무 유형", example = "EVERY_WEEK_MONDAY")
    String closedDayType,

    @Schema(description = "정기 휴무 유형 설명", example = "매주 월요일")
    String description
) {
    public static ShopClosedDayResponse from(
        Long id,
        String closedDayType,
        String description
    ) {
        return new ShopClosedDayResponse(
            id,
            closedDayType,
            description
        );
    }
}
