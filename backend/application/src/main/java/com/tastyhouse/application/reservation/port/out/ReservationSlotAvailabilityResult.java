package com.tastyhouse.application.reservation.port.out;

import java.time.LocalDate;
import java.util.List;

public record ReservationSlotAvailabilityResult(
    LocalDate date,
    boolean hasMyReservation,
    List<ReservationSlotResult> slots
) {
}
