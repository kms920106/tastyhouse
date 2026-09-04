package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import com.tastyhouse.application.product.port.in.ProductExposureHourCommand;

/**
 * 메뉴 노출 요일·시간대 한 줄.
 *
 * <p>{@code startTime}·{@code endTime}을 모두 비우면 그 요일 <b>종일</b> 노출이다.
 * {@code endTime}이 {@code startTime}보다 이르면 자정을 넘긴다(예: 22:00~02:00 야식).
 */
@Schema(description = "메뉴 노출 요일·시간대")
public record ProductExposureHourRequest(
    @NotBlank(message = "요일 구분은 필수입니다.")
    @Schema(description = "요일 구분. 묶음(DAILY/WEEKDAY/WEEKEND/HOLIDAY)과 개별 요일은 함께 쓸 수 없습니다.",
        example = "DAILY", requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"DAILY", "WEEKDAY", "WEEKEND", "HOLIDAY",
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"})
    String dayType,

    @Schema(description = "노출 시작 시각. 종료 시각과 함께 비우면 종일 노출입니다.", example = "11:00")
    LocalTime startTime,

    @Schema(description = "노출 종료 시각. 시작보다 이르면 자정을 넘깁니다.", example = "14:00")
    LocalTime endTime
) {

    public ProductExposureHourCommand toCommand() {
        return new ProductExposureHourCommand(dayType, startTime, endTime);
    }
}
