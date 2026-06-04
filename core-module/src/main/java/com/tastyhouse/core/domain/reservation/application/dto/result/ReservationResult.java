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
    public static ReservationResult from(
        Reservation reservation,
        String shopName,
        String shopImageUrl,
        String shopRoadAddress,
        String shopLotAddress
    ) {
        return new ReservationResult(
            reservation.getId(),
            reservation.getShopId(),
            shopName,
            shopImageUrl,
            shopRoadAddress,
            shopLotAddress,
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
