package com.tastyhouse.core.domain.reservation.domain.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.reservation.domain.model.ShopReservationSlot;

public interface ShopReservationSlotRepository {

    Optional<ShopReservationSlot> findByShopAndDateAndTime(Long shopId, LocalDate date, LocalTime time);

    List<ShopReservationSlot> findByShopAndDate(Long shopId, LocalDate date);

    ShopReservationSlot save(ShopReservationSlot slot);
}
