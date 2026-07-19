package com.tastyhouse.adminapi.shop.request;

import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "가게 운영시간 등록/수정 요청")
public record ShopBusinessHourSaveRequest(
    @NotBlank(message = "요일 유형은 필수입니다.")
    @Schema(description = "요일 유형", example = "WEEKDAY", allowableValues = {"DAILY", "WEEKDAY", "WEEKEND", "HOLIDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String dayType,

    @Schema(description = "영업 시작 시각", example = "09:00:00")
    LocalTime openTime,

    @Schema(description = "영업 종료 시각", example = "22:00:00")
    LocalTime closeTime,

    @Schema(description = "휴무 여부", example = "false")
    Boolean isClosed
) {
}
