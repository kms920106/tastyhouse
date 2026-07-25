package com.tastyhouse.ceoapi.shop.response;

import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 브레이크타임 응답")
public record ShopBreakTimeResponse(
    @Schema(description = "브레이크타임 ID", example = "1")
    Long id,

    @Schema(description = "요일 유형", example = "WEEKDAY")
    String dayType,

    @Schema(description = "요일 유형 설명", example = "평일")
    String description,

    @Schema(description = "시작 시각", example = "15:00:00")
    LocalTime startTime,

    @Schema(description = "종료 시각", example = "17:00:00")
    LocalTime endTime
) {
    public static ShopBreakTimeResponse from(
        Long id,
        String dayType,
        String description,
        LocalTime startTime,
        LocalTime endTime
    ) {
        return new ShopBreakTimeResponse(
            id,
            dayType,
            description,
            startTime,
            endTime
        );
    }
}
