package com.tastyhouse.webapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "브레이크타임 정보")
public record BreakTimeItem(
    @Schema(description = "요일 타입", example = "WEEKDAY")
    String dayType,

    @Schema(description = "요일 타입 설명", example = "평일")
    String dayTypeDescription,

    @Schema(description = "브레이크타임 시작", example = "15:00")
    String startTime,

    @Schema(description = "브레이크타임 종료", example = "17:00")
    String endTime
) {
    public static BreakTimeItem from(
        String dayType,
        String dayTypeDescription,
        String startTime,
        String endTime
    ) {
        return new BreakTimeItem(
            dayType,
            dayTypeDescription,
            startTime,
            endTime
        );
    }
}
