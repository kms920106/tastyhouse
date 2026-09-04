package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.application.shop.port.in.ShopBreakTimeManagementCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeManagementUpdateCommand;

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

    public ShopBreakTimeManagementCreateCommand toCreateCommand(Long adminId, Long shopId) {
        return new ShopBreakTimeManagementCreateCommand(adminId, shopId, dayType, startTime, endTime);
    }

    public ShopBreakTimeManagementUpdateCommand toUpdateCommand(Long adminId, Long breakTimeId) {
        return new ShopBreakTimeManagementUpdateCommand(adminId, breakTimeId, dayType, startTime, endTime);
    }
}
