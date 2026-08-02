package com.tastyhouse.ceoapi.shop.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 영업 임시중지 응답")
public record ShopSuspensionResponse(
    @Schema(description = "임시중지 ID", example = "1")
    Long id,

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "임시중지 사유", example = "BAD_WEATHER",
        allowableValues = {"EARLY_CLOSE", "OPEN_DELAY", "SHOP_CIRCUMSTANCE", "UNREACHABLE", "TERMINATION_REQUEST", "BAD_WEATHER"})
    String reason,

    @Schema(description = "대상 주문수단 (null이면 전체)", example = "DELIVERY",
        allowableValues = {"TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"})
    String orderMethod,

    @Schema(description = "중지 시작 시각", example = "2026-07-25T09:00:00")
    LocalDateTime startAt,

    @Schema(description = "중지 종료 시각", example = "2026-07-25T18:00:00")
    LocalDateTime endAt,

    @Schema(description = "해제 시각 (해제 전이면 null)", example = "2026-07-25T15:00:00")
    LocalDateTime releasedAt
) {
    public static ShopSuspensionResponse of(
        Long id,
        Long shopId,
        String reason,
        String orderMethod,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime releasedAt
    ) {
        return new ShopSuspensionResponse(
            id,
            shopId,
            reason,
            orderMethod,
            startAt,
            endAt,
            releasedAt
        );
    }
}
