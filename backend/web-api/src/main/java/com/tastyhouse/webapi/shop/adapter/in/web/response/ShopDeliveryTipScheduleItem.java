package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryTipScheduleItemResult;

@Schema(description = "시간대별 추가 배달팁")
public record ShopDeliveryTipScheduleItem(
    @Schema(description = "요일 타입(DAILY, WEEKDAY, WEEKEND, MONDAY~SUNDAY)", example = "WEEKEND")
    String dayType,

    @Schema(description = "요일 타입 설명", example = "주말")
    String dayTypeDescription,

    @Schema(description = "적용 시작 시각", example = "18:00")
    String startTime,

    @Schema(description = "적용 종료 시각", example = "22:00")
    String endTime,

    @Schema(description = "이 시간대의 추가 배달팁(원)", example = "1000")
    int tipAmount
) {
    public static ShopDeliveryTipScheduleItem from(ShopDeliveryTipScheduleItemResult result) {
        return new ShopDeliveryTipScheduleItem(
            result.dayType(),
            result.dayTypeDescription(),
            result.startTime(),
            result.endTime(),
            result.tipAmount()
        );
    }
}
