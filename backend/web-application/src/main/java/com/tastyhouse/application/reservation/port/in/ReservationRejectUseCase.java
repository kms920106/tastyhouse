package com.tastyhouse.application.reservation.port.in;

public interface ReservationRejectUseCase {

    void rejectReservation(ReservationRejectCommand command);
}
