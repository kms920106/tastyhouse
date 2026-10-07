package com.tastyhouse.application.reservation.port.in;

import java.time.LocalDate;

import com.tastyhouse.application.reservation.port.out.ReservationSlotAvailabilityResult;

public interface ReservationAvailabilityQueryUseCase {

    ReservationSlotAvailabilityResult getAvailability(Long shopId, LocalDate date, Long memberId);
}
