package com.tastyhouse.adminapi.shop.adapter.in.web.response;

import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;

@Schema(description = "가게 운영시간 응답")
public record ShopBusinessHourResponse(
    @Schema(description = "운영시간 ID", example = "1")
    Long id,

    @Schema(description = "요일 유형", example = "WEEKDAY")
    String dayType,

    @Schema(description = "요일 유형 설명", example = "평일")
    String description,

    @Schema(description = "영업 시작 시각", example = "09:00:00")
    LocalTime openTime,

    @Schema(description = "영업 종료 시각", example = "22:00:00")
    LocalTime closeTime,

    @Schema(description = "휴무 여부", example = "false")
    Boolean isClosed,

    @Schema(description = "24시간 영업 여부", example = "false")
    Boolean is24Hours
) {
    public static ShopBusinessHourResponse from(ShopBusinessHourResult result) {
        return new ShopBusinessHourResponse(
            result.id(),
            result.dayType().name(),
            result.dayType().getDescription(),
            result.openTime(),
            result.closeTime(),
            result.closed(),
            result.allDay()
        );
    }
}
