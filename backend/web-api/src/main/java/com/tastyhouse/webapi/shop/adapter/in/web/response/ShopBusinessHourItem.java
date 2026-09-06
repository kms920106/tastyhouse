package com.tastyhouse.webapi.shop.adapter.in.web.response;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;

@Schema(description = "운영시간 정보")
public record ShopBusinessHourItem(
    @Schema(description = "요일 타입", example = "WEEKDAY")
    String dayType,

    @Schema(description = "요일 타입 설명", example = "평일")
    String dayTypeDescription,

    @Schema(description = "오픈 시간", example = "11:00")
    String openTime,

    @Schema(description = "마감 시간", example = "22:00")
    String closeTime,

    @Schema(description = "휴무 여부", example = "false")
    boolean closed,

    @Schema(description = "24시간 영업 여부", example = "false")
    boolean is24Hours
) {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static ShopBusinessHourItem from(ShopBusinessHourResult result) {
        return new ShopBusinessHourItem(
            result.dayType().name(),
            result.dayType().getDescription(),
            formatTime(result.openTime()),
            formatTime(result.closeTime()),
            Boolean.TRUE.equals(result.closed()),
            Boolean.TRUE.equals(result.allDay())
        );
    }

    private static String formatTime(LocalTime time) {
        return time == null ? null : time.format(TIME_FORMATTER);
    }
}
