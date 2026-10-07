package com.tastyhouse.application.reservation.port.in;

import com.tastyhouse.application.reservation.port.out.ReservationResult;

public interface ReservationDetailByIdQueryUseCase {

    ReservationResult getReservation(Long id);
}
