package com.tastyhouse.application.reservation.port.out.write;

import com.tastyhouse.domain.reservation.model.Reservation;

public interface ReservationSavePort {

    Reservation save(Reservation reservation);
}
