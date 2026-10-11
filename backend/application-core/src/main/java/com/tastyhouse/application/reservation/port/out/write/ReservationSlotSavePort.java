package com.tastyhouse.application.reservation.port.out.write;

import com.tastyhouse.domain.reservation.model.ReservationSlot;

public interface ReservationSlotSavePort {

    ReservationSlot save(ReservationSlot slot);

    void saveImmediately(ReservationSlot slot);
}
