package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.adminapi.shop.application.port.in.ShopBreakTimeCreateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopBreakTimeUpdateCommand;

import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "가게 브레이크타임 등록/수정 요청")
public record ShopBreakTimeSaveRequest(
    @NotBlank(message = "요일 유형은 필수입니다.")
    @Schema(description = "요일 유형", example = "WEEKDAY", allowableValues = {"DAILY", "WEEKDAY", "WEEKEND", "HOLIDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String dayType,

    @NotNull(message = "시작 시각은 필수입니다.")
    @Schema(description = "브레이크타임 시작 시각", example = "15:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalTime startTime,

    @NotNull(message = "종료 시각은 필수입니다.")
    @Schema(description = "브레이크타임 종료 시각", example = "17:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalTime endTime
) {

    public ShopBreakTimeCreateCommand toCreateCommand(Long adminId, Long shopId) {
        return new ShopBreakTimeCreateCommand(adminId, shopId, dayType, startTime, endTime);
    }

    public ShopBreakTimeUpdateCommand toUpdateCommand(Long adminId, Long breakTimeId) {
        return new ShopBreakTimeUpdateCommand(adminId, breakTimeId, dayType, startTime, endTime);
    }
}
