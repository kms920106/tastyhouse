package com.tastyhouse.application.reservation.port.out;

import java.time.LocalTime;

public record ReservationSlotResult(
    LocalTime time,
    int remaining,
    boolean available
) {
}
