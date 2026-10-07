package com.tastyhouse.application.reservation.port.in;

public interface ReservationCreateUseCase {

    Long createReservation(ReservationCreateCommand command);
}
