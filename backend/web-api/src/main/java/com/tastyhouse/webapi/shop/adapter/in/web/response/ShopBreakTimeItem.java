package com.tastyhouse.webapi.shop.adapter.in.web.response;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopBreakTimeResult;

@Schema(description = "브레이크타임 정보")
public record ShopBreakTimeItem(
    @Schema(description = "요일 타입", example = "WEEKDAY")
    String dayType,

    @Schema(description = "요일 타입 설명", example = "평일")
    String dayTypeDescription,

    @Schema(description = "브레이크타임 시작", example = "15:00")
    String startTime,

    @Schema(description = "브레이크타임 종료", example = "17:00")
    String endTime
) {
    /** 시각 표기 — {@code ShopBusinessHourItem}과 같은 형태를 유지한다. */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static ShopBreakTimeItem from(ShopBreakTimeResult result) {
        return new ShopBreakTimeItem(
            result.dayType().name(),
            result.dayType().getDescription(),
            formatTime(result.startTime()),
            formatTime(result.endTime())
        );
    }

    private static String formatTime(LocalTime time) {
        return time == null ? null : time.format(TIME_FORMATTER);
    }
}
