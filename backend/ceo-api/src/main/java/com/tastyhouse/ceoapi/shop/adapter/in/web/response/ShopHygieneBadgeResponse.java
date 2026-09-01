package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopHygieneBadgeResult;

/**
 * 가게 위생 인증 뱃지 응답.
 *
 * <p>{@link ShopBusinessHourResponse}와 같은 이유로 이 모듈이 소유한다.
 */
@Schema(description = "가게 위생 인증 뱃지 응답")
public record ShopHygieneBadgeResponse(
    @Schema(description = "위생 인증 뱃지 ID", example = "1")
    Long id,

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "위생 인증 유형", example = "FOOD_SAFETY_CERTIFIED",
        allowableValues = {"FOOD_SAFETY_CERTIFIED", "CESCO_BLUE", "CESCO_WHITE"})
    String badgeType,

    @Schema(description = "인증일", example = "2026-01-15")
    LocalDate certifiedDate,

    @Schema(description = "세스코 최근 점검월 (\"2026-03\" 형태, nullable)", example = "2026-03")
    String lastInspectionMonth
) {
    public static ShopHygieneBadgeResponse from(ShopHygieneBadgeResult result) {
        return new ShopHygieneBadgeResponse(
            result.id(),
            result.shopId(),
            result.badgeType().name(),
            result.certifiedDate(),
            result.lastInspectionMonth()
        );
    }
}
