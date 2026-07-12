package com.tastyhouse.core.domain.reservation.application.dto.result;

import java.time.LocalTime;

public record SlotAvailability(
    LocalTime time,
    int remaining,
    boolean available
) {
}
