package com.tastyhouse.core.domain.reservation.application.dto.result;

import java.time.LocalDate;
import java.util.List;

public record DailySlotAvailabilityResult(
    LocalDate date,
    boolean hasMyReservation,
    List<SlotAvailability> slots
) {
}
