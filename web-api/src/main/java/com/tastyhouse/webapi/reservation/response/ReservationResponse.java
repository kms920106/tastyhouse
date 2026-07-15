package com.tastyhouse.webapi.reservation.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.reservation.application.dto.result.ReservationResult;

@Schema(description = "예약 응답")
public record ReservationResponse(
    @Schema(description = "예약 ID", example = "1")
    Long id,

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "가게명", example = "맛있는집")
    String shopName,

    @Schema(description = "예약자 회원 ID", example = "2")
    Long memberId,

    @Schema(description = "예약 날짜", example = "2026-06-10")
    LocalDate reservationDate,

    @Schema(description = "예약 시간", example = "13:00")
    LocalTime reservationTime,

    @Schema(description = "방문 인원수", example = "4")
    Integer partySize,

    @Schema(description = "예약 상태", example = "PENDING")
    String status,

    @Schema(description = "요청사항", example = "창가 자리 부탁드립니다")
    String request,

    @Schema(description = "예약 생성 일시")
    LocalDateTime createdAt
) {
    public static ReservationResponse from(ReservationResult result) {
        return new ReservationResponse(
            result.id().value(),
            result.shopId(),
            result.shopName(),
            result.memberId().value(),
            result.reservationDate(),
            result.reservationTime(),
            result.partySize(),
            result.status().name(),
            result.request(),
            result.createdAt()
        );
    }
}
