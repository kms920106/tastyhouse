package com.tastyhouse.apicommon.shop.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

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
    public static ShopHygieneBadgeResponse of(
        Long id,
        Long shopId,
        String badgeType,
        LocalDate certifiedDate,
        String lastInspectionMonth
    ) {
        return new ShopHygieneBadgeResponse(
            id,
            shopId,
            badgeType,
            certifiedDate,
            lastInspectionMonth
        );
    }
}
