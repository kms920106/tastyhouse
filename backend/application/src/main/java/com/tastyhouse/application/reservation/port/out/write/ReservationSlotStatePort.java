package com.tastyhouse.application.reservation.port.out.write;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public interface ReservationSlotStatePort {
    Optional<ReservationSlotState> findByShopAndDateAndTime(Long shopId, LocalDate date, LocalTime time);

    ReservationSlotState save(ReservationSlotState state);

    void saveAndFlush(ReservationSlotState state);
}
