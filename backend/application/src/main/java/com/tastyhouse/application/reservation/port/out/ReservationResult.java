package com.tastyhouse.application.reservation.port.out;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.tastyhouse.domain.reservation.model.ReservationStatus;

public record ReservationResult(
    Long id,
    Long shopId,
    String shopName,
    String shopImageUrl,
    String shopRoadAddress,
    String shopLotAddress,
    Long memberId,
    LocalDate reservationDate,
    LocalTime reservationTime,
    Integer partySize,
    ReservationStatus status,
    String request,
    LocalDateTime createdAt
) {
}
