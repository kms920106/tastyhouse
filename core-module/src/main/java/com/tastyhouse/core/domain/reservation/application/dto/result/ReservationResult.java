package com.tastyhouse.core.domain.reservation.application.dto.result;

import com.tastyhouse.core.domain.reservation.domain.model.Reservation;
import com.tastyhouse.core.domain.reservation.domain.model.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ReservationResult(
    Long id,
    Long shopId,
    String shopName,
    Long memberId,
    LocalDate reservationDate,
    LocalTime reservationTime,
    Integer partySize,
    ReservationStatus status,
    String request,
    LocalDateTime createdAt
) {
    public static ReservationResult from(Reservation reservation, String shopName) {
        return new ReservationResult(
            reservation.getId(),
            reservation.getShopId(),
            shopName,
            reservation.getMemberId(),
            reservation.getReservationDate(),
            reservation.getReservationTime(),
            reservation.getPartySize(),
            reservation.getStatus(),
            reservation.getRequest(),
            reservation.getCreatedAt()
        );
    }
}
