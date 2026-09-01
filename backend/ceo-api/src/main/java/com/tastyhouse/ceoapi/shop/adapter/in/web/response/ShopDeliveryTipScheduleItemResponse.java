package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryTipScheduleResult;

@Schema(description = "시간별 추가 배달팁 한 건")
public record ShopDeliveryTipScheduleItemResponse(
    @Schema(description = "시간별 배달팁 ID", example = "10")
    long id,

    @Schema(description = "요일 구분", example = "WEEKEND",
        allowableValues = {"DAILY", "WEEKDAY", "WEEKEND", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"})
    String dayType,

    @Schema(description = "적용 시작 시각", example = "18:00")
    LocalTime startTime,

    @Schema(description = "적용 종료 시각", example = "22:00")
    LocalTime endTime,

    @Schema(description = "이 시간대의 추가 배달팁(원)", example = "1000")
    int tipAmount
) {
    public static ShopDeliveryTipScheduleItemResponse from(ShopDeliveryTipScheduleResult result) {
        return new ShopDeliveryTipScheduleItemResponse(
            result.id(),
            result.dayType(),
            result.startTime(),
            result.endTime(),
            result.tipAmount()
        );
    }
}
