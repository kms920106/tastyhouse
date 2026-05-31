package com.tastyhouse.core.domain.reservation.application.dto.result;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record DailySlotAvailabilityResult(
    LocalDate date,
    List<SlotAvailability> slots
) {
    public record SlotAvailability(
        LocalTime time,
        int remaining,
        boolean available
    ) {
    }
}
