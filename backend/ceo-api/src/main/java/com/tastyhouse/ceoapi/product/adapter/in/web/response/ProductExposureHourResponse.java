package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메뉴 노출 요일·시간대 한 줄.
 *
 * <p>{@code startTime}·{@code endTime}이 모두 {@code null}이면 그 요일 종일 노출이다.
 */
@Schema(description = "메뉴 노출 요일·시간대")
public record ProductExposureHourResponse(
    @Schema(description = "요일 구분", example = "DAILY",
        allowableValues = {"DAILY", "WEEKDAY", "WEEKEND", "HOLIDAY",
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"})
    String dayType,

    @Schema(description = "노출 시작 시각. 종일 노출이면 null", example = "11:00")
    LocalTime startTime,

    @Schema(description = "노출 종료 시각. 시작보다 이르면 자정을 넘깁니다.", example = "14:00")
    LocalTime endTime
) {

    public static ProductExposureHourResponse from(
        String dayType,
        LocalTime startTime,
        LocalTime endTime
    ) {
        return new ProductExposureHourResponse(
            dayType,
            startTime,
            endTime
        );
    }
}
