package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.ceoapplication.shop.port.in.ShopDeliveryTipScheduleCommand;

@Schema(description = "시간별 추가 배달팁 한 건")
public record ShopDeliveryTipScheduleItemRequest(
    @NotBlank(message = "요일 구분은 필수입니다.")
    @Schema(description = "요일 구분. 공휴일(HOLIDAY)은 전용 엔드포인트로 설정합니다", example = "WEEKEND",
        allowableValues = {"DAILY", "WEEKDAY", "WEEKEND", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    String dayType,

    @NotNull(message = "시작 시각은 필수입니다.")
    @Schema(description = "적용 시작 시각", example = "18:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalTime startTime,

    @NotNull(message = "종료 시각은 필수입니다.")
    @Schema(description = "적용 종료 시각. 시작 시각보다 이르면 자정을 넘기는 구간으로 해석합니다", example = "22:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalTime endTime,

    @NotNull(message = "배달팁은 필수입니다.")
    @Min(value = 0, message = "배달팁은 0원 이상이어야 합니다.")
    @Schema(description = "이 시간대의 추가 배달팁(원). 10,000원 이하", example = "1000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer tipAmount
) {

    public ShopDeliveryTipScheduleCommand toCommand() {
        return new ShopDeliveryTipScheduleCommand(dayType(), startTime(), endTime(), tipAmount());
    }
}
