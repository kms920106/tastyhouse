package com.tastyhouse.application.reservation.port.out;

import java.time.LocalTime;

public record SlotOccupancyResult(
    LocalTime slotTime,
    int remaining
) {
}
