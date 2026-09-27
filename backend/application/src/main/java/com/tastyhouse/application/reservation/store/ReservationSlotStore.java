package com.tastyhouse.application.reservation.store;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import com.tastyhouse.application.reservation.port.out.write.ReservationSlotStatePort;
import com.tastyhouse.domain.reservation.model.ReservationSlot;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ReservationSlotStore implements ReservationSlotRepository {
    private final ReservationSlotStatePort reservationSlotStatePort;

    public ReservationSlotStore(ReservationSlotStatePort reservationSlotStatePort) {
        this.reservationSlotStatePort = reservationSlotStatePort;
    }

    @Override
    public Optional<ReservationSlot> findByShopAndDateAndTime(ShopId shopId, LocalDate date, LocalTime time) {
        return reservationSlotStatePort.findByShopAndDateAndTime(shopId.value(), date, time)
            .map(ReservationSlotStateMapper::toDomain);
    }

    @Override
    public ReservationSlot save(ReservationSlot slot) {
        return ReservationSlotStateMapper.toDomain(reservationSlotStatePort.save(ReservationSlotStateMapper.toState(slot)));
    }

    @Override
    public void saveAndFlush(ReservationSlot slot) {
        reservationSlotStatePort.saveAndFlush(ReservationSlotStateMapper.toState(slot));
    }
}
