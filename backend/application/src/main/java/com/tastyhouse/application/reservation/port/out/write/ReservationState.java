package com.tastyhouse.application.reservation.port.out.write;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ReservationState(
    Long id,
    Long memberId,
    Long shopId,
    LocalDate reservationDate,
    LocalTime reservationTime,
    Integer partySize,
    String status,
    String request,
    LocalDateTime createdAt
) {
}
