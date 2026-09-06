package com.tastyhouse.application.reservation.port.out;

import java.time.LocalDateTime;

public record ReservationDetailViewResult(
    Long id,
    Long shopId,
    String shopName,
    String shopImageUrl,
    String shopRoadAddress,
    String shopLotAddress,
    Long memberId,
    String reserverName,
    String reserverPhoneNumber,
    String reserverEmail,
    LocalDateTime reservationAt,
    Integer partySize,
    String status,
    String request,
    LocalDateTime createdAt
) {
}
