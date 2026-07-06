package com.tastyhouse.webapi.reservation.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.reservation.application.dto.result.ReservationResult;

@Schema(description = "예약 응답")
public record ReservationCompleteDetailResponse(
    @Schema(description = "예약 ID", example = "1")
    Long id,

    @Schema(description = "가게명", example = "맛있는집")
    String shopName,

    @Schema(description = "가게 썸네일 이미지 URL", example = "https://cdn.tastyhouse.com/shop/1/thumbnail.jpg")
    String shopImageUrl,

    @Schema(description = "예약 일시", example = "2026-06-10T13:00:00")
    LocalDateTime reservationAt,

    @Schema(description = "방문 인원수", example = "4")
    Integer partySize
) {
    public static ReservationCompleteDetailResponse from(ReservationResult result, String shopImageUrl) {
        return new ReservationCompleteDetailResponse(
            result.id().value(),
            result.shopName(),
            shopImageUrl,
            LocalDateTime.of(result.reservationDate(), result.reservationTime()),
            result.partySize()
        );
    }
}
