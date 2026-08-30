package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.adminapplication.shop.port.in.ShopHygieneBadgeCreateCommand;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 위생 인증 뱃지 등록 요청")
public record ShopHygieneBadgeCreateRequest(
    @NotBlank(message = "위생 인증 유형은 필수입니다.")
    @Schema(description = "위생 인증 유형", example = "FOOD_SAFETY_CERTIFIED",
        allowableValues = {"FOOD_SAFETY_CERTIFIED", "CESCO_BLUE", "CESCO_WHITE"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    String badgeType,

    @NotNull(message = "인증일은 필수입니다.")
    @Schema(description = "인증일", example = "2026-01-15", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDate certifiedDate,

    @Schema(description = "세스코 최근 점검월 (\"2026-03\" 형태, nullable)", example = "2026-03")
    String lastInspectionMonth
) {

    public ShopHygieneBadgeCreateCommand toCommand(Long shopId) {
        return new ShopHygieneBadgeCreateCommand(shopId, badgeType, certifiedDate, lastInspectionMonth);
    }
}
