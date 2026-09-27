package com.tastyhouse.application.reservation.port.out.write;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationSlotState(
    Long id,
    Long shopId,
    LocalDate slotDate,
    LocalTime slotTime,
    Integer capacity,
    Integer reservedCount,
    Long version
) {
}
