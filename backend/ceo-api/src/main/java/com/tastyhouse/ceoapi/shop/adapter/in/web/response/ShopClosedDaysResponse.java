package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopClosedDaysResult;

@Schema(description = "가게 휴무 통합 응답 (공휴일 휴무 여부 + 정기 휴무 + 임시 휴무)")
public record ShopClosedDaysResponse(
    @Schema(description = "공휴일 휴무 여부", example = "false")
    boolean closedOnPublicHolidays,

    @Schema(description = "정기 휴무 목록")
    List<ShopRegularClosedDayResponse> regularClosedDays,

    @Schema(description = "임시 휴무 목록")
    List<ShopTemporaryClosureResponse> temporaryClosures
) {
    public static ShopClosedDaysResponse from(ShopClosedDaysResult result) {
        return new ShopClosedDaysResponse(
            result.closedOnPublicHolidays(),
            result.regularClosedDays().stream()
                .map(ShopRegularClosedDayResponse::from)
                .toList(),
            result.temporaryClosures().stream()
                .map(ShopTemporaryClosureResponse::from)
                .toList()
        );
    }
}
