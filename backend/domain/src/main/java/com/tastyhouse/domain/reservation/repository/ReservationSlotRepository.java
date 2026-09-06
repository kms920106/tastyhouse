package com.tastyhouse.domain.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import com.tastyhouse.domain.reservation.model.ReservationSlot;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ReservationSlotRepository {
    Optional<ReservationSlot> findByShopAndDateAndTime(ShopId shopId, LocalDate date, LocalTime time);

    ReservationSlot save(ReservationSlot slot);

    void saveAndFlush(ReservationSlot slot);
}
