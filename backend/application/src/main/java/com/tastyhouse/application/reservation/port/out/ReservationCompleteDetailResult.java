package com.tastyhouse.application.reservation.port.out;

import java.time.LocalDateTime;

public record ReservationCompleteDetailResult(
    Long id,
    String shopName,
    String shopImageUrl,
    LocalDateTime reservationAt,
    Integer partySize
) {
}
