package com.tastyhouse.application.reservation.port.in;

public interface ReservationCancelUseCase {

    void cancelReservation(ReservationCancelCommand command);
}
