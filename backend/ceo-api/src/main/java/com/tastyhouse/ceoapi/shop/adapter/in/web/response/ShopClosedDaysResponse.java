package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 휴무 통합 응답 (공휴일 휴무 여부 + 정기 휴무 + 임시 휴무)")
public record ShopClosedDaysResponse(
    @Schema(description = "공휴일 휴무 여부", example = "false")
    boolean closedOnPublicHolidays,

    @Schema(description = "정기 휴무 목록")
    List<ShopRegularClosedDayResponse> regularClosedDays,

    @Schema(description = "임시 휴무 목록")
    List<ShopTemporaryClosureResponse> temporaryClosures
) {
    public static ShopClosedDaysResponse from(
        boolean closedOnPublicHolidays,
        List<ShopRegularClosedDayResponse> regularClosedDays,
        List<ShopTemporaryClosureResponse> temporaryClosures
    ) {
        return new ShopClosedDaysResponse(
            closedOnPublicHolidays,
            regularClosedDays,
            temporaryClosures
        );
    }
}
