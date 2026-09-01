package com.tastyhouse.adminapi.shop.adapter.in.web.response;

import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopBreakTimeResult;

/**
 * 가게 브레이크타임 응답.
 *
 * <p>{@link ShopBusinessHourResponse}와 같은 이유로 이 모듈이 소유한다.
 */
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
    public static ShopBreakTimeResponse from(ShopBreakTimeResult result) {
        return new ShopBreakTimeResponse(
            result.id(),
            result.dayType().name(),
            result.dayType().getDescription(),
            result.startTime(),
            result.endTime()
        );
    }
}
