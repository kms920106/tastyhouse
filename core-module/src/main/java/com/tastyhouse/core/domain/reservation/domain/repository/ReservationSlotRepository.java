package com.tastyhouse.core.domain.reservation.domain.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.reservation.domain.model.ReservationSlot;

public interface ReservationSlotRepository {

    Optional<ReservationSlot> findByShopAndDateAndTime(Long shopId, LocalDate date, LocalTime time);

    List<ReservationSlot> findByShopAndDate(Long shopId, LocalDate date);

    ReservationSlot save(ReservationSlot slot);
}
