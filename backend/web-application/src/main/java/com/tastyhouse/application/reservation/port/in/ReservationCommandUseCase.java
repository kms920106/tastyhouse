package com.tastyhouse.application.reservation.port.in;

public interface ReservationCommandUseCase {

    Long createReservation(ReservationCreateCommand command);

    void confirmReservation(ReservationConfirmCommand command);

    void completeReservation(ReservationCompleteCommand command);

    void rejectReservation(ReservationRejectCommand command);

    void cancelReservation(ReservationCancelCommand command);
}
