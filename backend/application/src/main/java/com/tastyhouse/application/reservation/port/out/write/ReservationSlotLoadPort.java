package com.tastyhouse.application.reservation.port.out.write;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import com.tastyhouse.domain.reservation.model.ReservationSlot;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ReservationSlotLoadPort {

    Optional<ReservationSlot> findByShopAndDateAndTime(ShopId shopId, LocalDate date, LocalTime time);
}
